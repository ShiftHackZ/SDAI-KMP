#!/usr/bin/env python3
"""Build pinned, unsigned MNN static Apple slices before SwiftPM resolution.

Sources and all build products remain ignored. No model weights are fetched.
One build directory is reused sequentially to bound disk consumption.
"""
import argparse
import hashlib
import json
import os
import plistlib
import shutil
import subprocess
import tarfile
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REVISION = "7e3c6fb5225d3ccf22807ee641332669df34650d"
SOURCE_HASH = "01dcdce39072b70188079a1dca0600e782c8dd0fcf2ad77f610bab264166a0cf"
BRIDGE = ROOT / "feature/mnn/swift/Sources/CMnnBridge"
ARTIFACTS = ROOT / "feature/mnn/swift/Artifacts"


def run(*args, **kwargs):
    subprocess.run(list(map(str, args)), check=True,
                   env={**os.environ, "ZERO_AR_DATE": "1", "SOURCE_DATE_EPOCH": "0", "LC_ALL": "C"},
                   **kwargs)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--work", type=Path, default=ROOT / "tmp/mnn/apple")
    parser.add_argument("--artifacts", type=Path, default=ARTIFACTS)
    parser.add_argument("--slices", nargs="+", choices=["iphoneos", "iphonesimulator", "macosx"],
                        default=["iphoneos", "iphonesimulator", "macosx"])
    args = parser.parse_args()
    artifacts = args.artifacts.resolve()
    work = args.work.resolve()
    work.mkdir(parents=True, exist_ok=True)
    archive = work / "mnn.tar.gz"
    if not archive.exists():
        cached = ROOT / "tmp/mnn/source.tar.gz"
        if cached.exists():
            shutil.copyfile(cached, archive)
        else:
            with urllib.request.urlopen(f"https://github.com/alibaba/MNN/archive/{REVISION}.tar.gz", timeout=120) as source:
                with archive.open("wb") as output:
                    shutil.copyfileobj(source, output)
    if hashlib.sha256(archive.read_bytes()).hexdigest() != SOURCE_HASH:
        raise ValueError("Pinned MNN source checksum mismatch")
    source = work / f"MNN-{REVISION}"
    patch = BRIDGE / "Runtime/patches/sdai-sd15.patch"
    patch_hash = hashlib.sha256(patch.read_bytes()).hexdigest()
    marker = source / ".sdai-patch"
    if not marker.exists() or marker.read_text() != patch_hash:
        if source.exists():
            shutil.rmtree(source)
        with tarfile.open(archive) as tar:
            tar.extractall(work, filter="data")
        run("patch", "-p1", "-i", patch, cwd=source)
        marker.write_text(patch_hash)
    artifacts.mkdir(parents=True, exist_ok=True)
    frameworks = []
    for sdk in args.slices:
        build = work / f"build-{sdk}"
        sdk_path = subprocess.check_output(["xcrun", "--sdk", sdk, "--show-sdk-path"], text=True).strip()
        system = "Darwin" if sdk == "macosx" else "iOS"
        minimum = "14.0" if sdk == "macosx" else "17.0"
        run("cmake", "-S", BRIDGE, "-B", build, "-G", "Ninja",
            f"-DMNN_SOURCE={source}", f"-DCMAKE_SYSTEM_NAME={system}",
            "-DCMAKE_OSX_ARCHITECTURES=arm64", f"-DCMAKE_OSX_SYSROOT={sdk_path}",
            f"-DCMAKE_OSX_DEPLOYMENT_TARGET={minimum}", "-DCMAKE_BUILD_TYPE=Release",
            "-DCMAKE_C_FLAGS_RELEASE=-O2 -DNDEBUG", "-DCMAKE_CXX_FLAGS_RELEASE=-O2 -DNDEBUG",
            "-DCMAKE_OBJCXX_FLAGS_RELEASE=-O2 -DNDEBUG")
        run("cmake", "--build", build, "--target", "MNN", "-j", min(6, os.cpu_count() or 2))
        libraries = list(build.rglob("libMNN.a"))
        if len(libraries) != 1:
            raise ValueError("Expected exactly one MNN static library")
        framework = artifacts / sdk / "MnnNative.framework"
        if framework.exists():
            shutil.rmtree(framework)
        (framework / "Headers").mkdir(parents=True)
        (framework / "Modules").mkdir()
        shutil.copyfile(libraries[0], framework / "MnnNative")
        run("xcrun", "strip", "-S", framework / "MnnNative")
        shutil.copyfile(BRIDGE / "include/MnnBridge.h", framework / "Headers/MnnBridge.h")
        (framework / "Modules/module.modulemap").write_text(
            'framework module MnnNative { umbrella header "MnnBridge.h" export * }\n')
        with (framework / "Info.plist").open("wb") as output:
            plistlib.dump({"CFBundleIdentifier": "cc.moroz.sdai.MnnNative",
                          "CFBundleName": "MnnNative", "CFBundleExecutable": "MnnNative",
                          "CFBundlePackageType": "FMWK", "CFBundleVersion": "1",
                          "CFBundleShortVersionString": "3.6.2",
                          "MinimumOSVersion": minimum}, output, sort_keys=True)
        frameworks.append(framework)
    xcframework = artifacts / "MnnNative.xcframework"
    if xcframework.exists():
        shutil.rmtree(xcframework)
    command = ["xcodebuild", "-create-xcframework"]
    for framework in frameworks:
        command += ["-framework", str(framework)]
    run(*command, "-output", xcframework)
    (artifacts / "inputs.json").write_text(json.dumps({
        "revision": REVISION, "sourceSha256": SOURCE_HASH, "patchSha256": patch_hash,
        "xcode": subprocess.check_output(["xcodebuild", "-version"], text=True).strip(),
        "slices": args.slices}, indent=2) + "\n")


if __name__ == "__main__":
    main()
