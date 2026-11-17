package com.shifthackz.aisdv1.work.mappers

import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies queued MNN work keeps immutable model identity and rejects unknown protocol values.
 *
 * @author Dmitriy Moroz
 */
class MnnWorkPayloadMappersTest {
    private val payload = TextToImagePayload(
        prompt = "cat 😀",
        negativePrompt = "",
        samplingSteps = 20,
        cfgScale = 7.5f,
        width = 512,
        height = 512,
        restoreFaces = false,
        seed = "42",
        subSeed = "",
        subSeedStrength = 0f,
        sampler = "PNDM",
        nsfw = false,
        batchCount = 1,
        style = null,
        quality = null,
        openAiModel = null,
        stabilityAiClipGuidance = null,
        stabilityAiStylePreset = null,
        sourceSnapshot = ServerSource.LOCAL_MNN,
        mnnModelId = "mnn_sd15",
        mnnActivationId = "a".repeat(64),
        mnnProfile = "sd15-512-general-int8-v1"
    )

    @Test
    fun `given MNN work payload, CPU request is serialized, expected complete round trip`() {
        // Given
        val original = payload.copy(mnnBackend = MnnBackend.CPU)

        // When
        val restored = original.toByteArray().toTextToImagePayload()

        // Then
        assertEquals(original, restored)
    }

    @Test
    fun `given MNN work payload, OpenCL and supplementary prompt are serialized, expected complete round trip`() {
        // Given
        val original = payload.copy(
            mnnBackend = MnnBackend.OPENCL,
            seed = Int.MAX_VALUE.toString(),
            providerSelectionRevision = 72L
        )

        // When
        val restored = original.toByteArray().toTextToImagePayload()

        // Then
        assertEquals(original, restored)
    }

    @Test
    fun `given legacy work payload, MNN fields are absent, expected CPU and absent captured identity`() {
        // Given
        val fields = Json.parseToJsonElement(payload.toByteArray().decodeToString()).jsonObject
        val legacy =
            JsonObject(fields.filterKeys { !it.startsWith("mnn") && it != "sourceSnapshot" })

        // When
        val restored = legacy.toString().encodeToByteArray().toTextToImagePayload()

        // Then
        assertEquals(
            payload.copy(
                mnnBackend = MnnBackend.CPU,
                mnnModelId = null,
                mnnActivationId = null,
                mnnProfile = null,
                sourceSnapshot = null
            ), restored
        )
    }

    @Test
    fun `given MNN work payload, backend key is unknown, expected unreadable request`() {
        // Given
        val fields = Json.parseToJsonElement(payload.toByteArray().decodeToString()).jsonObject
        val invalid = JsonObject(fields + ("mnnBackend" to JsonPrimitive("automatic")))

        // When
        val restored = invalid.toString().encodeToByteArray().toTextToImagePayload()

        // Then
        assertNull(restored)
    }

    @Test
    fun `given MNN work payload, source key is unknown, expected unreadable request`() {
        // Given
        val fields = Json.parseToJsonElement(payload.toByteArray().decodeToString()).jsonObject
        val invalid = JsonObject(fields + ("sourceSnapshot" to JsonPrimitive("removed-provider")))

        // When
        val restored = invalid.toString().encodeToByteArray().toTextToImagePayload()

        // Then
        assertNull(restored)
    }

    @Test
    fun `given MNN work payload, future unrelated fields exist, expected known request preserved`() {
        // Given
        val fields = Json.parseToJsonElement(payload.toByteArray().decodeToString()).jsonObject
        val future = JsonObject(fields + ("futureOption" to JsonPrimitive(true)))

        // When
        val restored = future.toString().encodeToByteArray().toTextToImagePayload()

        // Then
        assertEquals(payload, restored)
    }

    @Test
    fun `given work payload, JSON is truncated, expected unreadable request`() {
        // Given
        val truncated = "{".encodeToByteArray()

        // When
        val restored = truncated.toTextToImagePayload()

        // Then
        assertNull(restored)
    }
}
