#!/usr/bin/env python3
"""Verify the public blog schema and its canonical Jekyll sources."""

import argparse
from datetime import date
import json
from pathlib import Path
import sys
from urllib.parse import urlsplit

sys.dont_write_bytecode = True

from provider_contracts.schema import operation, payload, validate_documentation
from test_faq_contract import validate_value

ROOT = Path(__file__).resolve().parents[1]
DOCS = ROOT / "docs"


def verify_sources():
    """Read article metadata and Markdown from the website documents."""
    posts = {}
    for source in (DOCS / "blog").glob("*/BLOGPOST.md"):
        front_matter, markdown = source.read_text(encoding="utf-8").removeprefix("---\n").split("\n---\n", 1)
        fields = dict(line.split(": ", 1) for line in front_matter.splitlines() if ": " in line)
        required = ("title", "description", "date", "category", "cover_alt")
        assert all(fields.get(key, "").strip() for key in required), source
        date.fromisoformat(fields["date"].strip().strip('"'))
        assert markdown.strip(), source
        assert (source.parent / fields.get("cover", "header.jpg").strip().strip('"')).is_file(), source
        posts[source.parent.name] = markdown.strip()
    return posts


def verify_built_feed(site, posts, document, response):
    """Compare the real Jekyll feed, generated article pages and source bodies."""
    feed = json.loads((site / "blog.json").read_text(encoding="utf-8"))
    validate_value(document, response, feed, "$")
    entries = feed["posts"]
    assert len(entries) == len(posts)
    assert {post["slug"] for post in entries} == posts.keys()
    dates = [date.fromisoformat(post["publishedOn"]) for post in entries]
    assert dates == sorted(dates, reverse=True)
    for post in entries:
        slug = post["slug"]
        article = urlsplit(post["url"])
        cover = urlsplit(post["coverUrl"])
        assert article.scheme == cover.scheme == "https" and article.netloc == cover.netloc
        assert article.path == f"/blog/{slug}/"
        assert cover.path.startswith(article.path)
        assert (site / article.path.lstrip("/") / "index.html").is_file()
        assert (site / cover.path.lstrip("/")).is_file()
        assert post["markdown"] == posts[slug]


def main():
    """Check documented fields, article sources, and Jekyll output when available."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--built-site", type=Path)
    args = parser.parse_args()
    document = json.loads((ROOT / "network/blog.openapi.json").read_text(encoding="utf-8"))
    validate_documentation(document)
    endpoint = operation(document, "/blog.json", "GET")
    assert endpoint["operationId"] == "fetchBlog"
    posts = verify_sources()
    if args.built_site:
        verify_built_feed(args.built_site, posts, document, payload(document, endpoint, "response"))
    print(f"Blog OpenAPI and website contract passed for {len(posts)} articles.")


if __name__ == "__main__":
    main()
