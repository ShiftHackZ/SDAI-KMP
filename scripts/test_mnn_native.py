#!/usr/bin/env python3
"""Run production graph, tokenizer, PNDM and patched allocator component suites.

Requires a C++17 compiler and patch. Linux also runs the mmap fault-injection
suite against real file mappings; no Android inference or GPU is exercised.
Only pinned graph metadata and tokenizer fixtures are fetched, not model weights.
"""

import argparse
import hashlib
import io
import json
import os
import zipfile
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



class PinnedArchiveReader(io.RawIOBase):
    """Fetch only bounded ZIP ranges; never fall back to downloading model weights."""
    size = 1_154_537_931
    url = "https://huggingface.co/moroz-ellmsha/sdai-mnn-model-archives/resolve/5c242a99361e6ee55371e5d8dd196c8b4840b01d/sd15-512-general-int8-v1.zip?download=true"

    def __init__(self):
        self.position = 0

    def seek(self, offset, whence=0):
        self.position = offset + (self.position if whence == 1 else self.size if whence == 2 else 0)
        return self.position

    def tell(self):
        return self.position

    def read(self, count=-1):
        count = self.size - self.position if count < 0 else count
        if count == 0:
            return b""
        if count > 2_000_000 or self.position < 0:
            raise ValueError("Tokenizer fixture range exceeds bound")
        start, end = self.position, min(self.position + count, self.size) - 1
        request = urllib.request.Request(self.url, headers={"Range": f"bytes={start}-{end}"})
        with urllib.request.urlopen(request, timeout=120) as response:
            if response.status != 206 or response.headers.get("Content-Range") != f"bytes {start}-{end}/{self.size}":
                raise ValueError("Pinned tokenizer fixture range unavailable")
            data = response.read(count + 1)
        if len(data) != end - start + 1:
            raise ValueError("Pinned tokenizer fixture range length changed")
        self.position += len(data)
        return data


def tokenizer_fixture(work):
    folder = work / "tokenizer-fixture"
    folder.mkdir(exist_ok=True)
    target = folder / "tokenizer.mtok"
    if not target.exists():
        with zipfile.ZipFile(PinnedArchiveReader()) as archive:
            member = archive.getinfo("tokenizer.mtok")
            if member.file_size != 1_322_512:
                raise ValueError("Pinned tokenizer fixture length changed")
            target.write_bytes(archive.read(member))
    if hashlib.sha256(target.read_bytes()).hexdigest() != "21cc1d6f9dca099fb184a899b08e898121b6e18e9f38757ee9feede49d8c038a":
        raise ValueError("Pinned tokenizer fixture checksum changed")
    return folder

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--work", type=Path, required=True)
    parser.add_argument("--metal-library", type=Path,
                        help="Run actual Metal operator parity using a bootstrapped macOS libMNN archive")
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
    native = ROOT / "feature/mnn/swift/Sources/CMnnBridge/Runtime"
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
    pndm_binary = work / "pndm_test"
    subprocess.run(compiler + ["-I", str(native), str(tests / "pndm_test.cpp"), "-o", str(pndm_binary)], check=True)
    subprocess.run([str(pndm_binary)], check=True)
    # Reference vectors use the same immutable CLIP vocabulary/configuration as the bundle.
    cases = json.loads((tests.parent / "fixtures/clip-tokenization.json").read_text())
    names = ["empty prompt", "cat prompt", "uppercase prompt", "multiword prompt", "punctuation prompt",
             "sentence prompt", "Cyrillic prompt", "CJK prompt", "accented prompt", "truncated prompt"]
    header = "#include <string>\n#include <vector>\nstruct ClipCase { std::string name, prompt; std::vector<int> ids; };\nconst std::vector<ClipCase> clipCases = {\n"
    for case, name in zip(cases, names, strict=True):
        header += "{" + json.dumps(name) + "," + json.dumps(case["prompt"], ensure_ascii=False) + ",{"
        header += ",".join(map(str, case["ids"])) + "}},\n"
    (work / "clip_cases.hpp").write_text(header + "};\n")
    tokenizer_binary = work / "tokenizer_test"
    tokenizer_source = adapted / "transformers/llm/engine/src/tokenizer"
    tokenizer_sources = [tests / "tokenizer_test.cpp", adapted / "transformers/diffusion/engine/src/tokenizer.cpp",
                         tokenizer_source / "tokenizer.cpp", tokenizer_source / "unicode.cpp",
                         tokenizer_source / "unicode_data.cpp", adapted / "source/core/AutoTime.cpp"]
    subprocess.run(compiler + ["-DMNN_DIFFUSION_WITH_LLM_TOKENIZER=1", "-I", str(work), "-I", str(tests),
                              "-I", str(adapted / "transformers/diffusion/engine/src")]
                   + list(map(str, tokenizer_sources)) + ["-o", str(tokenizer_binary)], check=True)
    subprocess.run([str(tokenizer_binary), str(tokenizer_fixture(work))], check=True)
    if args.metal_library:
        if platform.system() != "Darwin":
            raise ValueError("Metal parity requires Darwin")
        metal_binary = work / "metal_attention_test"
        subprocess.run(compiler + [str(tests / "metal_attention_test.cpp"), str(args.metal_library.resolve()),
                                  "-framework", "Foundation", "-framework", "Metal", "-framework", "CoreGraphics",
                                  "-o", str(metal_binary)], check=True)
        subprocess.run([str(metal_binary)], check=True,
                       env={**os.environ, "MNN_METAL_ATTN_QSPLIT": "2"})
        lifetime_binary = work / "metal_weight_lifetime_test"
        subprocess.run(compiler + ["-fobjc-arc", str(tests / "metal_weight_lifetime_test.mm"),
                                  str(args.metal_library.resolve()), "-framework", "Foundation",
                                  "-framework", "Metal", "-framework", "CoreGraphics",
                                  "-o", str(lifetime_binary)], check=True)
        subprocess.run([str(lifetime_binary)], check=True)
    if platform.system() == "Linux":
        allocator_binary = work / "allocator_test"
        sources = [tests / "allocator_test.cpp"] + [adapted / "source/core" / name for name in ("BufferAllocator.cpp", "MNNMemoryUtils.cpp", "MNNFileUtils.cpp")]
        subprocess.run(compiler + ["-DSDAI_MNN_PRIVATE_MMAP=1"] + list(map(str, sources)) + ["-Wl,--gc-sections", "-Wl,--wrap=posix_fallocate", "-Wl,--wrap=ftruncate", "-Wl,--wrap=mmap", "-o", str(allocator_binary)], check=True)
        subprocess.run([str(allocator_binary)], check=True)
    elif platform.system() == "Darwin":
        reservation_binary = work / "darwin_reservation_test"
        subprocess.run(compiler + ["-I", str(native), str(tests / "darwin_reservation_test.cpp"),
                                  "-o", str(reservation_binary)], check=True)
        subprocess.run([str(reservation_binary)], check=True)
    else:
        print("Allocator syscall suite requires Linux; run the Linux CI job for this boundary.")


if __name__ == "__main__":
    main()
