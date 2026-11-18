@file:JvmName("ProviderContractExporter")
@file:OptIn(ExperimentalSerializationApi::class)

package com.shifthackz.aisdv1.network.contract

import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.model.HuggingFaceModelRaw
import com.shifthackz.aisdv1.network.model.StabilityAiEngineRaw
import com.shifthackz.aisdv1.network.request.FalAiImageToImageRequest
import com.shifthackz.aisdv1.network.request.FalAiTextToImageRequest
import com.shifthackz.aisdv1.network.request.HordeGenerationAsyncRequest
import com.shifthackz.aisdv1.network.request.HuggingFaceGenerationRequest
import com.shifthackz.aisdv1.network.request.OpenAiRequest
import com.shifthackz.aisdv1.network.request.StabilityTextToImageRequest
import com.shifthackz.aisdv1.network.response.FalAiGenerationResponse
import com.shifthackz.aisdv1.network.response.FalAiQueueStatusResponse
import com.shifthackz.aisdv1.network.response.FalAiQueueSubmitResponse
import com.shifthackz.aisdv1.network.response.HordeGenerationAsyncResponse
import com.shifthackz.aisdv1.network.response.HordeGenerationCheckFullResponse
import com.shifthackz.aisdv1.network.response.HordeGenerationCheckResponse
import com.shifthackz.aisdv1.network.response.HordeUserResponse
import com.shifthackz.aisdv1.network.response.OpenAiResponse
import com.shifthackz.aisdv1.network.response.StabilityAiErrorResponse
import com.shifthackz.aisdv1.network.response.StabilityCreditsResponse
import com.shifthackz.aisdv1.network.response.StabilityGenerationResponse
import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Writes wire names, types, nullability and required fields from the compiled application serializers.
 * The upstream checker consumes this generated metadata instead of guessing Kotlin types from source text.
 * Provider specifications are fetched independently during each check; this output contains no vendor schema.
 *
 * @param args Exactly one path for the generated descriptor document, normally under the module build directory.
 *
 * @author Dmitriy Moroz
 */
fun main(args: Array<String>) {
    require(args.size == 1) { "Expected one output file path." }
    val descriptors = linkedMapOf(
        "OpenAiRequest" to OpenAiRequest.serializer().descriptor,
        "HordeGenerationAsyncRequest" to HordeGenerationAsyncRequest.serializer().descriptor,
        "StabilityTextToImageRequest" to StabilityTextToImageRequest.serializer().descriptor,
        "HuggingFaceGenerationRequest" to HuggingFaceGenerationRequest.serializer().descriptor,
        "FalAiTextToImageRequest" to FalAiTextToImageRequest.serializer().descriptor,
        "FalAiImageToImageRequest" to FalAiImageToImageRequest.serializer().descriptor,
        "OpenAiResponse" to OpenAiResponse.serializer().descriptor,
        "HordeGenerationAsyncResponse" to HordeGenerationAsyncResponse.serializer().descriptor,
        "HordeGenerationCheckResponse" to HordeGenerationCheckResponse.serializer().descriptor,
        "HordeGenerationCheckFullResponse" to HordeGenerationCheckFullResponse.serializer().descriptor,
        "HordeUserResponse" to HordeUserResponse.serializer().descriptor,
        "StabilityGenerationResponse" to StabilityGenerationResponse.serializer().descriptor,
        "StabilityCreditsResponse" to StabilityCreditsResponse.serializer().descriptor,
        "StabilityAiErrorResponse" to StabilityAiErrorResponse.serializer().descriptor,
        "FalAiQueueSubmitResponse" to FalAiQueueSubmitResponse.serializer().descriptor,
        "FalAiQueueStatusResponse" to FalAiQueueStatusResponse.serializer().descriptor,
        "FalAiGenerationResponse" to FalAiGenerationResponse.serializer().descriptor,
        "StabilityAiEngineRaw" to StabilityAiEngineRaw.serializer().descriptor,
        "HuggingFaceModelRaw" to HuggingFaceModelRaw.serializer().descriptor,
    )
    val document = buildJsonObject {
        put("explicitNulls", defaultNetworkJson.configuration.explicitNulls)
        put("encodeDefaults", defaultNetworkJson.configuration.encodeDefaults)
        put("models", JsonObject(descriptors.mapValues { (_, descriptor) -> describe(descriptor) }))
    }
    val output = File(args.single())
    output.parentFile?.mkdirs()
    output.writeText(document.toString() + "\n")
    println("Exported ${descriptors.size} compiled provider DTO contracts to ${output.path}")
}

/**
 * Converts a serializer descriptor into a finite structural schema while preserving nested wire contracts.
 * Unsupported serializer kinds fail the export rather than producing an untyped placeholder.
 *
 * @param descriptor Application serializer whose encoded names and shape are being exported.
 *
 * @return A structural schema for the actual compiled serializer, including optional constructor elements.
 *
 * @author Dmitriy Moroz
 */
private fun describe(descriptor: SerialDescriptor): JsonObject = buildJsonObject {
    put("nullable", descriptor.isNullable)
    when (descriptor.kind) {
        PrimitiveKind.STRING, PrimitiveKind.CHAR -> put("type", "string")
        PrimitiveKind.BOOLEAN -> put("type", "boolean")
        PrimitiveKind.BYTE, PrimitiveKind.SHORT, PrimitiveKind.INT, PrimitiveKind.LONG -> put(
            "type",
            "integer"
        )

        PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> put("type", "number")
        StructureKind.LIST -> {
            put("type", "array")
            put("items", describe(descriptor.getElementDescriptor(0)))
        }

        StructureKind.MAP -> {
            put("type", "object")
            put("additionalProperties", describe(descriptor.getElementDescriptor(1)))
        }

        StructureKind.CLASS, StructureKind.OBJECT -> {
            val indices = 0 until descriptor.elementsCount
            val properties = indices.associate { index ->
                descriptor.getElementName(index) to describe(descriptor.getElementDescriptor(index))
            }
            val required = indices
                .filterNot(descriptor::isElementOptional)
                .map { index -> JsonPrimitive(descriptor.getElementName(index)) }
            put("type", "object")
            put("properties", JsonObject(properties))
            put("required", JsonArray(required))
        }

        SerialKind.ENUM -> {
            val values = (0 until descriptor.elementsCount)
                .map { index -> JsonPrimitive(descriptor.getElementName(index)) }
            put("type", "string")
            put("enum", JsonArray(values))
        }

        else -> error("Unsupported provider serializer ${descriptor.serialName}: ${descriptor.kind}")
    }
}
