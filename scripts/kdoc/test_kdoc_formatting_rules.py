"""Regression cases for KDoc layout and Kotlin literal/comment boundaries."""

from contextlib import redirect_stdout
import io
import os
import unittest
from unittest.mock import patch

from test_kdoc_formatting import report_issue, validate_source


VALID_DOC = """/**
 * Returns the current measurement.
 *
 * @param owner Submission identity.
 * @return Measured fraction, or null.
 * @author Dmitriy Moroz
 */
fun measurement(owner: String) = null
"""


class KDocFormattingRulesTest(unittest.TestCase):
    def test_given_multiline_kdoc_validated_expected_project_layout_accepted(self):
        # Given
        sources = [VALID_DOC, VALID_DOC.replace("\n", "\r\n")]
        sources += ["class Container {\n" + "\n".join("    " + line for line in VALID_DOC.splitlines()) + "\n}"]
        sources += [VALID_DOC.replace("Dmitriy Moroz", "Another contributor")]
        # When
        results = [validate_source(source) for source in sources]
        # Then
        self.assertEqual([([], 1)] * len(sources), results)

    def test_given_malformed_layout_validated_expected_precise_diagnostics(self):
        # Given
        cases = [
            ("/** Short. */\nfun run() {}", 1, "own line"),
            (VALID_DOC.replace("/**\n", "/** Summary\n"), 1, "own line"),
            (VALID_DOC.replace(" *\n * @param", " * @param"), 3, "blank ' *'"),
            (VALID_DOC.replace(" * Returns", " *Returns"), 2, "Align KDoc"),
            (VALID_DOC.replace(" * Returns", "* Returns"), 2, "Align KDoc"),
            (VALID_DOC.replace(" *\n", " * \n"), 3, "trailing spaces"),
            (VALID_DOC.replace(" */", "*/"), 7, "aligned */"),
            (VALID_DOC.replace(" */\nfun", " */ fun"), 7, "next line"),
            (VALID_DOC.replace(" */\nfun", " */\n\nfun"), 7, "blank line between"),
            (VALID_DOC.replace(" */\nfun", " */\n\nfun").replace("\n", "\r\n"), 7, "blank line between"),
            (VALID_DOC.replace(" * Returns the current measurement.", " * Returns @author Someone."), 2, "own line"),
            (VALID_DOC.replace(" * @return Measured fraction, or null.", " * @return Fraction. @since 1.0"), 5, "own line"),
            (VALID_DOC.replace(" * @author Dmitriy Moroz", " * @author Dmitriy Moroz\n * @since 1.0"), 7, "last KDoc tag"),
            ("/**\n * @author Someone\n */\nfun run() {}", 2, "description"),
            ("/**\n * Unfinished", 1, "Unterminated"),
            (VALID_DOC[:VALID_DOC.index("fun")] + VALID_DOC, 1, "Consecutive KDocs"),
        ]
        for source, line, message in cases:
            with self.subTest(message=message, source=source):
                # When
                issues, _ = validate_source(source)
                # Then
                self.assertTrue(any(actual_line == line and message in actual for actual_line, actual in issues), issues)

    def test_given_kdoc_like_literals_validated_expected_no_false_blocks(self):
        # Given
        source = '''// /** inline */
/* ordinary /* nested */ /** still ordinary */ */
val normal = "fake /** invalid */ and \\"quote\\""
val raw = """/** not a KDoc */"""
val character = '\\''
val `/** identifier */` = 1
''' + VALID_DOC
        # When
        issues, count = validate_source(source)
        # Then
        self.assertEqual([], issues)
        self.assertEqual(1, count)

    def test_given_string_template_code_validated_expected_real_nested_kdoc_checked(self):
        # Given
        source = 'val value = "${run {\n/** Invalid. */\n42 }}"'
        # When
        issues, count = validate_source(source)
        # Then
        self.assertEqual(1, count)
        self.assertEqual(2, issues[0][0])
        self.assertIn("own line", issues[0][1])

    def test_given_markdown_examples_validated_expected_example_tags_accepted(self):
        # Given
        source = """/**
 * Supports `@param` and ``@author`` references; email dev@example.com.
 *
 * ```kotlin
 * @param example This is sample code.
 * /* nested comment */
 * ```
 *
 * ~~~~
 * @return example
 * ~~~~
 *
 *     @author indented code
 *
 * @customTag Documentation extension.
 * @author Someone
 */
fun sample() {}
"""
        # When
        issues, count = validate_source(source)
        # Then
        self.assertEqual(([], 1), (issues, count))

    def test_given_header_and_annotations_validated_expected_attachment_accepted(self):
        # Given
        sources = [
            "/**\n * File overview.\n */\n\npackage example\n",
            VALID_DOC.replace("fun measurement", "@Deprecated(\"Use another measurement\")\nfun measurement"),
            "/**\n * Summary without tags.\n */\nval value = 1\n",
        ]
        # When
        results = [validate_source(source) for source in sources]
        # Then
        self.assertEqual([([], 1)] * len(sources), results)

    def test_given_github_annotation_path_escaped_expected_single_safe_annotation(self):
        # Given
        output = io.StringIO()
        path = "folder/line\nbreak,%.kt"
        # When
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "true"}), redirect_stdout(output):
            report_issue(path, 7, "Formatting issue.")
        # Then
        annotations = [line for line in output.getvalue().splitlines() if line.startswith("::error")]
        self.assertEqual(
            ["::error file=folder/line%0Abreak%2C%25.kt,line=7::Formatting issue."], annotations
        )


if __name__ == "__main__":
    unittest.main()
