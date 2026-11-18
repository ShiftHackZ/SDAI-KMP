"""Authoritative sources and the routes/DTOs consumed by the hosted clients.

Paths are relative to each upstream document's server/basePath, not the host root.
Keep bindings aligned when changing a Ktor client. Fal endpoints are read from the
application catalogue so newly added models cannot silently escape verification.
"""

import re
from dataclasses import dataclass
from urllib.parse import quote


@dataclass(frozen=True)
class Binding:
    path: str
    method: str = "get"
    request: str | None = None
    response: str | None = None
    status: str = "200"
    is_array: bool = False
    missing_operation_reason: str | None = None
    response_envelope_issue: str | None = None


SKIPPED = {"ARLI_AI", "OPEN_AI_OAUTH"}
PROVIDERS = ("OPEN_AI", "HORDE", "HUGGING_FACE", "STABILITY_AI", "FAL_AI")
STABILITY_ENVELOPE_ISSUE = "https://github.com/Stability-AI/rest-api-support/issues/38"


def sources(root):
    """Return schema URLs and bindings; never return checked-in vendor documents."""
    yield "OPEN_AI", "https://raw.githubusercontent.com/openai/openai-openapi/main/openapi.json", [
        Binding("/models"),
        Binding("/images/generations", "post", "OpenAiRequest", "OpenAiResponse"),
    ]
    yield "HORDE", "https://stablehorde.net/api/swagger.json", [
        Binding("/v2/find_user", response="HordeUserResponse"),
        Binding(
            "/v2/generate/async",
            "post",
            "HordeGenerationAsyncRequest",
            "HordeGenerationAsyncResponse",
            "202",
        ),
        Binding("/v2/generate/check/{id}", response="HordeGenerationCheckResponse"),
        Binding(
            "/v2/generate/status/{id}", response="HordeGenerationCheckFullResponse"
        ),
        Binding("/v2/generate/status/{id}", "delete"),
    ]
    yield "STABILITY_AI", "https://raw.githubusercontent.com/Stability-AI/rest-api-support/main/spec/v1.yaml", [
        Binding("/engines/list", response="StabilityAiEngineRaw", is_array=True),
        Binding("/user/account"),
        Binding("/user/balance", response="StabilityCreditsResponse"),
        Binding(
            "/generation/{engine_id}/text-to-image",
            "post",
            "StabilityTextToImageRequest",
            "StabilityGenerationResponse",
            response_envelope_issue=STABILITY_ENVELOPE_ISSUE,
        ),
        Binding(
            "/generation/{engine_id}/image-to-image",
            "post",
            response="StabilityGenerationResponse",
            response_envelope_issue=STABILITY_ENVELOPE_ISSUE,
        ),
        Binding(
            "/generation/{engine_id}/text-to-image",
            "post",
            response="StabilityAiErrorResponse",
            status="400",
        ),
        Binding(
            "/generation/{engine_id}/image-to-image",
            "post",
            response="StabilityAiErrorResponse",
            status="400",
        ),
    ]
    yield "HUGGING_FACE", "https://huggingface.co/.well-known/openapi.json", [
        Binding("/api/whoami-v2"),
        Binding(
            "/api/models",
            response="HuggingFaceModelRaw",
            is_array=True,
            missing_operation_reason="Hugging Face does not publish its model-list operation in the Hub OpenAPI document; model-list response coverage is unavailable",
        ),
    ]
    yield "HUGGING_FACE", (
        "https://raw.githubusercontent.com/huggingface/huggingface.js/main/"
        "packages/tasks/src/tasks/text-to-image/spec/input.json"
    ), [Binding("text-to-image JSON Schema", "schema", "HuggingFaceGenerationRequest")]
    yield "HUGGING_FACE", (
        "https://raw.githubusercontent.com/huggingface/huggingface.js/main/"
        "packages/tasks/src/tasks/image-to-image/spec/input.json"
    ), [Binding("image-to-image JSON Schema", "schema", "HuggingFaceGenerationRequest")]
    yield "FAL_AI", "https://fal.ai/docs/api-reference/inference-api/openapi/queue.json", [
        Binding("/{owner}/{app}/{path}", "post", response="FalAiQueueSubmitResponse"),
        Binding(
            "/{owner}/{app}/requests/{request_id}/status",
            response="FalAiQueueStatusResponse",
        ),
    ]
    catalogue = (
        root
        / "domain/src/commonMain/kotlin/com/shifthackz/aisdv1/domain/entity/FalAiModel.kt"
    )
    aliases = re.findall(r'alias\s*=\s*"([^"]+)"', catalogue.read_text())
    if not aliases or len(aliases) != len(set(aliases)):
        raise ValueError("Fal catalogue aliases are empty or duplicated")
    for alias in aliases:
        request = (
            "FalAiImageToImageRequest"
            if alias.endswith("/image-to-image")
            else "FalAiTextToImageRequest"
        )
        yield "FAL_AI", "https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=" + quote(
            alias, safe=""
        ), [
            Binding(f"/{alias}", "post", request),
            Binding(
                f"/{alias}/requests/{{request_id}}", response="FalAiGenerationResponse"
            ),
        ]


def verify_inventory(root):
    """Require an explicit check or skip decision for every cloud catalogue entry."""
    source = (
        root
        / "domain/src/commonMain/kotlin/com/shifthackz/aisdv1/domain/entity/ServerSource.kt"
    )
    entries = re.findall(
        r'^    ([A-Z][A-Z_0-9]*)\(\s*key\s*=\s*"[^"]+",\s*type\s*=\s*ServerSourceType\.CLOUD',
        source.read_text(),
        re.M,
    )
    if set(entries) != set(PROVIDERS) | SKIPPED:
        raise ValueError(f"Cloud coverage inventory changed: found {sorted(entries)}")
