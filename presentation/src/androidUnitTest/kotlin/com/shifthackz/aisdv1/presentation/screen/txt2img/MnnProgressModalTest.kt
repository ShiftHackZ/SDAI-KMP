package com.shifthackz.aisdv1.presentation.screen.txt2img

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.widget.dialog.progressStatusText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies the iOS MNN modal displays native overall progress without changing sampling inputs.
 *
 * @author Dmitriy Moroz
 */
class MnnProgressModalTest {
    @Test
    fun `given iOS MNN generation, native reports overall progress, expected percentage text and unchanged sampling count`() {
        // Given
        val state = TextToImageState(
            platform = Platform.IOS,
            mode = ServerSource.LOCAL_MNN,
            samplingSteps = 20
        )
        val initial =
            state.progressModal(canCancelLocalGeneration = true) as GenerationModal.Generating

        // When
        val modal = initial.copy(status = LocalDiffusionStatus(65, 100))
        val text = progressStatusText(null, modal.pair, modal.overallPercentage)

        // Then
        assertEquals("65%", text)
        assertNull(modal.pair)
        assertEquals(20, state.samplingSteps)
    }

    @Test
    fun `given iOS MNN generation, native counters are not available, expected no invented sampling or percentage status`() {
        // Given
        val state = TextToImageState(platform = Platform.IOS, mode = ServerSource.LOCAL_MNN)

        // When
        val modal =
            state.progressModal(canCancelLocalGeneration = true) as GenerationModal.Generating
        val text = progressStatusText(null, modal.pair, modal.overallPercentage)

        // Then
        assertNull(text)
        assertNull(modal.pair)
    }

    @Test
    fun `given sampling progress modal, provider reports actual iterations, expected original step counters retained`() {
        // Given
        val modal = GenerationModal.Generating(status = LocalDiffusionStatus(12, 20))

        // When
        val counters = modal.pair
        val percentage = modal.overallPercentage

        // Then
        assertEquals(12 to 20, counters)
        assertNull(percentage)
    }

    @Test
    fun `given overall progress modal, provider counters exceed bounds, expected percentage remains within zero and one hundred`() {
        // Given
        val modal = GenerationModal.Generating(isOverallProgress = true)

        // When
        val above = modal.copy(status = LocalDiffusionStatus(150, 100)).overallPercentage
        val below = modal.copy(status = LocalDiffusionStatus(-5, 100)).overallPercentage

        // Then
        assertEquals(100, above)
        assertEquals(0, below)
    }
}
