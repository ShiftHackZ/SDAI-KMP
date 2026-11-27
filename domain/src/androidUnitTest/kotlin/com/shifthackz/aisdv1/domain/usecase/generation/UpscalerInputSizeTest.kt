package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.HiresConfig
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.isUpscalerInputSizeValid
import com.shifthackz.aisdv1.domain.mocks.mockTextToImagePayload
import com.shifthackz.aisdv1.domain.mocks.mockImageToImagePayload
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects generation submission bounds without restricting standalone input preparation.
 *
 * @author Dmitriy Moroz
 */
class UpscalerInputSizeTest {
    @Test
    fun `given text generation with upscaling, width or height exceeds the bound, expected invalid submission`() {
        // Given
        val payload = mockTextToImagePayload.copy(upscaler = UpscalerConfiguration("model"))
        // When
        val isWideValid = payload.copy(width = 1025, height = 512).isUpscalerInputSizeValid
        val isTallValid = payload.copy(width = 512, height = 1025).isUpscalerInputSizeValid
        // Then
        assertFalse(isWideValid)
        assertFalse(isTallValid)
        assertTrue(payload.copy(width = 1024, height = 1024).isUpscalerInputSizeValid)
    }

    @Test
    fun `given image generation with upscaling, requested height exceeds the bound, expected invalid submission`() {
        // Given
        val payload = mockImageToImagePayload.copy(
            width = 512,
            height = 1536,
            upscaler = UpscalerConfiguration("model")
        )
        // When
        val isValid = payload.isUpscalerInputSizeValid
        // Then
        assertFalse(isValid)
        assertTrue(payload.copy(upscaler = null).isUpscalerInputSizeValid)
    }

    @Test
    fun `given text generation with Hires Fix and upscaling, scaled output exceeds bounds, expected invalid submission`() {
        // Given
        val payload = mockTextToImagePayload.copy(
            width = 768,
            height = 768,
            hires = HiresConfig(enabled = true, scale = 2f),
            upscaler = UpscalerConfiguration("model")
        )
        // When
        val isValid = payload.isUpscalerInputSizeValid
        // Then
        assertFalse(isValid)
        assertTrue(payload.copy(upscaler = null).isUpscalerInputSizeValid)
    }
}
