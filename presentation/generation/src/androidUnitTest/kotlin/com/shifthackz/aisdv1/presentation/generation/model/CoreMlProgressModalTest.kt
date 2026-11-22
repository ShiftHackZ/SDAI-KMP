package com.shifthackz.aisdv1.presentation.generation.model

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageState
import com.shifthackz.aisdv1.presentation.generation.txt2img.progressModal
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageState
import com.shifthackz.aisdv1.presentation.generation.img2img.progressModal
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies Core ML cancellation remains available without the legacy local debug switch.
 *
 * @author Dmitriy Moroz
 */
class CoreMlProgressModalTest {
    @Test
    fun `given iOS Core ML text generation, legacy cancellation flag is off, expected cancel remains available`() {
        // Given
        val state =
            TextToImageState(platform = Platform.IOS, mode = ServerSource.LOCAL_APPLE_CORE_ML)

        // When
        val modal =
            state.progressModal(canCancelLocalGeneration = false) as GenerationModal.Generating

        // Then
        assertTrue(modal.canCancel)
    }

    @Test
    fun `given iOS Core ML image generation, legacy cancellation flag is off, expected cancel remains available`() {
        // Given
        val state =
            ImageToImageState(platform = Platform.IOS, mode = ServerSource.LOCAL_APPLE_CORE_ML)

        // When
        val modal =
            state.progressModal(canCancelLocalGeneration = false) as GenerationModal.Generating

        // Then
        assertTrue(modal.canCancel)
    }
}
