package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection
import com.shifthackz.aisdv1.domain.mocks.mockImageToImagePayload
import com.shifthackz.aisdv1.domain.mocks.mockTextToImagePayload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Maps the application's actual text/image payloads without losing supported controls or captured identity.
 * @author Dmitriy Moroz
 */
class EasyDiffusionPayloadMappersTest {
    private val connection =
        EasyDiffusionConnection("https://server.example/prefix", "folder/model", 7)

    @Test
    fun `text mapping preserves supported controls and allocates independent operation identities`() {
        val payload = mockTextToImagePayload.copy(seed = "4294967295", batchCount = 3)
        val mapped = payload.toEasyDiffusionRequest(connection)
        assertEquals(connection.url, mapped.url)
        assertEquals(connection.model, mapped.model)
        assertEquals(connection.selectionRevision, mapped.selectionRevision)
        assertEquals(payload.prompt, mapped.prompt)
        assertEquals(payload.negativePrompt, mapped.negativePrompt)
        assertEquals(payload.width, mapped.width)
        assertEquals(payload.height, mapped.height)
        assertEquals(payload.samplingSteps, mapped.steps)
        assertEquals(payload.cfgScale, mapped.cfgScale)
        assertEquals(4294967295L, mapped.seed)
        assertEquals(3, mapped.batchCount)
        assertNull(mapped.inputImage)
        assertNotEquals(mapped.sessionId, payload.toEasyDiffusionRequest(connection).sessionId)
    }

    @Test
    fun `manual seed boundaries stay exact and invalid values fail`() {
        listOf("0", "4294967295").forEach {
            assertEquals(
                it.toLong(),
                mockTextToImagePayload.copy(seed = it).toEasyDiffusionRequest(connection).seed
            )
        }
        listOf("-2", "4294967296", "1.5", "12x", "999999999999999999999").forEach {
            assertTrue(runCatching {
                mockTextToImagePayload.copy(seed = it).toEasyDiffusionRequest(connection)
            }.isFailure)
        }
    }

    @Test
    fun `blank and minus one resolve into supported uint32 random seeds`() {
        listOf("", "-1").forEach { seed ->
            repeat(5) {
                assertTrue(
                    mockTextToImagePayload.copy(seed = seed)
                        .toEasyDiffusionRequest(connection).seed in 0L..4294967295L
                )
            }
        }
    }

    @Test
    fun `img2img retains source data URL strength and supported controls`() {
        val payload = mockImageToImagePayload.copy(base64Image = "AQID", denoisingStrength = 0.5f)
        val mapped = payload.toEasyDiffusionRequest(connection)
        assertEquals("data:image/png;base64,AQID", mapped.inputImage)
        assertEquals(0.5f, mapped.strength)
        assertEquals(payload.prompt, mapped.prompt)
        assertEquals(payload.seed.toLong(), mapped.seed)
        assertEquals(connection.model, mapped.model)
        assertEquals(connection.selectionRevision, mapped.selectionRevision)
        assertEquals(
            "data:image/jpeg;base64,AQID",
            payload.copy(base64Image = "data:image/jpeg;base64,AQID")
                .toEasyDiffusionRequest(connection).inputImage
        )
    }

    @Test
    fun `missing input and unsupported inpainting fail before transport`() {
        assertTrue(runCatching { mockImageToImagePayload.toEasyDiffusionRequest(connection) }.isFailure)
        assertTrue(runCatching {
            mockImageToImagePayload.copy(base64Image = "AQID", base64MaskImage = "mask")
                .toEasyDiffusionRequest(connection)
        }.isFailure)
    }
}
