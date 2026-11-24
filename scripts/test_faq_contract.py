#!/usr/bin/env python3
"""Verify the documented response for the website's canonical FAQ data."""

import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
from provider_contracts.schema import operation, payload, resolve, validate_documentation

ROOT = Path(__file__).resolve().parents[1]


def validate_value(document, schema, value, path):
    """Validate the static FAQ source against each documented response field."""
    schema = resolve(document, schema)
    if schema["type"] == "object":
        assert isinstance(value, dict), path
        assert set(value) == set(schema["required"]) == set(schema["properties"]), path
        for key, field in value.items():
            validate_value(document, schema["properties"][key], field, f"{path}.{key}")
    elif schema["type"] == "array":
        assert isinstance(value, list) and len(value) >= schema.get("minItems", 0), path
        for index, item in enumerate(value):
            validate_value(document, schema["items"], item, f"{path}[{index}]")
    else:
        assert schema["type"] == "string", path
        assert isinstance(value, str) and len(value.strip()) >= schema.get("minLength", 0), path


def main():
    """Check all OpenAPI descriptions and validate the Jekyll data served at /faq.json."""
    document = json.loads((ROOT / "network/faq.openapi.json").read_text())
    validate_documentation(document)
    endpoint = operation(document, "/faq.json", "GET")
    assert endpoint["operationId"] == "fetchFaq"
    faq = json.loads((ROOT / "docs/_data/faq.json").read_text())
    validate_value(document, payload(document, endpoint, "response"), faq, "$")
    categories = faq["categories"]
    assert len({category["id"] for category in categories}) == len(categories)
    for category in categories:
        items = category["items"]
        assert len({item["id"] for item in items}) == len(items), category["id"]
    print("FAQ OpenAPI documentation and website data contract passed.")


if __name__ == "__main__":
    main()
