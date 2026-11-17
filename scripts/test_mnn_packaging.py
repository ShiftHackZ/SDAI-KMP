#!/usr/bin/env python3
"""Unit coverage for model packaging; external downloads/exporter are isolated.

Run with Python 3.11+ and flatbuffers/jsonschema from mnn/requirements.lock.
These are component checks, not application compatibility or inference tests.
"""

import contextlib
import hashlib
import io
import json
import tempfile
import unittest
import zipfile
from pathlib import Path
from types import SimpleNamespace
from unittest.mock import patch

import flatbuffers
import package_mnn_sd15 as packaging


class PackagingScenarios(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name).resolve()

    def prepare_fixture(self):
        """Provide small deterministic payloads at the packaging IO boundaries."""
        source = self.root / "mnn"
        exporter = source / "transformers/diffusion/export/convert_mnn.py"
        exporter.parent.mkdir(parents=True)
        exporter.write_text("""from pathlib import Path
def export_diffusion_mtok(tokenizer, output):
    Path(output).mkdir(parents=True, exist_ok=True)
    (Path(output) / 'tokenizer.mtok').write_bytes(b'unit tokenizer')
""")
        writer = source / "transformers/llm/export/utils/tokenizer.py"
        writer.parent.mkdir(parents=True)
        writer.write_text("# isolated exporter dependency\n")
        signatures = {
            "text_encoder": {"input_ids": [1, -1]},
            "unet": {"sample": [1, -1, -1, -1], "timestep": [1], "encoder_hidden_states": [1, -1, 768]},
            "vae_decoder": {"latent_sample": [1, -1, -1, -1]},
        }
        real_hash = packaging.sha256

        def checksum(path):
            if path == exporter:
                return "32a2f3f98b3895a8daf5e41dcc5310f74ceae94ab727a7a55db3c3aa4100b8cc"
            if path == writer:
                return "bca79de3ed245c2c5aa587d8bd737735641c423043db67a2e1b24d49f16d8268"
            return real_hash(path)

        def download(repository, revision, name, destination):
            self.assertIn((repository, revision), ((packaging.PREBUILT_REPOSITORY, packaging.PREBUILT_REVISION), (packaging.SOURCE_REPOSITORY, packaging.SOURCE_REVISION)))
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_text('{"cat": 1}' if name.endswith("json") else "merge one\n" if name.endswith("merges.txt") else name)

        self.start_patch("sha256", side_effect=checksum)
        self.start_patch("download", side_effect=download)
        self.start_patch("input_shapes", side_effect=lambda path: signatures[path.stem])
        self.start_patch("urllib.request.urlopen", return_value=io.BytesIO(b"CreativeML Open RAIL-M unit license"))
        return SimpleNamespace(work=self.root / "work", mnn_source=source), signatures

    def start_patch(self, name, **kwargs):
        active = patch("package_mnn_sd15." + name, **kwargs)
        value = active.start()
        self.addCleanup(active.stop)
        return value


def scenario(name):
    """Register exact human-readable Given/action/expected unittest method names."""
    def register(function):
        setattr(PackagingScenarios, name, function)
        return function
    return register


@scenario("given bundle hashing, payload spans many blocks, expected SHA-256 of every byte")
def hash_payload(self):
    # Given
    payload = b"weights" * 200000
    path = self.root / "weights"
    path.write_bytes(payload)
    # When
    result = packaging.sha256(path)
    # Then
    self.assertEqual(hashlib.sha256(payload).hexdigest(), result)


@scenario("given model download, immutable response succeeds, expected atomic destination and no partial file")
def download_payload(self):
    # Given
    path = self.root / "bundle" / "unet.mnn"
    remote = self.start_patch("urllib.request.urlopen", return_value=io.BytesIO(b"graph"))
    # When
    packaging.download("owner/model", "a" * 40, "unet.mnn", path)
    # Then
    self.assertEqual(b"graph", path.read_bytes())
    self.assertFalse(path.with_suffix(".mnn.partial").exists())
    remote.assert_called_once_with("https://huggingface.co/owner/model/resolve/" + "a" * 40 + "/unet.mnn", timeout=120)


@scenario("given model download, destination already contains bytes, expected existing payload retained without network")
def download_existing(self):
    # Given
    path = self.root / "graph"
    path.write_bytes(b"existing")
    remote = self.start_patch("urllib.request.urlopen")
    # When
    packaging.download("owner/model", "a" * 40, "graph", path)
    # Then
    self.assertEqual(b"existing", path.read_bytes())
    remote.assert_not_called()


@scenario("given model download, response is empty, expected failure and no completed or partial payload")
def download_empty(self):
    # Given
    path = self.root / "graph"
    self.start_patch("urllib.request.urlopen", return_value=io.BytesIO())
    # When
    with self.assertRaisesRegex(ValueError, "Empty download"):
        packaging.download("owner/model", "a" * 40, "graph", path)
    # Then
    self.assertFalse(path.exists())
    self.assertFalse((self.root / "graph.partial").exists())


@scenario("given model download, stream fails after partial write, expected original error and partial cleanup")
def download_interrupted(self):
    # Given
    path = self.root / "graph"
    failure = OSError("connection lost")
    self.start_patch("urllib.request.urlopen", return_value=io.BytesIO(b"graph"))
    def fail_copy(source, output):
        output.write(b"partial")
        raise failure
    self.start_patch("shutil.copyfileobj", side_effect=fail_copy)
    # When
    with self.assertRaises(OSError) as caught:
        packaging.download("owner/model", "a" * 40, "graph", path)
    # Then
    self.assertIs(failure, caught.exception)
    self.assertEqual([], list(self.root.iterdir()))


def graph_fixture(path, has_input=True, has_operations=True):
    """Serialize an Input op with actual pinned-schema field positions."""
    builder = flatbuffers.Builder(256)
    name = builder.CreateString("sample")
    builder.StartVector(4, 4, 4)
    for dimension in reversed([1, -1, -1, -1]):
        builder.PrependInt32(dimension)
    dimensions = builder.EndVector()
    builder.StartObject(3)
    builder.PrependUOffsetTRelativeSlot(0, dimensions, 0)
    main = builder.EndObject()
    builder.StartObject(7)
    builder.PrependUOffsetTRelativeSlot(2, main, 0)
    builder.PrependUOffsetTRelativeSlot(3, name, 0)
    builder.PrependInt32Slot(5, 34 if has_input else 5, 0)
    op = builder.EndObject()
    builder.StartVector(4, 1, 4)
    builder.PrependUOffsetTRelative(op)
    operations = builder.EndVector()
    builder.StartObject(4)
    if has_operations:
        builder.PrependUOffsetTRelativeSlot(3, operations, 0)
    net = builder.EndObject()
    builder.Finish(net)
    path.write_bytes(builder.Output())


@scenario("given graph signature parsing, Input operation has dynamic dimensions, expected exact named dimensions")
def parse_input(self):
    # Given
    path = self.root / "unet.mnn"
    graph_fixture(path)
    # When
    result = packaging.input_shapes(path)
    # Then
    self.assertEqual({"sample": [1, -1, -1, -1]}, result)


@scenario("given graph signature parsing, operation is not Input, expected no signature entry")
def parse_other_op(self):
    # Given
    path = self.root / "unet.mnn"
    graph_fixture(path, has_input=False)
    # When
    result = packaging.input_shapes(path)
    # Then
    self.assertEqual({}, result)


@scenario("given graph signature parsing, operations are absent, expected explicit rejection")
def parse_missing_ops(self):
    # Given
    path = self.root / "unet.mnn"
    graph_fixture(path, has_operations=False)
    # When
    with self.assertRaisesRegex(ValueError, "No graph operations") as caught:
        packaging.input_shapes(path)
    # Then
    self.assertIn("unet.mnn", str(caught.exception))


@scenario("given bundle packaging, pinned source is missing, expected rejection before downloading")
def missing_exporter(self):
    # Given
    args = SimpleNamespace(work=self.root / "work", mnn_source=self.root / "absent")
    download = self.start_patch("download")
    # When
    with self.assertRaisesRegex(ValueError, "pinned MNN source"):
        packaging.prepare(args)
    # Then
    download.assert_not_called()


@scenario("given bundle packaging, exporter checksum changes, expected rejection before downloading")
def wrong_exporter(self):
    # Given
    args, _ = self.prepare_fixture()
    digest = self.start_patch("sha256", return_value="0" * 64)
    download = self.start_patch("download")
    # When
    with self.assertRaisesRegex(ValueError, "exporter checksum mismatch"):
        packaging.prepare(args)
    # Then
    self.assertEqual(1, digest.call_count)
    download.assert_not_called()


@scenario("given bundle packaging, tokenizer writer checksum changes, expected rejection before downloading")
def wrong_writer(self):
    # Given
    args, _ = self.prepare_fixture()
    self.start_patch("sha256", side_effect=["32a2f3f98b3895a8daf5e41dcc5310f74ceae94ab727a7a55db3c3aa4100b8cc", "0" * 64])
    download = self.start_patch("download")
    # When
    with self.assertRaisesRegex(ValueError, "writer checksum mismatch"):
        packaging.prepare(args)
    # Then
    download.assert_not_called()


@scenario("given bundle packaging, graph signature differs from profile, expected no archive")
def wrong_signature(self):
    # Given
    args, signatures = self.prepare_fixture()
    signatures["unet"]["sample"] = [2, 4, 64, 64]
    # When
    with self.assertRaisesRegex(ValueError, "Unsupported graph signatures"):
        packaging.prepare(args)
    # Then
    self.assertFalse((args.work / (packaging.PROFILE + ".zip")).exists())


@scenario("given bundle packaging, tokenizer vocabulary differs, expected identity rejection")
def wrong_vocabulary(self):
    # Given
    args, _ = self.prepare_fixture()
    real_download = packaging.download.side_effect
    def download(repository, revision, name, path):
        real_download(repository, revision, name, path)
        if repository == packaging.PREBUILT_REPOSITORY and name == "vocab.json":
            path.write_text('{"dog": 1}')
    self.start_patch("download", side_effect=download)
    # When
    with self.assertRaisesRegex(ValueError, "Tokenizer identity mismatch: vocab.json"):
        packaging.prepare(args)
    # Then
    self.assertFalse((args.work / "bundle/tokenizer.mtok").exists())


@scenario("given bundle packaging, tokenizer merges differ, expected identity rejection")
def wrong_merges(self):
    # Given
    args, _ = self.prepare_fixture()
    real_download = packaging.download.side_effect
    def download(repository, revision, name, path):
        real_download(repository, revision, name, path)
        if repository == packaging.PREBUILT_REPOSITORY and name == "merges.txt":
            path.write_text("other merge\n")
    self.start_patch("download", side_effect=download)
    # When
    with self.assertRaisesRegex(ValueError, "Tokenizer identity mismatch: merges.txt"):
        packaging.prepare(args)
    # Then
    self.assertFalse((args.work / "bundle/tokenizer.mtok").exists())


@scenario("given bundle packaging, model license is unrecognized, expected rejection without archive")
def wrong_license(self):
    # Given
    args, _ = self.prepare_fixture()
    self.start_patch("urllib.request.urlopen", return_value=io.BytesIO(b"wrong license"))
    # When
    with self.assertRaisesRegex(ValueError, "Expected CreativeML"):
        packaging.prepare(args)
    # Then
    self.assertFalse((args.work / (packaging.PROFILE + ".zip")).exists())


@scenario("given bundle packaging, work directory changes, expected byte identical archive with complete verified inventory")
def deterministic_archive(self):
    # Given
    args, _ = self.prepare_fixture()
    # When
    with contextlib.redirect_stdout(io.StringIO()):
        packaging.prepare(args)
        first = (args.work / (packaging.PROFILE + ".zip")).read_bytes()
        args.work = self.root / "another-directory"
        packaging.urllib.request.urlopen.return_value = io.BytesIO(b"CreativeML Open RAIL-M unit license")
        packaging.prepare(args)
        second = (args.work / (packaging.PROFILE + ".zip")).read_bytes()
    # Then
    self.assertEqual(first, second)
    with zipfile.ZipFile(io.BytesIO(first)) as archive:
        manifest = json.loads(archive.read("manifest.json"))
        self.assertEqual(9, len(manifest["files"]))
        self.assertEqual(set(archive.namelist()) - {"manifest.json"}, {entry["path"] for entry in manifest["files"]})
        for entry in manifest["files"]:
            payload = archive.read(entry["path"])
            self.assertEqual(len(payload), entry["size"])
            self.assertEqual(hashlib.sha256(payload).hexdigest(), entry["sha256"])
        for entry in archive.infolist():
            self.assertEqual((1980, 1, 1, 0, 0, 0), entry.date_time)
            self.assertEqual(0o100644, entry.external_attr >> 16)
        self.assertEqual(packaging.PREBUILT_REVISION, manifest["prebuiltRevision"])
        self.assertEqual(packaging.RUNTIME_REVISION, manifest["runtimeRevision"])


if __name__ == "__main__":
    loader = unittest.TestLoader()
    loader.testMethodPrefix = "given "
    result = unittest.TextTestRunner(verbosity=2).run(loader.loadTestsFromTestCase(PackagingScenarios))
    raise SystemExit(not result.wasSuccessful())
