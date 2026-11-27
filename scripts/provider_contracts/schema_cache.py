"""Persist validated upstream bytes with bounded freshness and source provenance."""

import base64
import hashlib
import json
import tempfile
from datetime import datetime, timezone
from pathlib import Path

MAX_SCHEMA_BYTES = 16 * 1024 * 1024


def _path(directory, url):
    """Keep distinct URLs, including Fal endpoint query parameters, separate."""
    return Path(directory) / (hashlib.sha256(url.encode()).hexdigest() + ".json")


def load(directory, url, max_age_seconds):
    """Return intact, unexpired bytes; missing or invalid entries require a download."""
    path = _path(directory, url)
    try:
        if path.stat().st_size > MAX_SCHEMA_BYTES * 2:
            return None
        entry = json.loads(path.read_text())
        provenance = entry["provenance"]
        if provenance["url"] != url or not provenance["resolved_url"].startswith("https://"):
            return None
        fetched_at = datetime.fromisoformat(provenance["fetched_at"])
        age = (datetime.now(timezone.utc) - fetched_at).total_seconds()
        if not 0 <= age < max_age_seconds:
            return None
        body = base64.b64decode(entry["body"], validate=True)
        if len(body) > MAX_SCHEMA_BYTES or hashlib.sha256(body).hexdigest() != provenance["sha256"]:
            return None
        return body, provenance
    except (FileNotFoundError, ValueError, KeyError, TypeError, AttributeError):
        return None


def save(directory, url, body, provenance):
    """Publish a complete cache entry atomically after schema validation succeeds."""
    path = _path(directory, url)
    path.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.NamedTemporaryFile(mode="w", dir=path.parent, suffix=".tmp", delete=False) as output:
        temporary = Path(output.name)
        try:
            json.dump({
                "provenance": provenance,
                "body": base64.b64encode(body).decode("ascii"),
            }, output)
        except BaseException:
            temporary.unlink(missing_ok=True)
            raise
    try:
        temporary.replace(path)
    finally:
        temporary.unlink(missing_ok=True)
