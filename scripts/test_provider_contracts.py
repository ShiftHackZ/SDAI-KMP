#!/usr/bin/env python3
"""Check upstream provider schemas and documented integration APIs against compiled serializers."""

import argparse
import json
import os
from pathlib import Path

from provider_contracts.providers import PROVIDERS, SKIPPED, sources, verify_inventory
from provider_contracts.schema import compare, operation, payload, resolve, validate_documentation
from provider_contracts.upstream import fetch

ROOT = Path(__file__).resolve().parents[1]


def check_documented_models(document, models):
    """Validate the complete Civitai document and all compiled Civitai wire shapes."""
    validate_documentation(document)
    names = sorted(name for name in models if name.startswith("Civitai"))
    if not names:
        raise ValueError("No compiled Civitai serializer contracts were exported")
    checks = []
    for name in names:
        client = models[name]
        schema = document["components"]["schemas"][name]
        errors = []
        if set(client.get("properties", {})) != set(schema.get("properties", {})):
            errors.append(f"{name}: documented and compiled wire fields differ")
        for direction in ("request", "response"):
            errors.extend(compare(document, client, schema, direction))
        checks.append({"operation": f"SCHEMA {name}", "errors": errors, "warnings": []})
    return checks


def check_binding(document, binding, models, warnings=None):
    """Check route existence and each consumed request/response shape independently."""
    if warnings is None:
        warnings = []
    if binding.missing_operation_reason and binding.method not in document.get(
        "paths", {}
    ).get(binding.path, {}):
        warnings.append(f"{binding.missing_operation_reason} (coverage gap)")
        return []
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
            if (
                direction == "response"
                and binding.response_envelope_issue
                and resolve(document, schema).get("type") == "array"
                and client.get("type") == "object"
                and set(client.get("properties", {})) == {"artifacts"}
            ):
                warnings.append(
                    f"Known upstream artifacts-envelope mismatch skipped: {binding.response_envelope_issue} (coverage gap); artifact item types are still checked"
                )
                client = client["properties"]["artifacts"]
            errors.extend(
                compare(document, client, schema, direction, warnings=warnings)
            )
        except (ValueError, KeyError, TypeError) as error:
            errors.append(f"{direction}: {error}")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--provider",
        choices=PROVIDERS,
        help="Only check this provider or integration; default checks all registered contracts",
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
                warnings = []
                try:
                    errors = check_binding(
                        document, binding, metadata["models"], warnings
                    )
                except (ValueError, KeyError, TypeError) as error:
                    errors = [str(error)]
                result["checks"].append(
                    {
                        "operation": f"{binding.method.upper()} {binding.path}",
                        "errors": errors,
                        "warnings": warnings,
                    }
                )
        report["sources"].append(result)
    if args.provider in (None, "CIVITAI"):
        path = ROOT / "network/civitai.openapi.json"
        result = {"provider": "CIVITAI", "url": str(path.relative_to(ROOT)), "source_kind": "application_documentation", "checks": []}
        try:
            result["checks"] = check_documented_models(json.loads(path.read_text()), metadata["models"])
            result["checks"].append({
                "operation": "COVERAGE",
                "errors": [],
                "warnings": ["Application-owned OpenAPI documentation; current upstream schema compatibility is not verified."],
            })
        except (ValueError, KeyError, TypeError) as error:
            result["checks"].append({"operation": "DOCUMENT", "errors": [str(error)], "warnings": []})
        report["sources"].append(result)
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2) + "\n")
    lines = [
        "## Provider and integration API contracts",
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
        warnings = [
            f"{check['operation']} {note}"
            for entry in entries
            for check in entry["checks"]
            for note in check["warnings"]
        ]
        status = "FAIL" if errors else "PASS with coverage gaps" if warnings else "PASS"
        lines.append(f"### {provider}: {status}")
        lines.extend(f"- {error}" for error in errors)
        lines.extend(f"- WARNING: {note}" for note in warnings)
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
