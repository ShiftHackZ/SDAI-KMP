#!/usr/bin/env python3
"""Reject known native reproducibility hazards in an actual APK or AAB."""

import argparse
from pathlib import Path
import sys
import zipfile

from native_symbols import elf_identity, read_entry, require, sha256


def native_inventory(package, forbidden_paths=()):
    """Discover every ELF by magic bytes, including executables or libraries stored in assets."""
    inventory = {}
    with zipfile.ZipFile(package) as archive:
        require(len(archive.namelist()) == len(set(archive.namelist())), "Duplicate package ZIP entries")
        for entry in archive.infolist():
            if entry.is_dir():
                continue
            with archive.open(entry) as stream:
                is_elf = stream.read(4) == b"\x7fELF"
            require(not entry.filename.endswith(".so") or is_elf,
                    "Packaged .so is not an ELF file: " + entry.filename)
            if not is_elf:
                continue
            data = read_entry(archive, entry)
            inventory[entry.filename] = {**elf_identity(data), "sha256": sha256(data)}
            for path in forbidden_paths:
                require(str(path).encode() not in data,
                        "Packaged native library contains a build path: " + entry.filename)
    require(inventory, "Package has no native ELF files")
    return inventory


def compare_native_packages(first, second, forbidden_paths=()):
    """Compare complete ELF bytes and identities without a module or library allowlist."""
    inventories = [native_inventory(package, forbidden_paths) for package in (first, second)]
    differences = {name: [inventory.get(name) for inventory in inventories]
                   for name in sorted(set(inventories[0]) | set(inventories[1]))
                   if inventories[0].get(name) != inventories[1].get(name)}
    return {"inventories": inventories, "differences": differences}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("first", type=Path)
    parser.add_argument("second", type=Path)
    parser.add_argument("--forbidden-path", action="append", default=[])
    args = parser.parse_args()
    report = compare_native_packages(args.first, args.second, args.forbidden_path)
    require(not report["differences"], "Native ELF differences: " + ", ".join(report["differences"]))
    print(f"All {len(report['inventories'][0])} native ELF files are byte-identical")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, zipfile.BadZipFile) as error:
        sys.exit(str(error))
