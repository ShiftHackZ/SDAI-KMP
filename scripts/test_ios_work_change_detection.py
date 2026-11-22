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
    start = lines.index("      - name: Check complete iOS work change range")
    start = lines.index("        run: |", start) + 1
    script = []
    for line in lines[start:]:
        if line and not line.startswith("          "):
            break
        script.append(line[10:] if line else "")
    return "\n".join(script) + "\n"


class IosWorkChangeDetectionTests(unittest.TestCase):
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

    def test_given_unrelated_documentation_and_android_work_when_the_range_is_checked_expected_native_work_tests_skipped(self):
        # Given
        paths = [["README.md","WorkManager and iOS background documentation\n"],["feature/work/src/androidMain/kotlin/NotificationWorker.kt","Android notification changes\n"],["feature/work/src/androidUnitTest/kotlin/WorkerTest.kt","BackgroundTaskManager fixture\n"],["feature/civitai/build.gradle.kts","dependencies { api(\"civitai\") }\n"],[".github/workflows/project_tests.yml","Gate-only change\n"],["scripts/test_ios_work_change_detection.py","Selector tests\n"]]
        for path, content in paths:
            self.write(path, content)
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-ios-work-changes=false", output)

    def test_given_ios_and_shared_work_sources_when_the_range_is_checked_expected_native_work_tests_run(self):
        # Given
        paths = [["feature/work/src/iosMain/kotlin/ModelDownloadManagerImpl.kt","changed\n"],["feature/work/src/iosTest/kotlin/AdmissionTest.kt","changed\n"],["feature/work/src/commonMain/kotlin/WorkModule.kt","changed\n"],["feature/work/swift/AppBridge/IosWorkExecutionAdapter.swift","changed\n"],["feature/work/build.gradle.kts","changed\n"]]
        for path, content in paths:
            with self.subTest(path=path):
                self.write(path, content)
                head = self.commit()
                # When
                output, _ = self.detect()
                # Then
                self.assertEqual("has-ios-work-changes=true", output)
                self.base = head

    def test_given_shared_execution_contracts_when_the_range_is_checked_expected_native_work_tests_run(self):
        # Given
        paths = [["domain/src/commonMain/kotlin/com/example/feature/work/BackgroundTaskManager.kt","changed\n"],["domain/src/commonMain/kotlin/com/example/entity/ModelDownloadBatch.kt","changed\n"],["domain/src/commonMain/kotlin/com/example/entity/TextToImagePayload.kt","changed\n"],["domain/src/commonMain/kotlin/com/example/usecase/generation/TextToImageUseCaseImpl.kt","changed\n"],["domain/src/commonMain/kotlin/com/example/usecase/downloadable/DownloadModelsUseCaseImpl.kt","changed\n"],["domain/src/commonMain/kotlin/com/example/gateway/RemoteGenerationProgressGateway.kt","changed\n"]]
        for path, content in paths:
            with self.subTest(path=path):
                self.write(path, content)
                head = self.commit()
                # When
                output, _ = self.detect()
                # Then
                self.assertEqual("has-ios-work-changes=true", output)
                self.base = head

    def test_given_related_native_and_shared_integrations_when_the_range_is_checked_expected_native_work_tests_run(self):
        # Given
        paths = [["app/ios/iosApp/AIStableDiffusionIOSApp.swift","installIosWorkExecution(koin)\n"],["app/ios/iosApp.xcodeproj/project.pbxproj","feature/work/swift/WorkActivity.swift\n"],["app/ios/iosApp/Info.plist","NSSupportsLiveActivities\n"],["app/ios/iosApp/iosApp.entitlements","com.apple.developer.background-tasks.continued-processing.gpu\n"],["presentation/generation/src/commonMain/kotlin/Handler.kt","BackgroundTaskManager.schedule()\n"],["core/localization/src/androidMain/res/values/strings.xml","<string name=\"notification_download_cancel_title\">Canceled</string>\n"],["gradle/libs.versions.toml","kotlinx-coroutines = \"1.11\"\n"],["build-logic/src/main/kotlin/KmpPlugin.kt","iosSimulatorArm64()\n"]]
        for path, content in paths:
            with self.subTest(path=path):
                self.write(path, content)
                head = self.commit()
                # When
                output, _ = self.detect()
                # Then
                self.assertEqual("has-ios-work-changes=true", output)
                self.base = head

    def test_given_existing_work_integration_when_only_unrelated_lines_change_expected_tests_skipped(self):
        # Given
        self.write("app/ios/iosApp/Info.plist", "NSSupportsLiveActivities\nUnrelated=old\n")
        self.base = self.commit()
        self.write("app/ios/iosApp/Info.plist", "NSSupportsLiveActivities\nUnrelated=new\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-ios-work-changes=false", output)

    def test_given_removed_native_integration_when_range_checked_expected_tests_run(self):
        # Given
        self.write("app/ios/iosApp/Bridge.swift", "installIosWorkExecution(koin)\n")
        self.base = self.commit()
        self.git("rm", "app/ios/iosApp/Bridge.swift")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-ios-work-changes=true", output)

    def test_given_earlier_work_commit_when_latest_commit_unrelated_expected_complete_pr_runs_tests(self):
        # Given
        self.write("feature/work/swift/Work.swift", "native work\n")
        self.commit()
        self.write("README.md", "later docs\n")
        self.commit()
        # When
        output, _ = self.detect()
        # Then
        self.assertEqual("has-ios-work-changes=true", output)

    def test_given_earlier_work_commit_when_complete_push_checked_expected_tests_run(self):
        # Given
        self.write("feature/work/src/iosTest/kotlin/OwnerTest.kt", "ownership\n")
        self.commit()
        self.write("README.md", "later docs\n")
        self.commit()
        # When
        output, _ = self.detect(event="push")
        # Then
        self.assertEqual("has-ios-work-changes=true", output)

    def test_given_unavailable_history_when_range_checked_expected_conservative_tests(self):
        # Given
        self.write("README.md", "docs\n")
        self.commit()
        # When
        output, summary = self.detect(base="0" * 40, event="push")
        # Then
        self.assertEqual("has-ios-work-changes=true", output)
        self.assertIn("Change history unavailable", summary)

    def test_given_base_branch_only_work_change_when_pr_checked_expected_unrelated_feature_skipped(self):
        # Given
        self.git("checkout", "-qb", "feature")
        self.write("README.md", "feature docs\n")
        self.commit()
        feature_head = self.git("rev-parse", "HEAD")
        self.git("checkout", "-qb", "develop", self.base)
        self.write("feature/work/swift/Work.swift", "develop-only change\n")
        develop_head = self.commit()
        self.git("checkout", "-q", feature_head)
        # When
        output, _ = self.detect(base=develop_head)
        # Then
        self.assertEqual("has-ios-work-changes=false", output)


if __name__ == "__main__":
    unittest.main()
