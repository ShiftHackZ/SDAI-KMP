package com.shifthackz.aisdv1.network.model

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer

/**
 * Normalizes upstream's single checkpoint string and ordered string-array metadata into one list.
 * Invalid element types remain decoding failures; null is handled by the nullable DTO property.
 * @author Dmitriy Moroz
 */
internal object EasyDiffusionCheckpointListSerializer :
    JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
    /**
     * Wraps a single identifier and validates every array entry before typed list decoding.
     * @param element Server checkpoint metadata, containing a string or an array of strings.
     * @author Dmitriy Moroz
     */
    override fun transformDeserialize(element: JsonElement): JsonElement = when (element) {
        is JsonPrimitive -> {
            require(element.isString) { "Easy Diffusion checkpoint metadata must contain strings" }
            JsonArray(listOf(element))
        }

        is JsonArray -> {
            require(element.all { it is JsonPrimitive && it.isString }) {
                "Easy Diffusion checkpoint metadata must contain strings"
            }
            element
        }

        else -> error("Easy Diffusion checkpoint metadata must be a string or string array")
    }
}
