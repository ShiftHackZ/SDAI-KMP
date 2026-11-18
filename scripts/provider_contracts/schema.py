"""Compare the consumed structural subset of Swagger, OpenAPI and JSON Schema.

This deliberately checks wire shapes, not arbitrary JSON Schema constraints. It
does not prove value ranges, model availability, authentication or generation.
Unknown/recursive structures in a consumed field fail instead of passing silently.
"""


def resolve(document, schema, trail=()):
    """Resolve local references and merge object inheritance without losing fields."""
    if not isinstance(schema, dict) or not schema:
        raise ValueError("Consumed schema is empty or untyped")
    schema = dict(schema)
    reference = schema.pop("$ref", None)
    if reference:
        if not reference.startswith("#/") or reference in trail:
            raise ValueError(
                f"Unsupported external or recursive reference: {reference}"
            )
        target = document
        for segment in reference[2:].split("/"):
            target = target[segment.replace("~1", "/").replace("~0", "~")]
        schema = {**resolve(document, target, (*trail, reference)), **schema}
    for part in schema.pop("allOf", []):
        part = resolve(document, part, trail)
        properties = {**schema.get("properties", {}), **part.get("properties", {})}
        required = sorted(
            set(schema.get("required", [])) | set(part.get("required", []))
        )
        schema = {**part, **schema}
        if properties:
            schema["properties"] = properties
        if required:
            schema["required"] = required
    return schema


def variants(document, schema, depth=0):
    """Expand unions and nullable types, retaining their surrounding constraints."""
    if depth > 32:
        raise ValueError("Consumed union is recursive or exceeds maximum nesting")
    schema = resolve(document, schema)
    branches = schema.pop("anyOf", schema.pop("oneOf", None))
    if branches is not None:
        return [
            item
            for branch in branches
            for item in variants(document, {**schema, **branch}, depth + 1)
        ]
    types = schema.get("type")
    if isinstance(types, list):
        return [{**schema, "type": kind} for kind in types]
    if schema.get("nullable"):
        return [{**schema, "nullable": False}, {"type": "null"}]
    if "type" not in schema:
        if "properties" in schema:
            schema["type"] = "object"
        elif "enum" in schema and all(
            isinstance(value, str) for value in schema["enum"]
        ):
            schema["type"] = "string"
        else:
            raise ValueError("Consumed schema has no structural type")
    return [schema]


def compare(document, client, upstream, direction, path="$", depth=0):
    """Return differences; request unions accept one shape, responses must accept all."""
    if depth > 32:
        raise ValueError(f"Consumed schema exceeds maximum nesting at {path}")
    choices = variants(document, upstream)
    differences = []
    for choice in choices:
        if choice["type"] == "null":
            result = (
                []
                if direction == "request" or client.get("nullable")
                else [f"{path}: upstream can return null"]
            )
        else:
            result = compare_shape(document, client, choice, direction, path, depth)
        differences.append(result)
    if direction == "request":
        # explicitNulls=false means nullable request values are omitted, not sent as null.
        non_null = [
            result
            for choice, result in zip(choices, differences)
            if choice["type"] != "null"
        ]
        return (
            min(non_null, key=len)
            if non_null
            else [f"{path}: upstream only accepts null"]
        )
    return sorted(set(error for result in differences for error in result))


def compare_shape(document, client, upstream, direction, path, depth):
    expected, actual = upstream["type"], client["type"]
    is_compatible = (
        actual == expected
        or (direction == "request" and actual == "integer" and expected == "number")
        or (direction == "response" and actual == "number" and expected == "integer")
    )
    if not is_compatible:
        return [f"{path}: app {actual}, upstream {expected} ({direction})"]
    if actual == "array":
        return compare(
            document,
            client["items"],
            upstream.get("items", {}),
            direction,
            path + "[]",
            depth + 1,
        )
    if actual != "object":
        return []
    if "additionalProperties" in client:
        values = upstream.get("additionalProperties")
        if not isinstance(values, dict) or not values:
            return [f"{path}: upstream map values have no published structural type"]
        return compare(
            document,
            client["additionalProperties"],
            values,
            direction,
            path + ".*",
            depth + 1,
        )
    errors = []
    properties = upstream.get("properties", {})
    client_properties = client.get("properties", {})
    if direction == "request":
        for name in upstream.get("required", []):
            if name not in client_properties or client_properties[name].get("nullable"):
                errors.append(
                    f"{path}.{name}: required upstream request field can be absent in app"
                )
    for name, field in client_properties.items():
        field_path = f"{path}.{name}"
        if name not in properties:
            errors.append(
                f"{field_path}: app field absent from published schema ({direction})"
            )
            continue
        if (
            direction == "response"
            and name in client.get("required", [])
            and not field.get("nullable")
        ):
            if name not in upstream.get("required", []):
                errors.append(
                    f"{field_path}: required app response field is optional upstream"
                )
        try:
            errors.extend(
                compare(
                    document, field, properties[name], direction, field_path, depth + 1
                )
            )
        except (ValueError, KeyError, TypeError) as error:
            errors.append(f"{field_path}: {error}")
    return errors


def operation(document, path, method):
    """Require an explicitly published route and HTTP method."""
    try:
        return resolve(document, document["paths"][path][method.lower()])
    except KeyError as error:
        raise ValueError(f"Published schema has no {method.upper()} {path}") from error


def payload(document, endpoint, direction, status="200", media="application/json"):
    """Select the application's content type, including Swagger 2 body parameters."""
    if direction == "request":
        if "swagger" in document:
            bodies = [
                item
                for item in endpoint.get("parameters", [])
                if item.get("in") == "body"
            ]
            if len(bodies) != 1:
                raise ValueError("Expected exactly one Swagger request body")
            return bodies[0]["schema"]
        value = resolve(document, endpoint["requestBody"])
    else:
        value = resolve(document, endpoint["responses"][status])
        if "swagger" in document:
            return value["schema"]
    return value["content"][media]["schema"]
