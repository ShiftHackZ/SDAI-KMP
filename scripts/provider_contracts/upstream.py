"""Retrieve authoritative schemas with an optional, freshness-bounded byte cache."""

import hashlib
import json
import time
from datetime import datetime, timezone
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

import yaml

from provider_contracts import schema_cache


def _parse(body):
    """Reject non-schema responses on downloads and cache reads alike."""
    try:
        document = json.loads(body)
    except (json.JSONDecodeError, UnicodeDecodeError):
        document = yaml.safe_load(body)
    if not isinstance(document, dict) or not any(
        key in document for key in ("openapi", "swagger", "$schema")
    ):
        raise ValueError("Response is not an OpenAPI, Swagger or JSON Schema document")
    return document


def fetch(url, cache_dir=None, max_age_seconds=86400):
    """Reuse an intact fresh schema; expired entries never conceal download failures."""
    if not url.startswith("https://"):
        raise ValueError("Schema sources must use HTTPS")
    if max_age_seconds < 0:
        raise ValueError("Schema cache age must not be negative")
    if cache_dir is not None:
        cached = schema_cache.load(cache_dir, url, max_age_seconds)
        if cached is not None:
            body, provenance = cached
            try:
                document = _parse(body)
            except (ValueError, yaml.YAMLError):
                pass
            else:
                return document, {**provenance, "is_cache_hit": True}
    for attempt in range(3):
        try:
            request = Request(
                url,
                headers={
                    "User-Agent": "SDAI-provider-contract-check",
                    "Cache-Control": "no-cache",
                    "Accept": "application/json, application/yaml, text/yaml, text/plain",
                },
            )
            with urlopen(request, timeout=30) as response:
                if not response.url.startswith("https://"):
                    raise ValueError("Schema redirected outside HTTPS")
                body = response.read(schema_cache.MAX_SCHEMA_BYTES + 1)
                if len(body) > schema_cache.MAX_SCHEMA_BYTES:
                    raise ValueError("Schema exceeds the 16 MiB limit")
                if "html" in response.headers.get("Content-Type", "").lower():
                    raise ValueError("Expected a schema, received HTML")
                document = _parse(body)
                provenance = {
                    "url": url,
                    "resolved_url": response.url,
                    "fetched_at": datetime.now(timezone.utc).isoformat(),
                    "sha256": hashlib.sha256(body).hexdigest(),
                    "version": document.get(
                        "openapi", document.get("swagger", document.get("$schema"))
                    ),
                }
        except (HTTPError, URLError, TimeoutError, OSError) as error:
            if isinstance(error, HTTPError) and error.code < 500 and error.code != 429:
                raise
            if attempt == 2:
                raise
            time.sleep(attempt + 1)
            continue
        if cache_dir is not None:
            schema_cache.save(cache_dir, url, body, provenance)
        return document, {**provenance, "is_cache_hit": False}
    raise AssertionError("Unreachable download state")
