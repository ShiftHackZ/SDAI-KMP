#!/usr/bin/env python3
"""Check every catalog schema/operation and the published JSON; no application flow runs here."""

import json
import re
import sys
from pathlib import Path

sys.dont_write_bytecode = True
from mnn.openapi import validate_document

ROOT = Path(__file__).resolve().parents[1]


def main():
    """Reject undocumented contracts, DTO drift and unusable catalog source placeholders."""
    document = json.loads((ROOT / "network/mnn-catalog.openapi.json").read_text())
    validate_document(document)
    assert set(document["paths"]) == {"/mnn.json"}
    operation = document["paths"]["/mnn.json"]["get"]
    assert operation["operationId"] == "fetchMnnModels"
    schema = document["components"]["schemas"]["MnnCatalogEntry"]
    source = (
        ROOT
        / "network/src/commonMain/kotlin/com/shifthackz/aisdv1/network/response/DownloadableModelResponse.kt"
    ).read_text()
    fields = set(re.findall(r"val\s+(\w+)\s*:", source))
    assert fields == set(schema["properties"]) == set(schema["required"])
    catalog = json.loads((ROOT / "docs/mnn.json").read_text())
    assert isinstance(catalog, list)
    identities = set()
    for model in catalog:
        assert set(model) == fields
        assert re.fullmatch(r"mnn_[A-Za-z0-9_-]+", model["id"])
        assert model["id"] not in identities
        identities.add(model["id"])
        assert isinstance(model["name"], str) and model["name"].strip()
        assert isinstance(model["size"], str) and model["size"].strip()
        assert isinstance(model["sources"], list) and model["sources"]
        for url in model["sources"]:
            assert isinstance(url, str) and url.startswith("https://")
            assert not any(
                value in url.lower()
                for value in ("example.com", "localhost", "pending", "placeholder")
            )

    upscale_document = json.loads((ROOT / "network/upscaler-catalog.openapi.json").read_text())
    validate_document(upscale_document)
    assert set(upscale_document["paths"]) == {"/upscaler-esrgan.json", "/upscaler-mnn.json"}
    upscale_schema = upscale_document["components"]["schemas"]["UpscalerCatalogEntry"]
    assert set(upscale_schema["properties"]) == set(upscale_schema["required"]) == fields
    for runtime, operation_id in (("esrgan", "fetchUpscalerEsrganModels"), ("mnn", "fetchUpscalerMnnModels")):
        assert upscale_document["paths"][f"/upscaler-{runtime}.json"]["get"]["operationId"] == operation_id
    manifest = json.loads((ROOT / "scripts/mnn/bundle.schema.json").read_text())
    assert manifest["additionalProperties"] is False
    assert set(manifest["required"]) == set(manifest["properties"])
    assert manifest["properties"]["profile"]["const"] == "sd15-512-general-int8-v1"
    assert manifest["properties"]["runtimeVersion"]["const"] == "3.6.2"
    print(
        "MNN/upscaler OpenAPI, DTO, MNN catalog and manifest contract gates passed."
    )


if __name__ == "__main__":
    main()
