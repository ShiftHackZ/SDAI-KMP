package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.data.mocks.mockImageToImagePayload
import com.shifthackz.aisdv1.data.mocks.mockTextToImagePayload
import com.shifthackz.aisdv1.domain.entity.FalAiModel
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.Assert
import org.junit.Test

class ProviderRequestSerializationTest {

    @Test
    fun `given Fal Dev image generation, request is serialized, expected unsupported image size omitted`() {
        // Given
        val payload = mockImageToImagePayload.copy(falAiModel = FalAiModel.FLUX_DEV_IMAGE_TO_IMAGE)

        // When
        val request = payload.mapToFalAiRequest()
        val encoded =
            defaultNetworkJson.parseToJsonElement(defaultNetworkJson.encodeToString(request)).jsonObject

        // Then
        Assert.assertNull(request.imageSize)
        Assert.assertFalse(encoded.containsKey("image_size"))
    }

    @Test
    fun `given text generation with an explicit seed, request is serialized, expected numeric seed`() {
        // Given
        val payload = mockTextToImagePayload.copy(seed = " 4294967295 ")

        // When
        val request = payload.mapToHuggingFaceRequest()
        val encoded =
            defaultNetworkJson.parseToJsonElement(defaultNetworkJson.encodeToString(request)).jsonObject
        val parameters = encoded.getValue("parameters").jsonObject

        // Then
        Assert.assertEquals(4294967295L, parameters.getValue("seed").jsonPrimitive.long)
        Assert.assertFalse(parameters.getValue("seed").jsonPrimitive.isString)
        Assert.assertEquals(payload.prompt, encoded.getValue("inputs").jsonPrimitive.content)
        Assert.assertFalse(parameters.containsKey("prompt"))
    }

    @Test
    fun `given random or invalid generation seed, request is serialized, expected seed omitted`() {
        // Given
        val seeds = listOf("", " ", "-1", "invalid", "9223372036854775808")

        seeds.forEach { seed ->
            // When
            val request = mockTextToImagePayload.copy(seed = seed).mapToHuggingFaceRequest()
            val encoded =
                defaultNetworkJson.parseToJsonElement(defaultNetworkJson.encodeToString(request)).jsonObject

            // Then
            Assert.assertFalse(encoded.getValue("parameters").jsonObject.containsKey("seed"))
        }
    }

    @Test
    fun `given image generation with prompt and seed, request is serialized, expected prompt key and numeric seed`() {
        // Given
        val payload = mockImageToImagePayload.copy(prompt = "  a lighthouse  ", seed = "17")

        // When
        val request = payload.mapToHuggingFaceRequest()
        val encoded =
            defaultNetworkJson.parseToJsonElement(defaultNetworkJson.encodeToString(request)).jsonObject
        val parameters = encoded.getValue("parameters").jsonObject

        // Then
        Assert.assertEquals(payload.base64Image, encoded.getValue("inputs").jsonPrimitive.content)
        Assert.assertEquals("a lighthouse", parameters.getValue("prompt").jsonPrimitive.content)
        Assert.assertEquals(17L, parameters.getValue("seed").jsonPrimitive.long)
        Assert.assertFalse(parameters.containsKey("text"))
    }
}
