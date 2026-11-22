#!/usr/bin/env python3
"""Archive native diagnostics only when their ELF build IDs match the built package."""

import argparse
import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import sys
import tomllib
import zipfile

FIRST_PARTY = {"libsdai_sdcpp.so", "libsdai_benchmark.so", "libsdai_bonsai.so"}
LIBRARY = re.compile(r"(?:base/)?lib/([^/]+)/([^/]+\.so)$")
SYMBOL = re.compile(r"(?:BUNDLE-METADATA/com.android.tools.build.debugsymbols/)?([^/]+)/([^/]+\.so)(?:\.dbg|\.sym)?$")
MAX_ELF_BYTES = 512 * 1024 * 1024
HOST_PATH = re.compile(rb"/(?:Users|home|private/tmp|tmp|var/tmp)/")


def require(is_valid, message):
    if not is_valid:
        raise ValueError(message)


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def file_sha256(path):
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def elf_identity(data):
    """Read Android ELF sections directly, without invoking a platform-specific tool."""
    require(len(data) >= 64 and data[:4] == b"\x7fELF" and data[5] == 1, "Expected a little-endian ELF file")
    is_64_bit = data[4] == 2
    require(is_64_bit or data[4] == 1, "Unsupported ELF class")
    header_format = "<HHIQQQIHHHHHH" if is_64_bit else "<HHIIIIIHHHHHH"
    header = struct.unpack_from(header_format, data, 16)
    section_offset, section_size, section_count, string_index = header[5], header[10], header[11], header[12]
    section_format = "<IIQQQQIIQQ" if is_64_bit else "<IIIIIIIIII"
    require(0 < section_count < 65535 and string_index < section_count, "Missing ELF section inventory")
    require(section_size >= struct.calcsize(section_format), "Invalid ELF section size")
    require(section_offset + section_size * section_count <= len(data), "ELF sections exceed file")
    sections = [struct.unpack_from(section_format, data, section_offset + index * section_size)
                for index in range(section_count)]
    strings = sections[string_index]
    names = data[strings[4]:strings[4] + strings[5]]
    section_names = set()
    build_ids = set()
    for section in sections:
        name_index, section_type, offset, size = section[0], section[1], section[4], section[5]
        require(name_index < len(names), "Invalid ELF section name")
        name = names[name_index:].split(b"\0", 1)[0].decode("ascii", errors="replace")
        if size:
            section_names.add(name)
        if section_type != 7:
            continue
        require(offset + size <= len(data), "ELF note exceeds file")
        position, end = offset, offset + size
        while position + 12 <= end:
            name_size, description_size, note_type = struct.unpack_from("<III", data, position)
            position += 12
            note_name = data[position:position + name_size]
            position += (name_size + 3) & ~3
            description = data[position:position + description_size]
            position += (description_size + 3) & ~3
            require(position <= end, "Invalid ELF note length")
            if note_type == 3 and note_name.rstrip(b"\0") == b"GNU":
                build_ids.add(description.hex())
    require(len(build_ids) <= 1, "ELF has conflicting GNU build IDs")
    quality = "full" if ".debug_line" in section_names else "symbols" if ".symtab" in section_names else "stripped"
    return {"build_id": build_ids.pop() if build_ids else None, "quality": quality, "machine": header[1], "bits": 64 if is_64_bit else 32}


def read_entry(archive, entry):
    require(entry.file_size <= MAX_ELF_BYTES, "Native archive entry exceeds limit")
    return archive.read(entry)


def package_libraries(path):
    libraries = {}
    with zipfile.ZipFile(path) as archive:
        require(len(archive.namelist()) == len(set(archive.namelist())), "Duplicate package ZIP entries")
        for entry in archive.infolist():
            match = LIBRARY.fullmatch(entry.filename)
            if match:
                key = match.groups()
                require(key not in libraries, "Duplicate packaged native library")
                data = read_entry(archive, entry)
                libraries[key] = {**elf_identity(data), "sha256": sha256(data)}
    require(libraries, "Package has no native libraries")
    return libraries


def verify_source_revision(package, source_sha):
    """Reject symbols built beside an artifact from a different checkout."""
    with zipfile.ZipFile(package) as archive:
        entries = [entry for entry in archive.infolist()
                   if entry.filename.endswith("/version-control-info.textproto")]
        require(entries, "Package has no embedded source revision")
        for entry in entries:
            require(entry.file_size < 65536, "Embedded source metadata exceeds limit")
            revisions = re.findall(r'\brevision\s*:\s*"([^"]+)"', archive.read(entry).decode())
            require(revisions and all(revision == source_sha for revision in revisions),
                    "Package source revision does not match symbols checkout")


def collect(args):
    source = args.source.resolve()
    source_sha = subprocess.check_output(["git", "-C", str(source), "rev-parse", "HEAD"], text=True).strip()
    require(source_sha == args.source_sha, "Source SHA changed")
    verify_source_revision(args.package, source_sha)
    versions = tomllib.loads((source / "gradle/libs.versions.toml").read_text())["versions"]
    libraries = package_libraries(args.package)
    chosen = {}
    unverifiable = set()
    rank = {"stripped": 0, "symbols": 1, "full": 2}

    def consider(key, data, origin, must_match=False):
        if key not in libraries:
            # AGP symbol archives may include dependency ABIs excluded from the package.
            return
        identity = elf_identity(data)
        has_matching_abi = all(identity[field] == libraries[key][field] for field in ("machine", "bits"))
        require(has_matching_abi or not must_match, "Native symbol ABI does not match package: " + "/".join(key))
        if not has_matching_abi:
            return
        if identity["build_id"] is None or libraries[key]["build_id"] is None:
            # Separate debug files cannot be authenticated by comparing their bytes to a stripped library.
            unverifiable.add(key)
            return
        matches = identity["build_id"] == libraries[key]["build_id"]
        require(matches or not must_match, "Native symbols do not match package build ID: " + "/".join(key))
        if not matches:
            return
        previous = chosen.get(key)
        if identity["quality"] != "stripped" and (previous is None or rank[identity["quality"]] > rank[previous[0]["quality"]]):
            chosen[key] = identity, data, origin

    with zipfile.ZipFile(args.package) as archive:
        for entry in archive.infolist():
            if entry.filename.startswith("BUNDLE-METADATA/com.android.tools.build.debugsymbols/"):
                match = SYMBOL.fullmatch(entry.filename)
                if match:
                    consider(match.groups(), read_entry(archive, entry), "bundle-metadata", True)

    symbol_zip = source / f"app/android/build/outputs/native-debug-symbols/{args.flavor}Release/native-debug-symbols.zip"
    if symbol_zip.is_file():
        with zipfile.ZipFile(symbol_zip) as archive:
            require(len(archive.namelist()) == len(set(archive.namelist())), "Duplicate symbol ZIP entries")
            for entry in archive.infolist():
                match = SYMBOL.fullmatch(entry.filename)
                if match:
                    consider(match.groups(), read_entry(archive, entry), "agp-symbol-archive", True)

    # Search only first-party native outputs; stale builds are accepted only by exact ABI/build ID.
    for module in ("sdxl", "benchmark", "bonsai"):
        for native_root in (source / "feature" / module / "build/intermediates/cxx", source / "feature" / module / ".cxx"):
            if not native_root.is_dir():
                continue
            for candidate in sorted(native_root.rglob("*.so")):
                if candidate.name not in FIRST_PARTY or candidate.parent.name not in {key[0] for key in libraries}:
                    continue
                require(candidate.stat().st_size <= MAX_ELF_BYTES, "Native output exceeds limit")
                consider((candidate.parent.name, candidate.name), candidate.read_bytes(), "first-party-native-output")

    entries = {}
    manifest_libraries = []
    for key, packaged in sorted(libraries.items()):
        abi, name = key
        selected = chosen.get(key)
        has_host_paths = selected is not None and HOST_PATH.search(selected[1]) is not None
        require(name not in FIRST_PARTY or not has_host_paths,
                "First-party debug data contains host paths; fix compiler prefix maps")
        if has_host_paths:
            selected = None
        quality = selected[0]["quality"] if selected else "stripped"
        require(name not in FIRST_PARTY or (quality == "full" and packaged["build_id"]), "First-party line symbols are missing: " + "/".join(key))
        has_unverifiable_symbols = selected is None and key in unverifiable
        limitation = None if quality == "full" else "Source lines unavailable; obtain matching symbols from dependency maintainer."
        if selected:
            origin = selected[2]
        elif has_host_paths:
            origin = "dependency-paths-excluded"
            limitation = "Debug data excluded because it contains host paths."
        elif has_unverifiable_symbols:
            origin = "dependency-build-id-missing"
            limitation = "Debug data excluded because a GNU build ID is missing."
        else:
            origin = "dependency-prestripped"
        record = {"abi": abi, "name": name, **packaged, "debug_quality": quality,
                  "origin": origin, "limitation": limitation}
        if selected:
            archive_path = f"native/{abi}/{packaged['build_id']}/{name}.dbg"
            entries[archive_path] = selected[1]
            record["symbols"] = archive_path
            record["symbols_sha256"] = sha256(selected[1])
        manifest_libraries.append(record)

    submodule_output = subprocess.check_output(["git", "-C", str(source), "submodule", "status", "--recursive"], text=True)
    submodules = []
    for line in submodule_output.splitlines():
        require(line.startswith(" "), "Submodule does not match source revision")
        revision, path, *_ = line.strip().split()
        submodules.append({"path": path, "sha": revision})
    mapping = source / f"app/android/build/outputs/mapping/{args.flavor}Release/mapping.txt"
    if mapping.is_file():
        entries["mapping.txt"] = mapping.read_bytes()
    manifest = {"schema_version": 1, "source_sha": source_sha, "flavor": args.flavor,
                "version_name": str(versions["versionName"]), "version_code": int(versions["versionCode"]),
                "format": args.package.suffix.lstrip("."), "unsigned_package_sha256": file_sha256(args.package),
                "toolchain": json.loads(args.toolchain.read_text()), "runtime_versions": versions,
                "submodules": submodules, "libraries": manifest_libraries,
                "mapping_sha256": sha256(entries["mapping.txt"]) if "mapping.txt" in entries else None}
    entries["native-diagnostics.json"] = (json.dumps(manifest, indent=2, sort_keys=True) + "\n").encode()
    require(not args.output.exists(), "Diagnostics output already exists")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(args.output, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for name, data in sorted(entries.items()):
            entry = zipfile.ZipInfo(name, (1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            entry.external_attr = 0o100644 << 16
            archive.writestr(entry, data)
    print(f"Preserved {len(libraries)} ABI/build-ID identities in {args.output.name}")


def verify_archive(symbols, package, metadata):
    """Bind a diagnostics archive to checked release metadata and packaged ELF bytes."""
    with zipfile.ZipFile(symbols) as archive:
        names = archive.namelist()
        require(len(names) == len(set(names)), "Duplicate diagnostics entries")
        manifest = json.loads(archive.read("native-diagnostics.json"))
        require(manifest["schema_version"] == 1, "Unsupported diagnostics schema")
        for field in ("source_sha", "version_name", "version_code", "flavor", "format"):
            require(manifest[field] == metadata[field], "Diagnostics release identity mismatch: " + field)
        require(manifest["unsigned_package_sha256"] == metadata["sha256"], "Diagnostics unsigned checksum mismatch")
        libraries = package_libraries(package)
        expected = {(row["abi"], row["name"]): row for row in manifest["libraries"]}
        require(len(expected) == len(manifest["libraries"]) and expected.keys() == libraries.keys(), "Diagnostics library inventory mismatch")
        allowed = {"native-diagnostics.json"}
        for key, identity in libraries.items():
            row = expected[key]
            require(all(row[field] == identity[field] for field in ("build_id", "machine", "bits", "sha256")), "Diagnostics ELF mismatch")
            if "symbols" in row:
                name = row["symbols"]
                require(name == f"native/{key[0]}/{identity['build_id']}/{key[1]}.dbg", "Invalid diagnostics symbol path")
                data = read_entry(archive, archive.getinfo(name))
                require(sha256(data) == row["symbols_sha256"], "Diagnostics symbol checksum mismatch")
                elf = elf_identity(data)
                require(all(elf[field] == identity[field] for field in ("build_id", "machine", "bits")), "Diagnostics symbol build ID mismatch")
                require(elf["quality"] == row["debug_quality"], "Diagnostics symbol quality mismatch")
                require(key[1] not in FIRST_PARTY or elf["quality"] == "full", "First-party line symbols are missing")
                allowed.add(name)
            else:
                require(key[1] not in FIRST_PARTY, "First-party symbols are missing")
        if manifest["mapping_sha256"] is not None:
            require(sha256(archive.read("mapping.txt")) == manifest["mapping_sha256"], "R8 mapping checksum mismatch")
            allowed.add("mapping.txt")
        require(set(names) == allowed, "Unexpected diagnostics content")
    return manifest


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("package", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--flavor", choices=("foss", "full", "playstore"), required=True)
    parser.add_argument("--source-sha", required=True)
    parser.add_argument("--toolchain", type=Path, required=True)
    args = parser.parse_args()
    require(re.fullmatch(r"[0-9a-f]{40}", args.source_sha), "Source SHA must be a complete commit ID")
    collect(args)


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, KeyError, struct.error, zipfile.BadZipFile, subprocess.CalledProcessError) as error:
        sys.exit(str(error))
