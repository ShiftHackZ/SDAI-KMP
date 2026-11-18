package com.shifthackz.aisdv1.domain.entity

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prevents ambiguous persisted requests that identify a single model without its selected archive.
 * @author Dmitriy Moroz
 */
class ModelDownloadRequestTest {
    @Test
    fun `given provider download request, no archive is selected, expected batch mode`() {
        // Given
        val type = LocalAiModel.Type.ONNX
        // When
        val request = ModelDownloadRequest(type)
        // Then
        assertTrue(request.isBatch)
    }

    @Test
    fun `given provider download request, model and mirror are selected, expected single mode`() {
        // Given
        val type = LocalAiModel.Type.MediaPipe
        // When
        val request = ModelDownloadRequest(type, "model", "https://models/model")
        // Then
        assertFalse(request.isBatch)
    }

    @Test
    fun `given provider download request, mirror is absent for selected model, expected invalid request rejected`() {
        // Given
        val type = LocalAiModel.Type.ONNX
        // When
        val failure =
            runCatching { ModelDownloadRequest(type, modelId = "model") }.exceptionOrNull()
        // Then
        assertTrue(failure is IllegalArgumentException)
    }

    @Test
    fun `given provider download request, model is absent for selected mirror, expected invalid request rejected`() {
        // Given
        val type = LocalAiModel.Type.ONNX
        // When
        val failure = runCatching {
            ModelDownloadRequest(
                type,
                sourceUrl = "https://models/model"
            )
        }.exceptionOrNull()
        // Then
        assertTrue(failure is IllegalArgumentException)
    }
}
