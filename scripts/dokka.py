"""Plan batched Dokka updates from Git objects without checking out source files."""

import argparse
import hashlib
import json
import os
import subprocess
import time
from pathlib import Path


STATE_PATH = "docs/docs/.dokka-inputs.sha256"


def git(*args):
    """Read repository metadata; propagate Git failures to the workflow."""
    return subprocess.check_output(["git", *args])


def is_dokka_input(path):
    """Include declarations, source documentation and build configuration."""
    parts = Path(path).parts
    if any(part in {"vendor", "build", ".gradle"} for part in parts):
        return False
    if path.startswith("docs/"):
        return False
    return (
        path.endswith((".kt", ".java", ".gradle", ".gradle.kts"))
        or ("src" in parts and path.endswith(".md"))
        or path.startswith(("gradle/", "build-logic/"))
        or path in {
            "gradle.properties",
            "gradlew",
            "gradlew.bat",
            "scripts/dokka.py",
            ".github/workflows/dokka.yml",
        }
    )


def fingerprint(ref):
    """Hash paths, modes and blob IDs, including file additions and deletions."""
    entries = git("ls-tree", "-rz", "--full-tree", ref).split(b"\0")
    inputs = [
        entry
        for entry in entries
        if entry and is_dokka_input(entry.split(b"\t", 1)[1].decode())
    ]
    return hashlib.sha256(b"\0".join(inputs)).hexdigest()


def plan(branches, quiet_minutes, should_force):
    """Select quiet branches whose committed inputs differ from the last build."""
    updates = []
    for branch in branches:
        ref = f"refs/remotes/origin/{branch}"
        sha = git("rev-parse", ref).decode().strip()
        digest = fingerprint(sha)
        # A missing marker is expected on the first automated build. Other Git
        # failures must fail the check rather than silently schedule a build.
        has_state = bool(git("ls-tree", sha, "--", STATE_PATH).strip())
        previous = (
            git("show", f"{sha}:{STATE_PATH}").decode().strip() if has_state else None
        )
        age = time.time() - int(git("show", "-s", "--format=%ct", sha))
        if not should_force and previous == digest:
            print(f"{branch}: Dokka inputs are unchanged.")
        elif not should_force and quiet_minutes > 0 and age < quiet_minutes * 60:
            print(f"{branch}: waiting for {quiet_minutes} quiet minutes.")
        else:
            updates.append({"branch": branch, "sha": sha, "digest": digest})
            print(f"{branch}: regenerate Dokka for {sha}.")
    if output := os.environ.get("GITHUB_OUTPUT"):
        with open(output, "a") as stream:
            stream.write(f"matrix={json.dumps({'include': updates})}\n")
            stream.write(f"has_updates={str(bool(updates)).lower()}\n")


def record(digest):
    """Mark inputs only after a successful generation from the exact source tree."""
    if fingerprint("HEAD") != digest:
        raise RuntimeError("Dokka inputs changed after planning; refusing to record them.")
    if not Path("docs/docs/index.html").is_file():
        raise RuntimeError("Dokka did not produce docs/docs/index.html.")
    Path(STATE_PATH).write_text(digest + "\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    planner = commands.add_parser("plan")
    planner.add_argument("branches", nargs="+")
    planner.add_argument("--quiet-minutes", type=int, default=10)
    planner.add_argument("--force", action="store_true")
    recorder = commands.add_parser("record")
    recorder.add_argument("digest")
    args = parser.parse_args()
    if args.command == "plan":
        plan(args.branches, args.quiet_minutes, args.force)
    else:
        record(args.digest)


if __name__ == "__main__":
    main()
