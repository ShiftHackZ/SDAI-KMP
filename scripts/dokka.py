"""Plan batched Dokka updates from Git objects without checking out source files."""

import argparse
import hashlib
import os
import subprocess
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


def plan(should_force, expected_digest):
    """Check current master inputs after the workflow handles push debouncing."""
    sha = git("rev-parse", "refs/remotes/origin/master").decode().strip()
    digest = fingerprint(sha)
    # A missing marker is expected on the first automated build. Other Git
    # failures must fail the check rather than silently request a build.
    has_state = bool(git("ls-tree", sha, "--", STATE_PATH).strip())
    previous = git("show", f"{sha}:{STATE_PATH}").decode().strip() if has_state else None
    has_input_changed = previous != digest
    has_expected_inputs = expected_digest is None or digest == expected_digest
    should_generate = has_expected_inputs and (should_force or has_input_changed)
    if not has_expected_inputs:
        print("master: inputs changed during the debounce; a newer push will check them.")
    elif should_generate:
        print(f"master: regenerate Dokka for {sha}.")
    else:
        print("master: Dokka inputs are unchanged.")
    if output := os.environ.get("GITHUB_OUTPUT"):
        with open(output, "a") as stream:
            stream.write(f"source_sha={sha}\n")
            stream.write(f"input_digest={digest}\n")
            stream.write(f"should_generate={str(should_generate).lower()}\n")


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
    planner.add_argument("--force", action="store_true")
    planner.add_argument("--expected-digest")
    recorder = commands.add_parser("record")
    recorder.add_argument("digest")
    args = parser.parse_args()
    if args.command == "plan":
        plan(args.force, args.expected_digest)
    else:
        record(args.digest)


if __name__ == "__main__":
    main()
