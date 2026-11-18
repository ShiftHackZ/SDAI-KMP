"""Documentation completeness checks for SDAI's own MNN catalogue contract."""


def validate_document(document):
    """Require typed, documented schemas and operations in the owned MNN catalogue contract."""
    errors = []

    def require(is_valid, location, reason):
        if not is_valid:
            errors.append(f"{location}: {reason}")

    def resolve(reference, location):
        if not isinstance(reference, str) or not reference.startswith("#/"):
            errors.append(f"{location}: only local references are allowed")
            return {}
        target = document
        try:
            for part in reference[2:].split("/"):
                target = target[part.replace("~1", "/").replace("~0", "~")]
        except (KeyError, TypeError):
            errors.append(f"{location}: unresolved reference {reference}")
            return {}
        require(
            isinstance(target, dict), location, "reference must resolve to an object"
        )
        return target if isinstance(target, dict) else {}

    def dereference(value, location):
        seen = set()
        while isinstance(value, dict) and "$ref" in value:
            reference = value["$ref"]
            if reference in seen:
                errors.append(
                    f"{location}: circular reference without a concrete definition"
                )
                return {}
            seen.add(reference)
            value = resolve(reference, location)
        return value if isinstance(value, dict) else {}

    def walk(value, location, is_schema=False, active_refs=frozenset()):
        if isinstance(value, list):
            for index, child in enumerate(value):
                walk(child, f"{location}/{index}", is_schema, active_refs)
            return
        if not isinstance(value, dict):
            if is_schema:
                errors.append(f"{location}: an explicit schema object is required")
            return
        if "$ref" in value:
            reference = value["$ref"]
            target = resolve(reference, location)
            if isinstance(reference, str) and reference not in active_refs:
                walk(target, reference, is_schema, active_refs | {reference})
            return
        if is_schema:
            require(
                bool(value.get("description", "").strip()),
                location,
                "description is required",
            )
            compositions = {key for key in ("allOf", "anyOf", "oneOf") if key in value}
            require(
                bool(value.get("type")) or bool(compositions),
                location,
                "explicit type or typed composition is required",
            )
            for key in compositions:
                require(
                    isinstance(value[key], list) and bool(value[key]),
                    location,
                    f"{key} must contain schemas",
                )
            types = value.get("type", [])
            types = [types] if isinstance(types, str) else types
            if "object" in types:
                additional = value.get("additionalProperties")
                require(
                    bool(value.get("properties")),
                    location,
                    "object properties must be enumerated",
                )
                require(
                    additional is False,
                    location,
                    "undocumented free-form object is prohibited",
                )
                require(
                    set(value.get("required", [])) <= set(value.get("properties", {})),
                    location,
                    "unknown required property",
                )
            if "array" in types:
                require("items" in value, location, "array items are required")
        for key, child in value.items():
            child_location = f"{location}/{key}"
            if key in {
                "schemas",
                "properties",
                "dependentSchemas",
                "patternProperties",
            }:
                for name, definition in child.items():
                    walk(definition, f"{child_location}/{name}", True, active_refs)
            elif key == "content":
                require(
                    isinstance(child, dict) and bool(child),
                    child_location,
                    "media content is required",
                )
                if isinstance(child, dict):
                    for media_type, media in child.items():
                        require(
                            isinstance(media, dict) and "schema" in media,
                            f"{child_location}/{media_type}",
                            "every media type requires a schema",
                        )
                walk(child, child_location, active_refs=active_refs)
            else:
                walk(
                    child,
                    child_location,
                    key
                    in {
                        "schema",
                        "items",
                        "requestUrl",
                        "allOf",
                        "anyOf",
                        "oneOf",
                        "not",
                        "if",
                        "then",
                        "else",
                        "contains",
                        "prefixItems",
                    }
                    or (key == "additionalProperties" and isinstance(child, dict)),
                    active_refs,
                )

    require(
        document.get("openapi", "").startswith("3.1."), "#", "OpenAPI 3.1 is required"
    )
    walk(document, "#")
    for route, item in document.get("paths", {}).items():
        for method, operation in item.items():
            if method not in {
                "get",
                "post",
                "put",
                "delete",
                "patch",
                "head",
                "options",
            }:
                continue
            location = f"#/paths/{route}/{method}"
            require(
                bool(operation.get("description")),
                location,
                "operation description required",
            )
            require(
                bool(operation.get("operationId")), location, "operationId required"
            )
            require(bool(operation.get("responses")), location, "responses required")
            for status, response in operation.get("responses", {}).items():
                response = dereference(response, location + "/" + status)
                require(
                    bool(response.get("description")),
                    location + "/" + status,
                    "response description required",
                )
            if "requestBody" in operation:
                body = dereference(operation["requestBody"], location + "/requestBody")
                require(
                    bool(body.get("description")) and bool(body.get("content")),
                    location,
                    "request body description/content required",
                )
            for parameter in operation.get("parameters", []):
                parameter = dereference(parameter, location + "/parameters")
                require(
                    bool(parameter.get("description")) and "schema" in parameter,
                    location,
                    "parameter description/schema required",
                )
    if errors:
        raise ValueError("\n".join(errors))
