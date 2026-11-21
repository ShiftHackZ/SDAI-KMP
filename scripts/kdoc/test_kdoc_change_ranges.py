"""Exercise the checker against actual branch, push and local Git histories."""

import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

from test_kdoc_formatting import changed_sources, event_range, is_authored_kotlin


CHECKER = Path(__file__).with_name("test_kdoc_formatting.py").resolve()
GOOD = "/**\n * Valid documentation.\n *\n * @author Someone\n */\nfun run() {}\n"
BAD = "/** Invalid inline documentation. */\nfun run() {}\n"


class KDocChangeRangesTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.git("init", "-b", "develop")
        self.write("existing.kt", BAD)
        self.base = self.commit("Base with unrelated legacy documentation")

    def git(self, *args):
        return subprocess.run(
            ["git", "-C", str(self.root), "-c", "user.name=Fixture", "-c",
             "user.email=fixture@example.com", *args],
            capture_output=True, text=True, check=True,
        ).stdout.strip()

    def write(self, path, content):
        file = self.root / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content, encoding="utf-8")

    def commit(self, message):
        self.git("add", "--all")
        self.git("commit", "-m", message)
        return self.git("rev-parse", "HEAD")

    def run_checker(self, *args):
        return subprocess.run(
            [sys.executable, str(CHECKER), *args], cwd=self.root,
            capture_output=True, text=True,
            env={**os.environ, "PYTHONDONTWRITEBYTECODE": "1", "GITHUB_ACTIONS": "false"},
        )

    def event(self, data):
        file = self.root / ".git" / "fixture-event.json"
        file.write_text(json.dumps(data), encoding="utf-8")
        return str(file)

    def test_given_feature_branch_multiple_commits_expected_early_commit_checked(self):
        # Given
        self.git("switch", "-c", "feature/docs")
        self.write("early.kt", BAD)
        self.commit("Early feature change")
        self.write("later.kt", GOOD)
        head = self.commit("Later feature change")
        event = self.event({"pull_request": {"base": {"sha": self.base}, "head": {"sha": head}}})
        # When
        result = self.run_checker("--event", event)
        # Then
        self.assertEqual(1, result.returncode, result.stderr)
        self.assertIn("early.kt:1:", result.stdout)
        self.assertNotIn("existing.kt:1:", result.stdout)
        self.assertIn("2 changed Kotlin files", result.stdout)

    def test_given_base_advanced_pr_compared_expected_base_only_file_excluded(self):
        # Given
        self.git("switch", "-c", "feature/docs")
        self.write("feature.kt", GOOD)
        head = self.commit("Feature")
        self.git("switch", "develop")
        self.write("base-only.kt", BAD)
        base = self.commit("Unrelated base advance")
        # When
        sources = changed_sources(self.root, base, head, should_use_merge_base=True)
        # Then
        self.assertEqual([("feature.kt", GOOD)], sources)

    def test_given_push_multiple_commits_expected_complete_before_after_patch_checked(self):
        # Given
        self.write("early.kt", BAD)
        self.commit("First pushed commit")
        self.write("late.kt", GOOD)
        head = self.commit("Second pushed commit")
        event = self.event({"before": self.base, "after": head})
        # When
        result = self.run_checker("--event", event)
        # Then
        self.assertEqual(1, result.returncode, result.stderr)
        self.assertIn("early.kt:1:", result.stdout)
        self.assertIn("2 changed Kotlin files", result.stdout)

    def test_given_feature_squash_merged_expected_merged_patch_only_checked(self):
        # Given
        self.git("switch", "-c", "feature/docs")
        self.write("feature.kt", GOOD)
        self.commit("Feature")
        self.git("switch", "develop")
        self.git("merge", "--squash", "feature/docs")
        head = self.commit("Squash feature")
        # When
        sources = changed_sources(self.root, self.base, head)
        # Then
        self.assertEqual([("feature.kt", GOOD)], sources)

    def test_given_feature_merge_commit_expected_feature_files_checked(self):
        # Given
        self.git("switch", "-c", "feature/docs")
        self.write("feature.kt", BAD)
        self.commit("Feature")
        self.git("switch", "develop")
        self.write("base.txt", "Base advance")
        before = self.commit("Base")
        self.git("merge", "--no-ff", "feature/docs", "-m", "Merge feature")
        after = self.git("rev-parse", "HEAD")
        # When
        result = self.run_checker("--event", self.event({"before": before, "after": after}))
        # Then
        self.assertEqual(1, result.returncode, result.stderr)
        self.assertIn("feature.kt:1:", result.stdout)
        self.assertIn("1 changed Kotlin files", result.stdout)

    def test_given_unusual_path_names_expected_nul_delimited_paths_preserved(self):
        # Given
        path = "folder/line\nbreak,%.kt"
        self.write(path, GOOD)
        head = self.commit("Add unusual path")
        # When
        sources = changed_sources(self.root, self.base, head)
        # Then
        self.assertEqual([(path, GOOD)], sources)

    def test_given_event_head_checkout_modified_expected_committed_blob_checked(self):
        # Given
        self.write("feature.kt", GOOD)
        head = self.commit("Feature")
        self.write("feature.kt", BAD)
        self.write("untracked.kt", BAD)
        event = self.event({"before": self.base, "after": head})
        # When
        result = self.run_checker("--event", event)
        # Then
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("1 changed Kotlin files", result.stdout)

    def test_given_local_overlay_staged_unstaged_untracked_expected_all_checked(self):
        # Given
        self.write("committed.kt", GOOD)
        self.commit("Feature")
        self.write("staged.kt", BAD)
        self.git("add", "staged.kt")
        self.write("committed.kt", BAD)
        self.write("untracked.kt", BAD)
        # When
        result = self.run_checker("--base", self.base, "--merge-base", "--worktree")
        # Then
        self.assertEqual(1, result.returncode, result.stderr)
        for path in ("committed.kt", "staged.kt", "untracked.kt"):
            self.assertIn(f"{path}:1:", result.stdout)
        self.assertIn("3 changed Kotlin files", result.stdout)

    def test_given_deletions_renames_and_spaces_expected_only_existing_destinations_checked(self):
        # Given
        self.git("mv", "existing.kt", "renamed file.kt")
        self.write("deleted.kt", GOOD)
        intermediate = self.commit("Rename and add")
        self.git("rm", "deleted.kt")
        head = self.commit("Remove file")
        # When
        sources = changed_sources(self.root, self.base, head)
        deleted = changed_sources(self.root, intermediate, head)
        # Then
        self.assertEqual([("renamed file.kt", BAD)], sources)
        self.assertEqual([], deleted)

    def test_given_generated_and_non_kotlin_changes_expected_no_project_scan(self):
        # Given
        for path in ("build/A.kt", "module/vendor/A.kt", "module/generated/A.kt", ".gradle-cache/A.kts", "readme.md"):
            self.write(path, BAD)
        self.commit("Excluded changes")
        # When
        result = self.run_checker("--base", self.base)
        # Then
        self.assertEqual(0, result.returncode, result.stderr)
        self.assertIn("0 changed Kotlin files", result.stdout)
        self.assertTrue(is_authored_kotlin("build.gradle.kts"))

    def test_given_missing_history_or_unsupported_event_expected_explicit_failure(self):
        # Given
        events = [
            {"before": "0" * 40, "after": self.base},
            {"before": "f" * 40, "after": self.base},
            {"pull_request": {"base": {}, "head": {"sha": self.base}}},
            {"ref": "refs/heads/develop"},
        ]
        for event in events:
            with self.subTest(event=event):
                # When
                result = self.run_checker("--event", self.event(event))
                # Then
                self.assertEqual(2, result.returncode)
                self.assertIn("could not determine/check", result.stderr)
                self.assertNotIn("existing.kt", result.stdout)

    def test_given_conflicting_or_absent_range_options_expected_usage_failure(self):
        # Given
        event = self.event({"before": self.base, "after": self.base})
        arguments = [[], ["--event", event, "--worktree"], ["--base", "--all"]]
        for args in arguments:
            with self.subTest(args=args):
                # When
                result = self.run_checker(*args)
                # Then
                self.assertEqual(2, result.returncode)

    def test_given_local_kotlin_symlink_expected_no_external_file_read(self):
        # Given
        self.write("source.txt", BAD)
        (self.root / "linked.kt").symlink_to(self.root / "source.txt")
        # When
        result = self.run_checker("--base", self.base, "--worktree")
        # Then
        self.assertEqual(2, result.returncode)
        self.assertIn("symlink", result.stderr)

    def test_given_event_payload_resolved_expected_correct_comparison_semantics(self):
        # Given
        pr = {"pull_request": {"base": {"sha": "base"}, "head": {"sha": "head"}}}
        push = {"before": "before", "after": "after"}
        # When
        ranges = [event_range(pr), event_range(push)]
        # Then
        self.assertEqual([("base", "head", True), ("before", "after", False)], ranges)


if __name__ == "__main__":
    unittest.main()
