#!/usr/bin/env python3
"""Validate KDoc layout in authored Kotlin files in a Git change range.

CI: python3 scripts/kdoc/test_kdoc_formatting.py --event "$GITHUB_EVENT_PATH"
Local: python3 scripts/kdoc/test_kdoc_formatting.py --base origin/develop --merge-base --worktree

PRs inspect merge-base..head; pushes inspect before..after. Every KDoc in each
changed file is checked, including unchanged blocks in that file. Unchanged
files are excluded. Missing history fails explicitly instead of scanning the
repository. This checks layout, not documentation completeness or prose quality.
"""

import argparse
import json
import os
from pathlib import Path, PurePosixPath
import re
import subprocess
import sys


BLOCK_TAG = re.compile(
    r"(?<![\w@])@(author|param|property|return|receiver|constructor|throws|exception|"
    r"sample|see|since|suppress)\b"
)
TAG_START = re.compile(r"^@([A-Za-z][\w]*)\b")
EXCLUDED_PARTS = {
    "tmp", "build", "vendor", ".build", ".kotlin", ".git", "node_modules",
    "generated", "third_party", "thirdparty",
}


class KDocScanner:
    """Find real KDocs, skipping literals and ordinary nested Kotlin comments."""

    def __init__(self, source):
        self.source = source
        self.blocks = []

    def scan(self):
        self._code(0)
        return self.blocks

    def _comment(self, start):
        depth = 1
        cursor = start + 2
        while cursor < len(self.source):
            if self.source.startswith("/*", cursor):
                depth += 1
                cursor += 2
            elif self.source.startswith("*/", cursor):
                depth -= 1
                cursor += 2
                if depth == 0:
                    return cursor
            else:
                cursor += 1
        return len(self.source)

    def _literal(self, start, delimiter, has_templates=False):
        cursor = start + len(delimiter)
        while cursor < len(self.source):
            if self.source.startswith(delimiter, cursor):
                return cursor + len(delimiter)
            if delimiter != '"""' and self.source[cursor] == "\\":
                cursor += 2
            elif has_templates and self.source.startswith("${", cursor):
                cursor = self._code(cursor + 2, is_template=True)
            else:
                cursor += 1
        return cursor

    def _code(self, cursor, is_template=False):
        braces = 0
        while cursor < len(self.source):
            if self.source.startswith("//", cursor):
                end = self.source.find("\n", cursor)
                cursor = len(self.source) if end < 0 else end + 1
            elif self.source.startswith("/*", cursor):
                end = self._comment(cursor)
                if self.source.startswith("/**", cursor):
                    self.blocks.append((cursor, end))
                cursor = end
            elif self.source.startswith('"""', cursor):
                cursor = self._literal(cursor, '"""', has_templates=True)
            elif self.source[cursor] in "\"'`":
                delimiter = self.source[cursor]
                cursor = self._literal(cursor, delimiter, delimiter == '"')
            elif is_template and self.source[cursor] == "}" and braces == 0:
                return cursor + 1
            else:
                if self.source[cursor] == "{":
                    braces += 1
                elif self.source[cursor] == "}":
                    braces -= 1
                cursor += 1
        return cursor


def validate_source(source):
    """Return (line, message) diagnostics and the number of KDocs inspected."""
    source = source.replace("\r\n", "\n")
    issues = []
    blocks = KDocScanner(source).scan()
    for index, (start, end) in enumerate(blocks):
        line = source.count("\n", 0, start) + 1
        indent = source[source.rfind("\n", 0, start) + 1:start]
        block = source[start:end]
        lines = block.splitlines()
        if not block.endswith("*/"):
            issues.append((line, "Unterminated KDoc."))
            continue
        if len(lines) < 3 or lines[0] != "/**" or indent.strip():
            issues.append((line, "Put /** on its own line; expand one-line KDocs."))
            continue
        if lines[-1] != indent + " */":
            issues.append((line + len(lines) - 1, "Put aligned */ on its own line."))
        line_end = source.find("\n", end)
        suffix = source[end:line_end if line_end >= 0 else len(source)]
        if suffix.strip():
            issues.append((line + len(lines) - 1, "Move code after */ to the next line."))
        contents = []
        for offset, text in enumerate(lines[1:-1], 1):
            prefix = indent + " *"
            remainder = text[len(prefix):]
            if not text.startswith(prefix) or (remainder and not remainder.startswith(" ")):
                issues.append((line + offset, "Align KDoc lines as ' * text' or ' *'."))
                contents.append("")
            else:
                if remainder and not remainder.strip():
                    issues.append((line + offset, "Use ' *' without trailing spaces for blank KDoc lines."))
                contents.append(remainder.removeprefix(" "))
        has_description = False
        has_tags = False
        has_author = False
        fence = None
        for offset, content in enumerate(contents, 1):
            stripped = content.strip()
            marker = re.match(r"(`{3,}|~{3,})", stripped)
            if marker:
                current = marker.group(1)
                if fence is None:
                    fence = current
                elif current[0] == fence[0] and len(current) >= len(fence):
                    fence = None
                has_description = True
                continue
            if fence is not None or content.startswith(("    ", "\t")):
                continue
            # Tags shown as inline code are examples, not KDoc block tags.
            prose = re.sub(r"(`+).*?\1", "", content)
            tag = TAG_START.match(content)
            if tag:
                if not has_tags:
                    if not has_description:
                        issues.append((line + offset, "Add a description before KDoc tags."))
                    if offset == 1 or contents[offset - 2].strip():
                        issues.append((line + offset, "Insert a blank ' *' line before the tag section."))
                if has_author:
                    issues.append((line + offset, "Keep @author as the last KDoc tag."))
                has_author = tag.group(1) == "author"
                has_tags = True
                prose = prose[tag.end():]
            elif stripped:
                has_description = True
            if BLOCK_TAG.search(prose):
                issues.append((line + offset, "Put each KDoc tag on its own line."))
        if index + 1 < len(blocks) and not source[end:blocks[index + 1][0]].strip():
            issues.append((line, "Consecutive KDocs leave the first block unattached; combine or move it."))
        # File headers may precede package/import; declaration KDocs attach directly.
        following = source[end:]
        if re.match(r"[ \t]*\n[ \t]*\n", following) and not re.match(
            r"\s*(?:package|import)\b", following
        ):
            issues.append((line + len(lines) - 1, "Remove the blank line between KDoc and its declaration."))
    return issues, len(blocks)


def git(root, *args):
    result = subprocess.run(
        ["git", "-C", str(root), *args], capture_output=True, check=False
    )
    if result.returncode:
        raise ValueError(f"Git {args[0]} failed: {result.stderr.decode(errors='replace').strip()}")
    return result.stdout


def resolve_commit(root, ref):
    if not ref or set(ref) == {"0"}:
        raise ValueError("Change range is missing a nonzero base or head commit; refusing a full-project scan.")
    return git(root, "rev-parse", "--verify", "--end-of-options", ref + "^{commit}").decode().strip()


def event_range(event):
    """Return immutable base/head refs and whether PR merge-base semantics apply."""
    if "pull_request" in event:
        pr = event["pull_request"]
        return pr["base"]["sha"], pr["head"]["sha"], True
    if "before" in event and "after" in event:
        return event["before"], event["after"], False
    raise ValueError("Expected a pull_request or push event with an explicit change range.")


def is_authored_kotlin(path):
    parts = PurePosixPath(path).parts
    return path.endswith((".kt", ".kts")) and not any(
        part in EXCLUDED_PARTS or part.startswith(".gradle") for part in parts
    )


def changed_sources(root, base, head, *, should_use_merge_base=False, should_use_worktree=False):
    """Read changed Kotlin files from the selected commit, or explicit local overlay."""
    base = resolve_commit(root, base)
    head = resolve_commit(root, head)
    if should_use_merge_base:
        base = git(root, "merge-base", base, head).decode().strip()
    paths = set(git(
        root, "diff", "--name-only", "--no-renames", "--diff-filter=ACMT", "-z", base, head, "--"
    ).split(b"\0"))
    if should_use_worktree:
        if head != resolve_commit(root, "HEAD"):
            raise ValueError("--worktree requires --head HEAD to avoid mixing unrelated snapshots.")
        paths.update(git(
            root, "diff", "--name-only", "--no-renames", "-z", head, "--"
        ).split(b"\0"))
        paths.update(git(
            root, "ls-files", "--others", "--exclude-standard", "-z"
        ).split(b"\0"))
    sources = []
    for raw_path in sorted(paths):
        path = os.fsdecode(raw_path)
        if not is_authored_kotlin(path):
            continue
        if should_use_worktree:
            file = root / path
            if file.is_symlink():
                raise ValueError(f"Refusing to follow changed Kotlin symlink: {path}")
            if not file.exists():
                continue
            source = file.read_text(encoding="utf-8")
        else:
            entry = git(root, "ls-tree", head, "--", path)
            if not entry.startswith(b"100"):
                raise ValueError(f"Changed Kotlin path is not a regular file: {path}")
            source = git(root, "show", f"{head}:{path}").decode("utf-8")
        sources.append((path, source))
    return sources


def report_issue(path, line, message):
    print(f"{path}:{line}: {message}")
    if os.environ.get("GITHUB_ACTIONS") == "true":
        escaped = path.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
        escaped = escaped.replace(",", "%2C").replace(":", "%3A")
        print(f"::error file={escaped},line={line}::{message}")


def main(argv=None):
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--event", type=Path, help="GitHub pull_request/push event JSON")
    parser.add_argument("--base", help="Local comparison base (required without --event)")
    parser.add_argument("--head", default="HEAD", help="Local head commit; default HEAD")
    parser.add_argument("--merge-base", action="store_true", dest="should_use_merge_base", help="Compare the full branch from its merge base")
    parser.add_argument(
        "--worktree", action="store_true", dest="should_use_worktree",
        help="Include staged, unstaged and untracked local Kotlin files",
    )
    args = parser.parse_args(argv)
    if args.event and (args.base or args.should_use_merge_base or args.should_use_worktree or args.head != "HEAD"):
        parser.error("--event cannot be combined with local range options")
    if not args.event and not args.base:
        parser.error("provide --event or an explicit --base; the whole project is never scanned")
    try:
        root = Path(git(Path.cwd(), "rev-parse", "--show-toplevel").decode().strip())
        base, head, should_use_merge_base = (
            event_range(json.loads(args.event.read_text(encoding="utf-8")))
            if args.event else (args.base, args.head, args.should_use_merge_base)
        )
        sources = changed_sources(
            root, base, head, should_use_merge_base=should_use_merge_base, should_use_worktree=args.should_use_worktree
        )
        issue_count = 0
        block_count = 0
        for path, source in sources:
            issues, count = validate_source(source)
            block_count += count
            for line, message in issues:
                report_issue(path, line, message)
            issue_count += len(issues)
        print(
            f"KDoc formatting: {len(sources)} changed Kotlin files, "
            f"{block_count} blocks, {issue_count} issues."
        )
        return 1 if issue_count else 0
    except (ValueError, OSError, KeyError, TypeError) as error:
        print(f"KDoc formatting could not determine/check the change range: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    sys.exit(main())
