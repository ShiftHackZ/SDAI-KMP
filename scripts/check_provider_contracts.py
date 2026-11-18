#!/usr/bin/env python3
"""Fetch current provider schemas and compare them with compiled app serializers."""

import argparse
import json
import os
from pathlib import Path

from provider_contracts.providers import PROVIDERS, SKIPPED, sources, verify_inventory
from provider_contracts.schema import compare, operation, payload
from provider_contracts.upstream import fetch

ROOT = Path(__file__).resolve().parents[1]


def check_binding(document, binding, models):
    """Check route existence and each consumed request/response shape independently."""
    endpoint = (
        operation(document, binding.path, binding.method)
        if binding.method != "schema"
        else None
    )
    if endpoint is not None and binding.status not in endpoint.get("responses", {}):
        raise ValueError(f"Published operation has no HTTP {binding.status} response")
    errors = []
    for direction, name in (
        ("request", binding.request),
        ("response", binding.response),
    ):
        if name is None:
            continue
        client = models[name]
        if direction == "response" and binding.is_array:
            client = {"type": "array", "items": client}
        try:
            schema = (
                document
                if endpoint is None
                else payload(document, endpoint, direction, binding.status)
            )
            errors.extend(compare(document, client, schema, direction))
        except (ValueError, KeyError, TypeError) as error:
            errors.append(f"{direction}: {error}")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--provider",
        choices=PROVIDERS,
        help="Only check this provider; default checks all five",
    )
    parser.add_argument(
        "--models",
        type=Path,
        default=ROOT / "network/build/provider-contracts/models.json",
    )
    parser.add_argument(
        "--report",
        type=Path,
        default=ROOT / "build/reports/provider-contracts/report.json",
    )
    args = parser.parse_args()
    verify_inventory(ROOT)
    metadata = json.loads(args.models.read_text())
    if metadata["explicitNulls"] or not metadata["encodeDefaults"]:
        parser.error(
            "Serializer omission policy changed; update the compatibility checker"
        )
    report = {
        "skipped": sorted(SKIPPED),
        "limits": [
            "Structural schema compatibility only; no authenticated API or generation calls.",
            "Stability multipart request values and Fal catalogue validation have no compiled JSON DTO binding.",
            "Hugging Face inference uses its official task JSON Schema; binary output is not checked.",
            "Value constraints, enum values in String DTOs, headers and authentication behavior are not checked.",
        ],
        "sources": [],
    }
    for provider, url, bindings in sources(ROOT):
        if args.provider and args.provider != provider:
            continue
        result = {"provider": provider, "url": url, "checks": []}
        try:
            document, provenance = fetch(url)
            result.update(provenance)
        except Exception as error:
            result["download_error"] = f"{type(error).__name__}: {error}"
        else:
            for binding in bindings:
                try:
                    errors = check_binding(document, binding, metadata["models"])
                except (ValueError, KeyError, TypeError) as error:
                    errors = [str(error)]
                result["checks"].append(
                    {
                        "operation": f"{binding.method.upper()} {binding.path}",
                        "errors": errors,
                    }
                )
        report["sources"].append(result)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2) + "\n")
    lines = [
        "## Current upstream provider contracts",
        "",
        "ArliAI and ChatGPT OAuth: skipped (no public schema).",
        "",
    ]
    has_failed = False
    for provider in PROVIDERS:
        entries = [
            entry for entry in report["sources"] if entry["provider"] == provider
        ]
        if not entries:
            continue
        errors = [
            f"Download failed: {entry['download_error']}"
            for entry in entries
            if "download_error" in entry
        ]
        for entry in entries:
            for check in entry["checks"]:
                errors.extend(
                    f"{check['operation']} {error}" for error in check["errors"]
                )
        lines.append(f"### {provider}: {'FAIL' if errors else 'PASS'}")
        lines.extend(f"- {error}" for error in errors)
        lines.extend([f"- Source: {entry['url']}" for entry in entries])
        lines.append("")
        has_failed = has_failed or bool(errors)
    lines.extend(
        [
            "Published-schema differences do not by themselves establish a deployed-service failure.",
            "",
            *report["limits"],
            "",
        ]
    )
    summary = "\n".join(lines)
    args.report.with_suffix(".md").write_text(summary)
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a") as output:
            output.write(summary)
    print(summary)
    return int(has_failed)


if __name__ == "__main__":
    raise SystemExit(main())
