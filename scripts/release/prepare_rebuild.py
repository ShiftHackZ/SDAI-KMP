#!/usr/bin/env python3
"""Populate a native Linux filesystem in a controlled order and run the FOSS build."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time


def copy_source(original, destination, profile, timestamp):
    """Preserve source bytes, modes and symlinks while varying creation order."""
    destination.mkdir()
    for entry in sorted(original.iterdir(), key=lambda path: path.name, reverse=profile == "varied"):
        target = destination / entry.name
        if entry.is_symlink():
            target.symlink_to(os.readlink(entry))
            shutil.copystat(entry, target, follow_symlinks=False)
        elif entry.is_dir():
            copy_source(entry, target, profile, timestamp)
        elif entry.is_file():
            shutil.copy2(entry, target)
        else:
            raise ValueError(f"Unsupported source entry: {entry}")
        if profile == "varied":
            os.utime(target, (timestamp, timestamp), follow_symlinks=False)
    shutil.copystat(original, destination)
    if profile == "varied":
        os.utime(destination, (timestamp, timestamp))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("original", type=Path)
    parser.add_argument("source", type=Path)
    parser.add_argument("profile", choices=("baseline", "varied"))
    parser.add_argument("command", nargs=argparse.REMAINDER)
    args = parser.parse_args()
    command = args.command[1:] if args.command[:1] == ["--"] else args.command
    if sys.platform != "linux":
        parser.error("Native ext4 build preparation requires Linux")
    if not command:
        parser.error("A build command is required")
    original, source = args.original.resolve(), args.source.resolve()
    if source == original or original in source.parents or source.exists():
        raise ValueError("A new build directory outside the original checkout is required")
    filesystem = subprocess.check_output([
        "findmnt", "--noheadings", "--output", "FSTYPE", "--mountpoint", str(source.parent),
    ], text=True).strip()
    if filesystem != "ext4":
        raise ValueError("Build source must reside on the owned native ext4 mount")
    if not (original / ".git").is_dir():
        raise ValueError("An independent checkout with its own Git directory is required")
    if subprocess.check_output(["git", "-C", str(original), "status", "--porcelain"]):
        raise ValueError("Build preparation requires a clean checkout")
    timestamp = int(subprocess.check_output(["git", "-C", str(original), "show", "-s", "--format=%ct"])) - 86400
    if timestamp >= time.time():
        raise ValueError("Rebuild source mtimes must precede the real filesystem clock")
    started = time.monotonic()
    copy_source(original, source, args.profile, timestamp)
    if subprocess.check_output(["git", "-C", str(source), "status", "--porcelain"]):
        raise ValueError("Prepared checkout differs from the original source revision")
    reordered_directories = 0
    directory_order = hashlib.sha256()
    for directory, directories, _ in os.walk(original):
        # Git bookkeeping is outside the source-tree ordering witness.
        directories[:] = sorted(name for name in directories if name != ".git")
        relative = Path(directory).relative_to(original)
        names = [name for name in os.listdir(source / relative) if name != ".git"]
        directory_order.update(os.fsencode(relative) + b"\0")
        for name in names:
            directory_order.update(os.fsencode(name) + b"\0")
        if [name for name in os.listdir(directory) if name != ".git"] != names:
            reordered_directories += 1
    if args.profile == "varied" and reordered_directories == 0:
        raise ValueError("Native ext4 preparation did not change directory traversal order")
    (source / ".git/sdai-repro-inputs.json").write_text(json.dumps({
        "reordered_directories": reordered_directories,
        "filesystem": filesystem,
        "creation_order": "descending" if args.profile == "varied" else "ascending",
        "directory_order_sha256": directory_order.hexdigest(),
    }) + "\n")
    print(f"Prepared {args.profile} on ext4 in {time.monotonic() - started:.1f}s; "
          f"{reordered_directories} directories have different traversal order", flush=True)
    return subprocess.run(command, check=False).returncode


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ValueError, OSError, subprocess.SubprocessError) as error:
        sys.exit(str(error))
