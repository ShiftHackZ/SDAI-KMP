"""Download authoritative schemas on every invocation, without an offline fallback."""

import hashlib
import json
import time
from datetime import datetime, timezone
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

import yaml


def fetch(url):
    """Return a parsed public schema and provenance; reject redirects to HTML."""
    if not url.startswith("https://"):
        raise ValueError("Schema sources must use HTTPS")
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
                body = response.read(16 * 1024 * 1024 + 1)
                if len(body) > 16 * 1024 * 1024:
                    raise ValueError("Schema exceeds the 16 MiB limit")
                if "html" in response.headers.get("Content-Type", "").lower():
                    raise ValueError("Expected a schema, received HTML")
                try:
                    document = json.loads(body)
                except (json.JSONDecodeError, UnicodeDecodeError):
                    document = yaml.safe_load(body)
                if not isinstance(document, dict) or not any(
                    key in document for key in ("openapi", "swagger", "$schema")
                ):
                    raise ValueError(
                        "Response is not an OpenAPI, Swagger or JSON Schema document"
                    )
                return document, {
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
    raise AssertionError("Unreachable download state")
