"""Test documentation and serialized DTO fields for the consumed progress contract."""

import json
import re
from pathlib import Path


def validate(document):
    """Reject undocumented schemas, properties, unresolved references and free-form objects."""
    def visit(node):
        if isinstance(node, list):
            for item in node:
                visit(item)
        elif isinstance(node, dict):
            if "$ref" in node:
                target = document
                for part in node["$ref"].removeprefix("#/").split("/"):
                    target = target[part]
            if "type" in node:
                assert node.get("description"), node
            if node.get("type") == "object":
                assert node.get("properties"), node
                for schema in node["properties"].values():
                    assert schema.get("description"), schema
                    assert "type" in schema or "$ref" in schema, schema
            if node.get("type") == "array":
                assert node.get("items"), node
            for child in node.values():
                visit(child)

    visit(document)


def main():
    """Check documented fields against every request/response DTO used by progress transport."""
    root = Path(__file__).resolve().parent.parent
    document = json.loads((root / "network/generation-progress.openapi.json").read_text())
    validate(document)
    source = root / "network/src/commonMain/kotlin/com/shifthackz/aisdv1/network"
    for name, folder in (
        ("Automatic1111ProgressResponse", "response"),
        ("SwarmUiGenerationRequest", "request"),
        ("SwarmUiWorkflowProgress", "response"),
        ("SwarmUiProgressFrame", "response"),
    ):
        text = (source / folder / f"{name}.kt").read_text()
        fields = set()
        for alias, field in re.findall(
            r'(?:@SerialName\("([^"]+)"\)\s*)?val\s+(\w+)\s*:', text
        ):
            fields.add(alias or field)
        assert fields == set(document["components"]["schemas"][name]["properties"]), name
    for name in ("TextToImageRequest", "ImageToImageRequest"):
        text = (source / "request" / f"{name}.kt").read_text()
        assert '@SerialName("force_task_id")' in text, name
    print("Generation progress contracts are documented and match the transport DTOs.")


if __name__ == "__main__":
    main()
