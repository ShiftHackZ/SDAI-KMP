#!/usr/bin/env python3
"""Exercise the workflow's actual Bash selector against complete Git change ranges."""

import os
from pathlib import Path
import subprocess
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[1]


def detection_script():
    """Read the selector directly from the workflow so tests cannot drift from CI."""
    lines = (ROOT / ".github/workflows/project_tests.yml").read_text().splitlines()
    start = lines.index("      - name: Check the complete change range")
    start = lines.index("        run: |", start) + 1
    script = []
    for line in lines[start:]:
        if line and not line.startswith("          "):
            break
        script.append(line[10:] if line else "")
    return "\n".join(script) + "\n"


class MnnChangeDetectionTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.env = {
            **os.environ,
            "GIT_CONFIG_GLOBAL": os.devnull,
            "GIT_CONFIG_NOSYSTEM": "1",
            "GIT_AUTHOR_NAME": "CI fixture",
            "GIT_AUTHOR_EMAIL": "ci@example.invalid",
            "GIT_COMMITTER_NAME": "CI fixture",
            "GIT_COMMITTER_EMAIL": "ci@example.invalid",
        }
        self.git("init", "-q")
        self.write("seed.txt", "Initial tree\n")
        self.base = self.commit()

    def git(self, *arguments):
        return subprocess.check_output(
            ["git", *arguments], cwd=self.root, env=self.env, stderr=subprocess.PIPE,
        ).decode().strip()

    def write(self, path, content):
        destination = self.root / path
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_text(content)

    def commit(self):
        self.git("add", ".")
        self.git("-c", "commit.gpgsign=false", "commit", "-qm", "Fixture change")
        return self.git("rev-parse", "HEAD")

    def detect(self, base=None, event="pull_request"):
        output = self.root / "output"
        summary = self.root / "summary"
        output.write_text("")
        summary.write_text("")
        head = self.git("rev-parse", "HEAD")
        base = self.base if base is None else base
        subprocess.run(
            ["bash", "-e", "-o", "pipefail", "-c", detection_script()],
            cwd=self.root,
            env={
                **self.env,
                "EVENT_NAME": event,
                "PR_BASE_SHA": base,
                "PR_HEAD_SHA": head,
                "PUSH_BASE_SHA": base,
                "PUSH_HEAD_SHA": head,
                "RUNNER_TEMP": str(self.root),
                "GITHUB_OUTPUT": str(output),
                "GITHUB_STEP_SUMMARY": str(summary),
            },
            check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
        )
        return output.read_text().strip(), summary.read_text()

    def test_given_unrelated_build_changes_when_complete_pr_checked_expected_native_jobs_skipped(self):
        # Given
        for path in (
            "build.gradle.kts", "settings.gradle.kts", "gradle.properties",
            "gradle/libs.versions.toml", "gradle/presentation-architecture.gradle.kts",
            "build-logic/src/main/kotlin/LibraryPlugin.kt", "feature/civitai/build.gradle.kts",
            "app/ios/iosApp/Info.plist", "app/ios/iosApp.xcodeproj/project.pbxproj",
        ):
            self.write(path, "Civitai dependency or configuration\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=false", output)

    def test_given_shared_mnn_fixtures_when_civitai_fields_added_expected_native_jobs_skipped(self):
        # Given
        self.write("domain/src/androidUnitTest/kotlin/MnnTextToImageUseCaseTest.kt", "AutoUploadToCivitaiUseCase\n")
        self.write("data/src/androidUnitTest/kotlin/CivitaiTest.kt", 'model = "MNN"\n')
        self.write("feature/civitai/src/androidUnitTest/kotlin/CivitaiTest.kt", 'provider = "MNN"\n')
        self.write("storage/schemas/PersistentDatabase/19.json", '{"model": "MNN"}\n')
        self.write("README.md", "Civitai can share MNN outputs\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=false", output)

    def test_given_existing_mnn_gradle_wiring_when_unrelated_dependency_added_expected_native_jobs_skipped(self):
        # Given
        self.write("settings.gradle.kts", 'include(":feature:mnn")\n')
        self.write("gradle/libs.versions.toml", 'mnn = "3.6.2"\n')
        self.base = self.commit()
        self.write("settings.gradle.kts", 'include(":feature:mnn")\ninclude(":feature:civitai")\n')
        self.write("gradle/libs.versions.toml", 'mnn = "3.6.2"\nappauth = "0.11.1"\n')
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=false", output)

    def test_given_mnn_owned_changes_when_complete_pr_checked_expected_native_jobs_run(self):
        # Given
        paths = (
            "feature/mnn/swift/Sources/CMnnBridge/Runtime/allocator.cpp",
            "scripts/mnn/bundle.schema.json", "scripts/build_mnn_ios.py",
            "scripts/test_mnn_native.py", "docs/mnn.json", "network/mnn-catalog.openapi.json",
        )
        # When
        for path in paths:
            with self.subTest(path=path):
                self.write(path, "Updated MNN contract\n")
                self.commit()
                output, summary = self.detect()
                # Then
                self.assertEqual("has-mnn-changes=true", output)
                self.assertIn(path + " (MNN-owned file)", summary)

    def test_given_mnn_production_file_when_body_changes_without_name_expected_native_jobs_run(self):
        # Given
        self.write("data/src/commonMain/kotlin/MnnModelStore.kt", "return 2\n")
        self.commit()
        # When
        output, summary = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=true", output)
        self.assertIn("MNN production source", summary)

    def test_given_shared_production_wiring_when_mnn_reference_changes_expected_native_jobs_run(self):
        # Given
        self.write("presentation/app/src/commonMain/kotlin/KoinBootstrap.kt", "include(mnnModule)\n")
        self.commit()
        # When
        output, summary = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=true", output)
        self.assertIn("changed MNN reference", summary)

    def test_given_mnn_library_when_version_changes_expected_native_jobs_run(self):
        # Given
        self.write("gradle/libs.versions.toml", 'mnn = "3.6.2"\n')
        self.base = self.commit()
        self.write("gradle/libs.versions.toml", 'mnn = "3.7.0"\n')
        self.commit()
        # When
        output, summary = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=true", output)
        self.assertIn("gradle/libs.versions.toml (changed MNN reference)", summary)

    def test_given_mnn_linkage_when_removed_expected_native_jobs_run(self):
        # Given
        self.write("app/ios/iosApp.xcodeproj/project.pbxproj", "SiliconDiffusionMnn\n")
        self.base = self.commit()
        self.write("app/ios/iosApp.xcodeproj/project.pbxproj", "OtherFramework\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=true", output)

    def test_given_earlier_mnn_commit_when_later_commit_unrelated_expected_whole_pr_runs_native_jobs(self):
        # Given
        self.write("feature/mnn/Package.swift", "Updated runtime package\n")
        self.commit()
        self.write("README.md", "Later unrelated change\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=true", output)

    def test_given_push_range_when_mnn_changed_before_latest_commit_expected_native_jobs_run(self):
        # Given
        self.write("feature/mnn/build.gradle.kts", "Changed MNN build\n")
        self.commit()
        self.write("README.md", "Later unrelated change\n")
        self.commit()
        # When
        output, _ = self.detect(event="push")
        # Then
        self.assertEqual("has-mnn-changes=true", output)

    def test_given_gate_only_changes_when_checked_expected_no_self_trigger(self):
        # Given
        self.write(".github/workflows/project_tests.yml", "MNN gate updated\n")
        self.write("scripts/test_mnn_change_detection.py", "MNN selector regressions\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-mnn-changes=false", output)

    def test_given_unavailable_push_history_when_checked_expected_conservative_native_checks(self):
        # Given
        self.write("README.md", "Unrelated change\n")
        self.commit()
        # When
        output, summary = self.detect(base="0" * 40, event="push")
        # Then
        self.assertEqual("has-mnn-changes=true", output)
        self.assertIn("Change history unavailable", summary)


if __name__ == "__main__":
    unittest.main()
