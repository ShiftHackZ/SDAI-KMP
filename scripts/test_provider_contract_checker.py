#!/usr/bin/env python3
"""Regression checks for the schema checker itself, without contacting providers."""

import unittest
from io import BytesIO
from unittest.mock import patch
from urllib.error import URLError

from check_provider_contracts import check_binding
from provider_contracts.providers import Binding, STABILITY_ENVELOPE_ISSUE
from provider_contracts.schema import compare, operation, payload
from provider_contracts.upstream import fetch


class ContractCheckerTests(unittest.TestCase):
    def test_optional_request_extensions(self):
        """given optional nullable request field, schema permits extensions, expected warning only"""
        # Given a shared DTO field omitted when null and an open upstream object.
        client = {
            "type": "object",
            "properties": {"image_size": {"type": "string", "nullable": True}},
        }
        upstream = {"type": "object", "properties": {}}
        warnings = []
        # When checking the request contract.
        errors = compare({}, client, upstream, "request", warnings=warnings)
        # Then this absence is a coverage warning; an explicitly closed schema still fails.
        self.assertEqual(errors, [])
        self.assertEqual(len(warnings), 1)
        self.assertEqual(
            compare({}, {**client, "required": ["image_size"]}, upstream, "request"), []
        )
        self.assertTrue(
            compare({}, client, {**upstream, "additionalProperties": False}, "request")
        )
        self.assertTrue(
            compare(
                {},
                client,
                {"type": "object", "properties": {"image_size": {"type": "integer"}}},
                "request",
            )
        )

    def test_stability_envelope_issue(self):
        """given known Stability envelope issue, nested types change, expected unrelated mismatch still fails"""
        # Given the published array response and an app artifacts envelope.
        item = {"type": "object", "properties": {"base64": {"type": "string"}}}
        document = {
            "paths": {
                "/generation": {
                    "post": {
                        "responses": {
                            "200": {
                                "content": {
                                    "application/json": {
                                        "schema": {"type": "array", "items": item}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        models = {
            "Result": {
                "type": "object",
                "properties": {"artifacts": {"type": "array", "items": item}},
            }
        }
        binding = Binding(
            "/generation",
            "post",
            response="Result",
            response_envelope_issue=STABILITY_ENVELOPE_ISSUE,
        )
        warnings = []
        # When the documented issue is the only difference.
        errors = check_binding(document, binding, models, warnings)
        # Then it is linked in the warning, but incompatible nested fields still block CI.
        self.assertEqual(errors, [])
        self.assertIn(STABILITY_ENVELOPE_ISSUE, warnings[0])
        changed = {"type": "object", "properties": {"base64": {"type": "integer"}}}
        models["Result"]["properties"]["artifacts"]["items"] = changed
        self.assertTrue(check_binding(document, binding, models))

    def test_unpublished_operation_gap(self):
        """given explicitly unpublished operation, provider starts documenting it, expected validation resumes"""
        # Given a named coverage gap, not a blanket allowance for missing routes.
        binding = Binding(
            "/models", response="Result", missing_operation_reason="Not published"
        )
        models = {"Result": {"type": "integer"}}
        warnings = []
        # When the route is absent and subsequently becomes available with an incompatible response.
        errors = check_binding({"paths": {}}, binding, models, warnings)
        published = {
            "paths": {
                "/models": {
                    "get": {
                        "responses": {
                            "200": {
                                "content": {
                                    "application/json": {"schema": {"type": "string"}}
                                }
                            }
                        }
                    }
                }
            }
        }
        # Then the coverage gap is visible, and new published fields cannot bypass comparison.
        self.assertEqual(errors, [])
        self.assertEqual(len(warnings), 1)
        self.assertTrue(check_binding(published, binding, models))

    def test_structural_compatibility(self):
        # Given compiled wire shapes and provider schema changes.
        integer = {"type": "integer"}
        nullable_string = {"type": "string", "nullable": True}
        cases = [
            (
                "given typed map, upstream changes its value type, expected mismatch",
                {"type": "object", "additionalProperties": integer},
                {"type": "object", "additionalProperties": {"type": "string"}},
                "response",
                True,
            ),
            (
                "given numeric request, integer is accepted as number, expected compatibility",
                integer,
                {"type": "number"},
                "request",
                False,
            ),
            (
                "given string seed, upstream requires integer, expected mismatch",
                {"type": "string"},
                integer,
                "request",
                True,
            ),
            (
                "given integer response, upstream permits number, expected mismatch",
                integer,
                {"type": "number"},
                "response",
                True,
            ),
            (
                "given response union, one variant is incompatible, expected mismatch",
                integer,
                {"anyOf": [integer, {"type": "string"}]},
                "response",
                True,
            ),
            (
                "given request union, one variant accepts the app shape, expected compatibility",
                integer,
                {"oneOf": [integer, {"type": "string"}]},
                "request",
                False,
            ),
            (
                "given nullable response, upstream can return null, expected compatibility",
                nullable_string,
                {"type": ["string", "null"]},
                "response",
                False,
            ),
            (
                "given strict response, upstream can return null, expected mismatch",
                integer,
                {"type": ["integer", "null"]},
                "response",
                True,
            ),
            (
                "given response with new upstream field, app ignores unknown keys, expected compatibility",
                {"type": "object", "properties": {}},
                {"type": "object", "properties": {"extra": integer}},
                "response",
                False,
            ),
            (
                "given consumed optional field, upstream removes it, expected mismatch",
                {"type": "object", "properties": {"id": nullable_string}},
                {"type": "object", "properties": {}},
                "response",
                True,
            ),
            (
                "given required request field, app can omit it, expected mismatch",
                {"type": "object", "properties": {"id": nullable_string}},
                {
                    "type": "object",
                    "properties": {"id": {"type": "string"}},
                    "required": ["id"],
                },
                "request",
                True,
            ),
            (
                "given required response field, upstream makes it optional, expected mismatch",
                {"type": "object", "properties": {"id": integer}, "required": ["id"]},
                {"type": "object", "properties": {"id": integer}},
                "response",
                True,
            ),
        ]
        for scenario, client, upstream, direction, has_errors in cases:
            with self.subTest(scenario):
                # When the consumed subset is compared in its wire direction.
                errors = compare({}, client, upstream, direction)
                # Then request and response assignability stay distinct.
                self.assertEqual(bool(errors), has_errors, errors)

    def test_inherited_schema(self):
        """given inherited schema, fields span allOf references, expected complete comparison"""
        # Given an inherited object with escaped reference names.
        document = {
            "definitions": {
                "base/id": {"type": "object", "properties": {"id": {"type": "integer"}}}
            }
        }
        schema = {
            "allOf": [
                {"$ref": "#/definitions/base~1id"},
                {"type": "object", "properties": {"name": {"type": "string"}}},
            ]
        }
        client = {
            "type": "object",
            "properties": {"id": {"type": "integer"}, "name": {"type": "string"}},
        }
        # When both inherited fields are consumed.
        errors = compare(document, client, schema, "response")
        # Then neither parent fields nor child fields are lost.
        self.assertEqual(errors, [])

    def test_unresolvable_schema(self):
        """given unsupported schema, no usable type exists, expected failure"""
        # Given empty, external and cyclic consumed schemas.
        cases = [{}, {"$ref": "https://example.com/schema"}, {"$ref": "#/loop"}]
        for schema in cases:
            with self.subTest(schema=schema):
                # When comparing a consumed field.
                with self.assertRaises(ValueError):
                    compare(
                        {"loop": {"$ref": "#/loop"}},
                        {"type": "string"},
                        schema,
                        "response",
                    )
                # Then unsupported structures cannot silently produce a pass.

    def test_swagger_and_openapi_bodies(self):
        """given provider operation, body uses a supported dialect, expected selected wire schema"""
        # Given Swagger body parameters and OpenAPI response references.
        schema = {"type": "string"}
        swagger = {"swagger": "2.0"}
        endpoint = {"parameters": [{"in": "body", "schema": schema}]}
        document = {
            "components": {
                "responses": {
                    "Result": {"content": {"application/json": {"schema": schema}}}
                }
            }
        }
        # When selecting the actual JSON payload.
        request = payload(swagger, endpoint, "request")
        response = payload(
            document,
            {"responses": {"200": {"$ref": "#/components/responses/Result"}}},
            "response",
        )
        # Then both dialects select the concrete schema.
        self.assertEqual(request, schema)
        self.assertEqual(response, schema)

    def test_recursive_union(self):
        """given recursive union, a branch refers to itself, expected bounded failure"""
        # Given recursion through a union rather than a direct reference chain.
        document = {"loop": {"anyOf": [{"$ref": "#/loop"}]}}
        # When expanding the consumed union.
        with self.assertRaisesRegex(ValueError, "recursive"):
            compare(document, {"type": "string"}, {"$ref": "#/loop"}, "response")
        # Then the checker fails before exhausting Python's call stack.

    def test_removed_route(self):
        """given consumed route, provider removes the operation, expected failure"""
        # Given a document without the application's route.
        document = {"paths": {}}
        # When selecting that operation.
        with self.assertRaisesRegex(ValueError, "no POST /generate"):
            operation(document, "/generate", "post")
        # Then absent operations cannot count as covered.

    def test_downloads_fail_closed(self):
        """given schema download, upstream is unavailable or returns HTML, expected failure"""
        # Given a public endpoint returning a documentation page with HTTP 200.
        page = BytesIO(b"<html>Documentation</html>")
        page.url = "https://example.com/docs"
        page.headers = {"Content-Type": "text/html"}
        # When retrieving a schema.
        with patch("provider_contracts.upstream.urlopen", return_value=page):
            with self.assertRaisesRegex(ValueError, "received HTML"):
                fetch("https://example.com/schema")
        # Then HTML is rejected and outages exhaust retries without any cached fallback.
        with patch(
            "provider_contracts.upstream.urlopen", side_effect=URLError("offline")
        ) as download:
            with patch("provider_contracts.upstream.time.sleep"):
                with self.assertRaises(URLError):
                    fetch("https://example.com/schema")
            self.assertEqual(download.call_count, 3)

    def test_fresh_json_download(self):
        """given JSON schema, provider uses tab indentation, expected fresh successful parsing"""
        # Given two independent schema downloads with legal JSON tab indentation.
        responses = []
        for kind in ("string", "integer"):
            response = BytesIO(
                (
                    ' {\n\t"$schema":"https://json-schema.org/draft-07/schema", "type":"'
                    + kind
                    + '"}'
                ).encode()
            )
            response.url = "https://example.com/schema"
            response.headers = {"Content-Type": "application/json"}
            responses.append(response)
        # When checking the same URL twice.
        with patch(
            "provider_contracts.upstream.urlopen", side_effect=responses
        ) as download:
            first, first_source = fetch("https://example.com/schema")
            second, second_source = fetch("https://example.com/schema")
        # Then no cached document masks a change and each body has its own hash.
        self.assertEqual(download.call_count, 2)
        self.assertEqual((first["type"], second["type"]), ("string", "integer"))
        self.assertNotEqual(first_source["sha256"], second_source["sha256"])


if __name__ == "__main__":
    unittest.main()
