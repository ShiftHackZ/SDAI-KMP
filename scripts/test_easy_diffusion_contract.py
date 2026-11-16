#!/usr/bin/env python3
"""Whole-document and consumed DTO parity gate; does not run a server or inference."""

import json
import re
import sys
from pathlib import Path

sys.dont_write_bytecode = True
from test_openai_compatible_contract import validate

ROOT = Path(__file__).resolve().parents[1]
NETWORK = ROOT / "network/src/commonMain/kotlin/com/shifthackz/aisdv1/network"
CONTRACT = ROOT / "network/easy-diffusion.openapi.json"


def check_contract():
    """Reject incomplete schemas, disconnected operations and undocumented consumed DTO fields."""
    document = json.loads(CONTRACT.read_text())
    validate(document, compatible_client=False, allow_documented_extensions=True)
    expected = {
        "/ping": ("get", "Ping"),
        "/get/models": ("get", "Models"),
        "/render": ("post", "Submission"),
        "/image/stream/{task}": ("get", "Frame"),
        "/image/stop": ("get", "Stop"),
    }
    assert set(document["paths"]) == set(expected)
    for route, (method, schema) in expected.items():
        operation = document["paths"][route][method]
        assert "#/components/schemas/" + schema in json.dumps(operation["responses"]["200"]), route
    render = document["paths"]["/render"]["post"]
    assert render["requestBody"]["required"] is True
    assert "#/components/schemas/RenderRequest" in json.dumps(render["requestBody"])
    definitions = document["components"]["schemas"]
    dto_files = {
        "RenderRequest": ("request/EasyDiffusionRequest.kt", "EasyDiffusionRequest"),
        "Ping": ("response/EasyDiffusionPing.kt", "EasyDiffusionPing"),
        "Models": ("response/EasyDiffusionModels.kt", "EasyDiffusionModels"),
        "Model": ("model/EasyDiffusionModelRaw.kt", "EasyDiffusionModelRaw"),
        "Submission": ("response/EasyDiffusionSubmission.kt", "EasyDiffusionSubmission"),
        "Frame": ("response/EasyDiffusionFrame.kt", "EasyDiffusionFrame"),
        "Image": ("response/EasyDiffusionImage.kt", "EasyDiffusionImage"),
        "TaskMetadata": ("model/EasyDiffusionTaskMetadata.kt", "EasyDiffusionTaskMetadata"),
    }
    for schema, (filename, name) in dto_files.items():
        source = (NETWORK / filename).read_text()
        declaration = re.search(r"data class " + name + r"\((.*)\)\s*$", source, re.S)
        assert declaration is not None, name
        fields = re.findall(r'(?:@SerialName\("([^"\n]+)"\)\s*)?val\s+(\w+)\s*:', declaration.group(1))
        consumed = {wire or field for wire, field in fields}
        documented = set(definitions[schema]["properties"])
        assert consumed <= documented, (schema, consumed - documented)
        if schema == "RenderRequest":
            assert consumed == documented, schema
    request = definitions["RenderRequest"]["properties"]
    assert request["sampler_name"]["enum"] == ["ddim"]
    assert request["num_outputs"]["enum"] == [1]
    assert request["seed"]["maximum"] == 4294967295
    assert definitions["RenderRequest"]["additionalProperties"] is False
    assert {"init_image", "save_to_disk_path"}.isdisjoint(definitions["RenderRequest"]["required"])
    known_fields = {
        "Ping": {"status", "tasks", "devices", "packages_installed", "packages_installing", "cloudflare"},
        "Submission": {"task", "stream", "status", "queue"},
        "Image": {"data", "seed", "path_abs"},
        "Frame": {"status", "step", "step_time", "total_steps", "output", "detail", "render_request", "task_data"},
        "Devices": {"all", "active", "enable_trt"},
    }
    for name, fields in known_fields.items():
        assert fields <= set(definitions[name]["properties"]), name
    assert len(definitions["Frame"]["oneOf"]) == 3
    for name in ("ProgressFrame", "SuccessFrame", "FailureFrame"):
        assert "#/components/schemas/" + name in json.dumps(definitions["Frame"]["oneOf"]), name
    for route, name in (("/image/stream/{task}", "task"), ("/image/stop", "task")):
        parameters = document["paths"][route]["get"]["parameters"]
        task = next(value for value in parameters if value["name"] == name)
        assert task["required"] and task["schema"]["minimum"] == 1
    print("Easy Diffusion OpenAPI completeness and DTO parity passed (static documentation gate).")


if __name__ == "__main__":
    check_contract()
