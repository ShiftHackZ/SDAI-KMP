#!/usr/bin/env python3
"""Run the Linux rebuild through disorderfs, varying directory reads and source mtimes."""

import argparse
import json
import os
from pathlib import Path
import subprocess
import sys
import time


def is_mounted(path):
    return subprocess.run(["mountpoint", "-q", str(path)], check=False).returncode == 0


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("mountpoint", type=Path)
    parser.add_argument("command", nargs=argparse.REMAINDER)
    args = parser.parse_args()
    command = args.command[1:] if args.command[:1] == ["--"] else args.command
    if sys.platform != "linux":
        parser.error("Directory-order variation requires Linux with disorderfs/FUSE; macOS is unsupported")
    if not command:
        parser.error("A build command is required after the source directory")
    original = args.source.resolve()
    source = args.mountpoint.resolve()
    if source == original or original in source.parents:
        raise ValueError("The disorderfs mountpoint must be outside the source checkout")
    if not (original / ".git").is_dir():
        raise ValueError("An independent checkout with its own Git directory is required")
    if subprocess.check_output(["git", "-C", str(original), "status", "--porcelain"]):
        raise ValueError("Rebuild preparation requires a clean checkout")
    timestamp = int(subprocess.check_output(["git", "-C", str(original), "show", "-s", "--format=%ct"])) + 86400
    if source.exists():
        raise ValueError("Rebuild mountpoint already exists")
    process = None
    try:
        source.mkdir()
        for directory, directories, files in os.walk(original):
            for name in directories + files:
                os.utime(Path(directory) / name, (timestamp, timestamp), follow_symlinks=False)
        # Foreground process: this wrapper owns its lifetime and always unmounts it.
        process = subprocess.Popen([
            "disorderfs", "-f", "--sort-dirents=yes", "--reverse-dirents=yes",
            str(original), str(source),
        ])
        deadline = time.monotonic() + 30
        while not is_mounted(source):
            if process.poll() is not None or time.monotonic() >= deadline:
                raise ValueError("disorderfs did not mount the rebuild checkout")
            time.sleep(0.1)
        reordered_directories = 0
        for directory, _, _ in os.walk(original):
            relative = Path(directory).relative_to(original)
            if os.listdir(directory) != os.listdir(source / relative):
                reordered_directories += 1
        if reordered_directories == 0:
            raise ValueError("disorderfs did not change directory traversal order")
        (source / ".git/sdai-repro-inputs.json").write_text(json.dumps({
            "reordered_directories": reordered_directories,
        }) + "\n")
        print(f"disorderfs changed traversal order in {reordered_directories} directories", flush=True)
        return subprocess.run(command, check=False).returncode
    finally:
        if source.exists() and is_mounted(source):
            subprocess.run(["fusermount", "-u", str(source)], check=True)
        if process is not None:
            if process.poll() is None:
                process.terminate()
            process.wait(timeout=10)
        if source.exists():
            source.rmdir()


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ValueError, OSError, subprocess.SubprocessError) as error:
        sys.exit(str(error))
