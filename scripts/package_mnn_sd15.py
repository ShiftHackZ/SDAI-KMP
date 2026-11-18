#!/usr/bin/env python3
"""Package the pinned, preconverted SD 1.5 graphs with their matching tokenizer.

This maintainer operation prepares an archive; it does not establish application
compatibility. No weights are converted or published by this script.
"""

import argparse
import hashlib
import importlib.util
import json
import shutil
import struct
import urllib.request
import zipfile
from pathlib import Path

RUNTIME_REVISION = "7e3c6fb5225d3ccf22807ee641332669df34650d"
PREBUILT_REPOSITORY = "taobao-mnn/stable-diffusion-v1-5-mnn-general"
PREBUILT_REVISION = "56c93a62ac9d6b5d18605632b85e25f87281f317"
SOURCE_REPOSITORY = "stable-diffusion-v1-5/stable-diffusion-v1-5"
SOURCE_REVISION = "451f4fe16113bff5a5d2269ed5ad43b0592e9a14"
PROFILE = "sd15-512-general-int8-v1"
GRAPH_NAMES = ("text_encoder", "unet", "vae_decoder")


def sha256(path):
    """Hash large files without keeping model weights in memory."""
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def download(repository, revision, name, destination):
    """Resolve only immutable repository files and atomically finish each copy."""
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.is_file() and destination.stat().st_size:
        return
    url = f"https://huggingface.co/{repository}/resolve/{revision}/{name}"
    partial = destination.with_name(destination.name + ".partial")
    try:
        with urllib.request.urlopen(url, timeout=120) as response, partial.open("wb") as output:
            shutil.copyfileobj(response, output)
        if not partial.stat().st_size:
            raise ValueError(f"Empty download: {name}")
        partial.replace(destination)
    finally:
        partial.unlink(missing_ok=True)


def input_shapes(path):
    """Read Input ops using the field offsets from the pinned MNN FlatBuffer schema."""
    import flatbuffers.encode
    import flatbuffers.number_types as numbers
    from flatbuffers.table import Table

    data = path.read_bytes()
    root = struct.unpack_from("<I", data)[0]
    net = Table(data, root)
    ops_offset = net.Offset(10)  # Net.oplists, slot 3
    if not ops_offset:
        raise ValueError(f"No graph operations: {path.name}")
    shapes = {}
    for index in range(net.VectorLen(ops_offset)):
        op = Table(data, net.Indirect(net.Vector(ops_offset) + index * 4))
        type_offset = op.Offset(14)  # Op.type, slot 5
        if not type_offset or op.Get(numbers.Int32Flags, op.Pos + type_offset) != 34:
            continue
        name = op.String(op.Pos + op.Offset(10)).decode()  # Op.name
        main = Table(data, op.Indirect(op.Pos + op.Offset(8)))  # Op.main union
        dims_offset = main.Offset(4)
        shapes[name] = [
            main.Get(numbers.Int32Flags, main.Vector(dims_offset) + i * 4)
            for i in range(main.VectorLen(dims_offset))
        ]
    return shapes


def prepare(args):
    """Verify tokenizer identity/signatures, then produce a complete versioned archive."""
    from jsonschema import validate

    output = args.work.resolve()
    bundle = output / "bundle"
    bundle.mkdir(parents=True, exist_ok=True)
    source = args.mnn_source.resolve()
    if not (source / "transformers/diffusion/export/convert_mnn.py").is_file():
        raise ValueError("--mnn-source must contain the pinned MNN source tree")
    expected_exporter = "32a2f3f98b3895a8daf5e41dcc5310f74ceae94ab727a7a55db3c3aa4100b8cc"
    exporter = source / "transformers/diffusion/export/convert_mnn.py"
    if sha256(exporter) != expected_exporter:
        raise ValueError("Pinned tokenizer exporter checksum mismatch")
    if sha256(source / "transformers/llm/export/utils/tokenizer.py") != "bca79de3ed245c2c5aa587d8bd737735641c423043db67a2e1b24d49f16d8268":
        raise ValueError("Pinned binary tokenizer writer checksum mismatch")
    for name in GRAPH_NAMES:
        for extension in (".mnn", ".mnn.weight"):
            filename = name + extension
            download(PREBUILT_REPOSITORY, PREBUILT_REVISION, filename, bundle / filename)
    signatures = {name: input_shapes(bundle / (name + ".mnn")) for name in GRAPH_NAMES}
    expected = {
        "text_encoder": {"input_ids": [1, -1]},
        "unet": {"sample": [1, -1, -1, -1], "timestep": [1], "encoder_hidden_states": [1, -1, 768]},
        "vae_decoder": {"latent_sample": [1, -1, -1, -1]},
    }
    if signatures != expected:
        raise ValueError(f"Unsupported graph signatures: {signatures}")
    tokenizer = output / "tokenizer"
    for name in ("vocab.json", "merges.txt", "tokenizer_config.json", "special_tokens_map.json"):
        download(SOURCE_REPOSITORY, SOURCE_REVISION, "tokenizer/" + name, tokenizer / name)
    # A vocabulary filename is not identity evidence: compare the actual prebuilt assets.
    for name in ("vocab.json", "merges.txt"):
        target = output / ("prebuilt-" + name)
        download(PREBUILT_REPOSITORY, PREBUILT_REVISION, name, target)
        if name.endswith("json"):
            matches = json.loads(target.read_text()) == json.loads((tokenizer / name).read_text())
        else:
            matches = target.read_text().splitlines() == (tokenizer / name).read_text().splitlines()
        if not matches:
            raise ValueError(f"Tokenizer identity mismatch: {name}")
    spec = importlib.util.spec_from_file_location("mnn_tokenizer_export", exporter)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    tokenizer_output = output / "exported-tokenizer"
    module.export_diffusion_mtok(str(tokenizer), str(tokenizer_output))
    shutil.copyfile(tokenizer_output / "tokenizer.mtok", bundle / "tokenizer.mtok")
    license_path = bundle / "LICENSE"
    if not license_path.exists():
        url = "https://raw.githubusercontent.com/CompVis/stable-diffusion/21f890f9da3cfbeaba8e2ac3c425ee9e998d5229/LICENSE"
        with urllib.request.urlopen(url, timeout=60) as response:
            license_path.write_bytes(response.read())
    if "CreativeML Open RAIL-M" not in license_path.read_text():
        raise ValueError("Expected CreativeML model license")
    (bundle / "NOTICE.md").write_text(
        "# Stable Diffusion 1.5 MNN bundle\n\n"
        f"Preconverted graphs/weights: https://huggingface.co/{PREBUILT_REPOSITORY}/tree/{PREBUILT_REVISION}\n\n"
        f"Model/tokenizer lineage: https://huggingface.co/{SOURCE_REPOSITORY}/tree/{SOURCE_REVISION}\n\n"
        "Model use is subject to CreativeML OpenRAIL-M (LICENSE), including its use restrictions. "
        "The upstream MNN runtime is separately licensed under Apache-2.0. "
        "Eight-bit weight quantization does not imply INT8 activation execution.\n"
    )
    manifest = {
        "schemaVersion": 1, "profile": PROFILE,
        "prebuiltRepository": PREBUILT_REPOSITORY, "prebuiltRevision": PREBUILT_REVISION,
        "sourceRepository": SOURCE_REPOSITORY, "sourceRevision": SOURCE_REVISION,
        "runtimeVersion": "3.6.2", "runtimeRevision": RUNTIME_REVISION,
        "tokenizerSourceRevision": SOURCE_REVISION, "tokenizerToolRevision": RUNTIME_REVISION,
        "packagingLockSha256": sha256(Path(__file__).parent / "mnn/requirements.lock"),
        "width": 512, "height": 512, "guidance": 7.5, "scheduler": "PNDM",
        "files": [{"path": path.name, "size": path.stat().st_size, "sha256": sha256(path)}
                  for path in sorted(bundle.iterdir()) if path.name != "manifest.json"],
    }
    validate(manifest, json.loads((Path(__file__).parent / "mnn/bundle.schema.json").read_text()))
    (bundle / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n")
    archive = output / (PROFILE + ".zip")
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_STORED, allowZip64=True) as zip_output:
        for path in sorted(bundle.iterdir()):
            info = zipfile.ZipInfo(path.name, (1980, 1, 1, 0, 0, 0))
            info.external_attr = 0o100644 << 16
            with path.open("rb") as source_file, zip_output.open(info, "w") as target_file:
                shutil.copyfileobj(source_file, target_file)
    print(json.dumps({"archive": str(archive), "size": archive.stat().st_size,
                      "sha256": sha256(archive), "signatures": signatures}, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--work", required=True, type=Path)
    parser.add_argument("--mnn-source", required=True, type=Path)
    prepare(parser.parse_args())
