#!/usr/bin/env python3
"""Compile and run the production UNet adapter and patched allocator unit suites.

Requires a C++17 compiler and patch. Linux also runs the mmap fault-injection
suite against real file mappings; no Android inference or GPU is exercised.
Only the 1.3 MB pinned graph metadata fixture is needed, not model weights.
"""

import argparse
import hashlib
import platform
import shutil
import subprocess
import tarfile
import urllib.request
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REVISION = "7e3c6fb5225d3ccf22807ee641332669df34650d"
SOURCE_HASH = "01dcdce39072b70188079a1dca0600e782c8dd0fcf2ad77f610bab264166a0cf"
GRAPH_HASH = "3895d9101b16f99660010c87cf1413407103a237636d6a112de11a674a960d4c"


def fetch(url, path, digest):
    """Reject changed source/fixture bytes before compiling or running tests."""
    if not path.exists():
        with urllib.request.urlopen(url, timeout=120) as response:
            path.write_bytes(response.read())
    if hashlib.sha256(path.read_bytes()).hexdigest() != digest:
        raise ValueError(f"Pinned test input checksum mismatch: {path.name}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--work", type=Path, required=True)
    args = parser.parse_args()
    work = args.work.resolve()
    work.mkdir(parents=True, exist_ok=True)
    archive = work / "mnn.tar.gz"
    fetch(f"https://github.com/alibaba/MNN/archive/{REVISION}.tar.gz", archive, SOURCE_HASH)
    source = work / f"MNN-{REVISION}"
    if not source.exists():
        with tarfile.open(archive) as tar:
            tar.extractall(work, filter="data")
    adapted = work / "adapted"
    if adapted.exists():
        shutil.rmtree(adapted)
    shutil.copytree(source, adapted)
    native = ROOT / "feature/mnn/src/androidMain/cpp"
    subprocess.run(["patch", "-p1", "-i", str(native / "patches/sdai-sd15.patch")], cwd=adapted, check=True)
    graph = work / "unet.mnn"
    fetch("https://huggingface.co/taobao-mnn/stable-diffusion-v1-5-mnn-general/resolve/56c93a62ac9d6b5d18605632b85e25f87281f317/unet.mnn", graph, GRAPH_HASH)
    tests = ROOT / "feature/mnn/src/nativeTest/cpp"
    compiler = ["c++", "-std=c++17", "-O1", "-g", "-fexceptions", "-ffunction-sections", "-fdata-sections"]
    for folder in ("include", "source", "schema/current", "3rd_party/flatbuffers/include"):
        compiler.extend(["-I", str(adapted / folder)])
    graph_binary = work / "graph_test"
    subprocess.run(compiler + ["-I", str(native), str(tests / "graph_test.cpp"), str(native / "sdai_unet_graph.cpp"), "-o", str(graph_binary)], check=True)
    subprocess.run([str(graph_binary), str(graph)], check=True)
    if platform.system() == "Linux":
        allocator_binary = work / "allocator_test"
        sources = [tests / "allocator_test.cpp"] + [adapted / "source/core" / name for name in ("BufferAllocator.cpp", "MNNMemoryUtils.cpp", "MNNFileUtils.cpp")]
        subprocess.run(compiler + ["-DSDAI_MNN_PRIVATE_MMAP=1"] + list(map(str, sources)) + ["-Wl,--gc-sections", "-Wl,--wrap=posix_fallocate", "-Wl,--wrap=ftruncate", "-Wl,--wrap=mmap", "-o", str(allocator_binary)], check=True)
        subprocess.run([str(allocator_binary)], check=True)
    else:
        print("Allocator syscall suite requires Linux; run the Linux CI job for this boundary.")


if __name__ == "__main__":
    main()
