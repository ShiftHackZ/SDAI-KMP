#!/usr/bin/env python3
"""Attach checked diagnostic archives to an existing matching release, never create one."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
from urllib.parse import quote
import zipfile

from native_symbols import require, verify_archive


def gh_json(arguments):
    result = subprocess.run(["gh", *arguments], check=True, capture_output=True, text=True, timeout=120)
    return json.loads(result.stdout)


def verify_tag(repository, tag, source_sha):
    reference = gh_json(["api", f"repos/{repository}/git/ref/tags/{quote(tag, safe='')}"])["object"]
    for _ in range(5):
        if reference["type"] == "commit":
            require(reference["sha"] == source_sha, "Release tag does not match diagnostics source SHA")
            return
        require(reference["type"] == "tag", "Release tag does not resolve to a commit")
        reference = gh_json(["api", f"repos/{repository}/git/tags/{reference['sha']}"])["object"]
    raise ValueError("Release tag nesting is too deep")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("artifacts", type=Path, nargs="?")
    parser.add_argument("--tag", required=True)
    parser.add_argument("--source-sha", required=True)
    parser.add_argument("--check-only", dest="should_check_only", action="store_true", help="Validate without uploading assets")
    parser.add_argument("--check-release-only", dest="should_check_release_only", action="store_true", help="Validate the existing release before building")
    parser.add_argument("--version", help="Expected version for release preflight")
    args = parser.parse_args()
    repository = os.environ["GITHUB_REPOSITORY"]
    require(re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository), "Invalid repository")
    require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._/-]{0,99}", args.tag) and args.tag.lower() != "nightly", "Use a version release tag")
    require(re.fullmatch(r"[0-9a-f]{40}", args.source_sha), "Invalid source SHA")
    release = gh_json(["release", "view", args.tag, "--repo", repository, "--json", "tagName,assets"])
    require(release["tagName"] == args.tag, "Existing release tag mismatch")
    verify_tag(repository, args.tag, args.source_sha)
    if args.should_check_release_only:
        require(args.version is not None and args.tag in (args.version, "v" + args.version),
                "Use the matching version release tag")
        print("Existing version release and immutable source tag verified")
        return
    require(args.artifacts is not None, "Signed artifact directory required")
    existing = {asset["name"] for asset in release["assets"]}
    uploads = []
    for metadata_path in sorted(args.artifacts.glob("*/metadata.json")):
        metadata = json.loads(metadata_path.read_text())
        require(metadata["source_sha"] == args.source_sha, "Mixed source revisions in diagnostics")
        flavor, format_, version = (metadata[field] for field in ("flavor", "format", "version_name"))
        require(flavor in ("foss", "full", "playstore") and format_ in ("apk", "aab"), "Invalid artifact variant")
        require(re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._+-]{0,99}", version), "Invalid version")
        require(args.tag in (version, "v" + version), "Use the matching version release tag")
        directory = metadata_path.parent
        package = directory / f"sdai-{flavor}-release-{version}.{format_}"
        diagnostics = directory / f"sdai-{flavor}-{format_}-{version}-native-diagnostics.zip"
        verify_archive(diagnostics, package, metadata)
        # Bind this symbols archive to the signed package too; ELF bytes are checked above.
        binding = directory / (diagnostics.name + ".binding.json")
        require(not binding.exists(), "Diagnostics binding already exists")
        if not args.should_check_only:
            binding.write_text(json.dumps({"source_sha": args.source_sha, "release_tag": args.tag,
                          "package": package.name, "package_sha256": hashlib.sha256(package.read_bytes()).hexdigest(),
                          "diagnostics": diagnostics.name, "diagnostics_sha256": hashlib.sha256(diagnostics.read_bytes()).hexdigest()},
                             indent=2, sort_keys=True) + "\n")
        uploads.extend((diagnostics, binding))
    require(uploads, "No signed release diagnostics found")
    require(len({path.name for path in uploads}) == len(uploads), "Duplicate diagnostic assets")
    require(not any(path.name in existing for path in uploads), "Diagnostic asset already exists; refusing to overwrite")
    if args.should_check_only:
        print(f"Validated {len(uploads) // 2} matching diagnostics archives; no upload performed")
        return
    # Recheck immediately before the write. No tag/release creation or replacement is performed.
    verify_tag(repository, args.tag, args.source_sha)
    subprocess.run(["gh", "release", "upload", args.tag, "--repo", repository, *map(str, uploads)],
                   check=True, timeout=600)


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, KeyError, zipfile.BadZipFile, subprocess.CalledProcessError, subprocess.TimeoutExpired) as error:
        sys.exit(str(error))
