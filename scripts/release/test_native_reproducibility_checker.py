"""Exercise the native checker with ELF bytes produced by the actual FOSS build."""

import os
from pathlib import Path
import tempfile
import unittest
import zipfile

from test_native_reproducibility import compare_native_packages, native_inventory


@unittest.skipUnless(os.environ.get("SDAI_NATIVE_TEST_PACKAGE"), "Requires a built FOSS APK/AAB")
class NativeReproducibilityCheckerTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.package = Path(os.environ["SDAI_NATIVE_TEST_PACKAGE"])
        cls.inventory = native_inventory(cls.package)
        with zipfile.ZipFile(cls.package) as archive:
            cls.libraries = {name: archive.read(name) for name in cls.inventory}

    def write_archive(self, directory, filename, libraries):
        path = Path(directory) / filename
        with zipfile.ZipFile(path, "w") as archive:
            for name, data in libraries.items():
                archive.writestr(name, data)
        return path

    def test_given_built_native_files_when_compared_unchanged_expected_every_elf_to_match(self):
        # Given
        with tempfile.TemporaryDirectory() as directory:
            first = self.write_archive(directory, "first.zip", self.libraries)
            second = self.write_archive(directory, "second.zip", self.libraries)

            # When
            report = compare_native_packages(first, second)

            # Then
            self.assertEqual(report["differences"], {})
            self.assertEqual(report["inventories"], [self.inventory, self.inventory])

    def test_given_built_native_files_when_each_elf_changes_expected_every_change_to_be_detected(self):
        # Given
        with tempfile.TemporaryDirectory() as directory:
            first = self.write_archive(directory, "first.zip", self.libraries)
            for name in self.libraries:
                with self.subTest(entry=name):
                    changed = dict(self.libraries)
                    changed[name] += b"\0"  # Deliberate byte-level fault; preserve a parseable real ELF.
                    second = self.write_archive(directory, "second.zip", changed)

                    # When
                    report = compare_native_packages(first, second)

                    # Then
                    self.assertEqual(set(report["differences"]), {name})

    def test_given_built_native_files_when_elf_has_no_extension_expected_discovery_without_name_rules(self):
        # Given
        with tempfile.TemporaryDirectory() as directory:
            unknown_path = "assets/" + Path(directory).name + "/runtime"
            libraries = {unknown_path: next(iter(self.libraries.values()))}
            package = self.write_archive(directory, "unknown.zip", libraries)

            # When
            inventory = native_inventory(package)

            # Then
            self.assertEqual(set(inventory), {unknown_path})
