"""Exercise release runner selection using the real repository and planner CLI."""

import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


ROOT = Path(__file__).resolve().parents[2]
PLANNER = Path(__file__).with_name("plan.py")


def planned_builds(foss, full, playstore):
    """Run the production planner with actual release selections and repository metadata."""
    with tempfile.TemporaryDirectory(prefix="sdai-release-plan-") as temporary:
        output = Path(temporary) / "output"
        environment = dict(os.environ, FOSS_OUTPUT=foss, FULL_OUTPUT=full, PLAYSTORE_OUTPUT=playstore,
                           FOSS_REFERENCE_URL="", FOSS_REFERENCE_SHA256="", GITHUB_OUTPUT=str(output),
                           GITHUB_STEP_SUMMARY=str(Path(temporary) / "summary"))
        subprocess.run([sys.executable, str(PLANNER), str(ROOT)], env=environment, check=True,
                       capture_output=True, text=True)
        values = dict(line.split("=", 1) for line in output.read_text().splitlines())
        return json.loads(values["builds"])["include"]


class ReleasePlanTests(unittest.TestCase):
    def test_given_default_outputs_when_planned_expected_only_foss_to_rebuild(self):
        # Given
        selections = ("apk", "apk", "aab")

        # When
        builds = planned_builds(*selections)

        # Then
        self.assertEqual([(b["flavor"], b["format"], b["attempt"], b["attempts"]) for b in builds],
                         [("foss", "apk", 1, 2), ("foss", "apk", 2, 2),
                          ("full", "apk", 1, 1), ("playstore", "aab", 1, 1)])

    def test_given_non_foss_outputs_when_both_formats_selected_expected_one_build_per_output(self):
        # Given
        selections = ("skip", "both", "both")

        # When
        builds = planned_builds(*selections)

        # Then
        self.assertEqual(len(builds), 4)
        self.assertEqual({(b["flavor"], b["format"]) for b in builds},
                         {("full", "apk"), ("full", "aab"), ("playstore", "apk"), ("playstore", "aab")})
        self.assertTrue(all(b["attempt"] == b["attempts"] == 1 for b in builds))
        self.assertTrue(all(b["repro_profile"] == "single" for b in builds))

    def test_given_foss_outputs_when_both_formats_selected_expected_two_builds_per_output(self):
        # Given
        selections = ("both", "skip", "skip")

        # When
        builds = planned_builds(*selections)

        # Then
        self.assertEqual([(b["format"], b["attempt"]) for b in builds],
                         [("apk", 1), ("apk", 2), ("aab", 1), ("aab", 2)])
        self.assertTrue(all(b["flavor"] == "foss" and b["attempts"] == 2 for b in builds))

    def test_given_foss_rebuild_when_planned_expected_different_source_and_sdk_paths(self):
        # Given
        selections = ("apk", "skip", "skip")

        # When
        first, second = planned_builds(*selections)

        # Then
        self.assertEqual(first["source_path"], "source")
        self.assertEqual(first["sdk_path"], "/opt/android-sdk")
        self.assertNotEqual(first["source_path"], second["source_path"])
        self.assertNotEqual(first["sdk_path"], second["sdk_path"])
        self.assertEqual((first["repro_profile"], second["repro_profile"]), ("baseline", "varied"))

    def test_given_playstore_aab_when_selected_alone_expected_one_build_runner(self):
        # Given
        selections = ("skip", "skip", "aab")

        # When
        builds = planned_builds(*selections)

        # Then
        self.assertEqual(len(builds), 1)
        self.assertEqual(builds[0]["task"], ":app:bundlePlaystoreRelease")
        self.assertEqual(builds[0]["attempts"], 1)


if __name__ == "__main__":
    unittest.main()
