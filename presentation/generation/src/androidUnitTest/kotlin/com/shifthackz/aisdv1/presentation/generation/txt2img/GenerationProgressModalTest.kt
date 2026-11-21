package com.shifthackz.aisdv1.presentation.generation.txt2img

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.widget.dialog.progressStatusText
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.model.toGenerationModal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Covers progress units and stage gating without inventing measurements during queue or model preparation. @author Dmitriy Moroz */
class GenerationProgressModalTest {
    @Before
    fun initializeLocalization() {
        Localization.setLanguageCode("en")
    }

    @Test
    fun `given Android MNN progress, preparation stage arrives, expected indeterminate progress`() {
        // Given
        val state = TextToImageState(platform = Platform.ANDROID, mode = ServerSource.LOCAL_MNN)
        val initial = state.progressModal(true) as GenerationModal.Generating
        // When
        val modal = initial.copy(
            nativeStage = NativeGenerationStage.PREPARING,
            status = LocalDiffusionStatus(55, 100)
        )
        // Then
        assertTrue(modal.isOverallProgress)
        assertEquals(null, modal.overallPercentage)
        assertNull(modal.pair)
    }

    @Test
    fun `given Android MNN progress, tokenization stage arrives, expected indeterminate progress`() {
        // Given
        val state = TextToImageState(platform = Platform.ANDROID, mode = ServerSource.LOCAL_MNN)
        val initial = state.progressModal(true) as GenerationModal.Generating
        // When
        val modal = initial.copy(
            nativeStage = NativeGenerationStage.TOKENIZING,
            status = LocalDiffusionStatus(55, 100)
        )
        // Then
        assertTrue(modal.isOverallProgress)
        assertEquals(null, modal.overallPercentage)
        assertNull(modal.pair)
    }

    @Test
    fun `given Android MNN progress, native execution stage arrives, expected overall percentage without sampling counters`() {
        // Given
        val state = TextToImageState(platform = Platform.ANDROID, mode = ServerSource.LOCAL_MNN)
        val initial = state.progressModal(true) as GenerationModal.Generating
        // When
        val modal = initial.copy(
            nativeStage = NativeGenerationStage.RUNNING,
            status = LocalDiffusionStatus(55, 100)
        )
        // Then
        assertTrue(modal.isOverallProgress)
        assertEquals(55, modal.overallPercentage)
        assertNull(modal.pair)
    }

    @Test
    fun `given Android MNN progress, decoding stage arrives, expected indeterminate progress`() {
        // Given
        val state = TextToImageState(platform = Platform.ANDROID, mode = ServerSource.LOCAL_MNN)
        val initial = state.progressModal(true) as GenerationModal.Generating
        // When
        val modal = initial.copy(
            nativeStage = NativeGenerationStage.DECODING,
            status = LocalDiffusionStatus(55, 100)
        )
        // Then
        assertTrue(modal.isOverallProgress)
        assertEquals(null, modal.overallPercentage)
        assertNull(modal.pair)
    }

    @Test
    fun `given Android MNN progress, result writing stage arrives, expected indeterminate progress`() {
        // Given
        val state = TextToImageState(platform = Platform.ANDROID, mode = ServerSource.LOCAL_MNN)
        val initial = state.progressModal(true) as GenerationModal.Generating
        // When
        val modal = initial.copy(
            nativeStage = NativeGenerationStage.WRITING_RESULT,
            status = LocalDiffusionStatus(55, 100)
        )
        // Then
        assertTrue(modal.isOverallProgress)
        assertEquals(null, modal.overallPercentage)
        assertNull(modal.pair)
    }

    @Test
    fun `given Android MNN progress, denoising-labelled overall stage arrives, expected indeterminate progress`() {
        // Given
        val state = TextToImageState(platform = Platform.ANDROID, mode = ServerSource.LOCAL_MNN)
        val initial = state.progressModal(true) as GenerationModal.Generating
        // When
        val modal = initial.copy(
            nativeStage = NativeGenerationStage.DENOISING,
            status = LocalDiffusionStatus(55, 100)
        )
        // Then
        assertTrue(modal.isOverallProgress)
        assertEquals(null, modal.overallPercentage)
        assertNull(modal.pair)
    }

    @Test
    fun `given sampling progress, preparing stage arrives with stale counters, expected no sampling percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeStage = NativeGenerationStage.PREPARING,
            status = LocalDiffusionStatus(12, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given sampling progress, tokenizing stage arrives with stale counters, expected no sampling percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeStage = NativeGenerationStage.TOKENIZING,
            status = LocalDiffusionStatus(12, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given sampling progress, running stage arrives with stale counters, expected no sampling percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeStage = NativeGenerationStage.RUNNING,
            status = LocalDiffusionStatus(12, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given sampling progress, decoding stage arrives with stale counters, expected no sampling percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeStage = NativeGenerationStage.DECODING,
            status = LocalDiffusionStatus(12, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given sampling progress, writing_result stage arrives with stale counters, expected no sampling percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeStage = NativeGenerationStage.WRITING_RESULT,
            status = LocalDiffusionStatus(12, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given sampling status, normal counters are formatted, expected bounded percentage and original step count`() {
        // Given
        val counters = 12 to 20
        // When
        val text = progressStatusText(null, counters, null)
        // Then
        assertEquals(
            Localization.string(
                "communicating_status_steps",
                counters.first,
                counters.second
            ) + " · 60%", text
        )
    }

    @Test
    fun `given sampling status, negative counters are formatted, expected bounded percentage and original step count`() {
        // Given
        val counters = -4 to 20
        // When
        val text = progressStatusText(null, counters, null)
        // Then
        assertEquals(
            Localization.string(
                "communicating_status_steps",
                counters.first,
                counters.second
            ) + " · 0%", text
        )
    }

    @Test
    fun `given sampling status, overshoot counters are formatted, expected bounded percentage and original step count`() {
        // Given
        val counters = 25 to 20
        // When
        val text = progressStatusText(null, counters, null)
        // Then
        assertEquals(
            Localization.string(
                "communicating_status_steps",
                counters.first,
                counters.second
            ) + " · 100%", text
        )
    }

    @Test
    fun `given sampling status, large counters are formatted, expected bounded percentage and original step count`() {
        // Given
        val counters = 2147483647 to 2147483647
        // When
        val text = progressStatusText(null, counters, null)
        // Then
        assertEquals(
            Localization.string(
                "communicating_status_steps",
                counters.first,
                counters.second
            ) + " · 100%", text
        )
    }

    @Test
    fun `given queue status, sampling and overall measurements also exist, expected queue takes precedence`() {
        // Given
        val position = 3
        // When
        val text = progressStatusText(position, 10 to 20, 80)
        // Then
        assertEquals(Localization.string("communicating_status_queue", position), text)
    }

    @Test
    fun `given overall progress, sampling counters also exist, expected percentage uses the overall unit`() {
        // Given
        val overall = 80
        // When
        val text = progressStatusText(null, 10 to 20, overall)
        // Then
        assertEquals("80%", text)
    }

    @Test
    fun `given unknown provider progress, no measurements exist, expected no fabricated status`() {
        // Given
        val modal = GenerationModal.Generating(status = LocalDiffusionStatus(0, 0))
        // When
        val text = progressStatusText(null, modal.pair, modal.overallPercentage)
        // Then
        assertNull(text)
    }

    @Test
    fun `given Easy Diffusion progress, loading has no total, expected cancellable indeterminate modal`() {
        // Given
        val progress = EasyDiffusionProgress(isRunning = true)
        // When
        val modal = progress.toGenerationModal()
        // Then
        assertTrue(modal.canCancel)
        assertNull(modal.pair)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given Easy Diffusion sampling, native count exceeds total, expected bounded original steps with percentage`() {
        // Given
        val progress = EasyDiffusionProgress(step = 30, totalSteps = 20, isRunning = true)
        // When
        val modal = progress.toGenerationModal()
        // Then
        assertEquals(20 to 20, modal.pair)
        assertFalse(modal.isOverallProgress)
        assertTrue(progressStatusText(null, modal.pair, null)!!.endsWith("100%"))
    }

    @Test
    fun `given Easy Diffusion sampling, native count is negative, expected no measurement`() {
        // Given
        val progress = EasyDiffusionProgress(step = -1, totalSteps = 20, isRunning = true)
        // When
        val modal = progress.toGenerationModal()
        // Then
        assertNull(modal.pair)
    }

    @Test
    fun `given ONNX native generation, denoising reports intermediate steps, expected sampling counters and percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeEngine = NativeGenerationEngine.ONNX,
            nativeStage = NativeGenerationStage.DENOISING,
            status = LocalDiffusionStatus(8, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertEquals(8 to 20, counters)
        assertTrue(progressStatusText(null, counters, modal.overallPercentage)!!.endsWith("40%"))
    }

    @Test
    fun `given BONSAI native generation, denoising reports intermediate steps, expected sampling counters and percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeEngine = NativeGenerationEngine.BONSAI,
            nativeStage = NativeGenerationStage.DENOISING,
            status = LocalDiffusionStatus(8, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertEquals(8 to 20, counters)
        assertTrue(progressStatusText(null, counters, modal.overallPercentage)!!.endsWith("40%"))
    }

    @Test
    fun `given MediaPipe generation, initial counters arrive without intermediate updates, expected indeterminate progress`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeEngine = NativeGenerationEngine.MEDIA_PIPE,
            nativeStage = NativeGenerationStage.DENOISING,
            status = LocalDiffusionStatus(0, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given MediaPipe generation, completed counters arrive without intermediate updates, expected indeterminate progress`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeEngine = NativeGenerationEngine.MEDIA_PIPE,
            nativeStage = NativeGenerationStage.DENOISING,
            status = LocalDiffusionStatus(20, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }

    @Test
    fun `given stable diffusion cpp generation, opaque runtime counters arrive, expected no invented sampling percentage`() {
        // Given
        val modal = GenerationModal.Generating(
            nativeEngine = NativeGenerationEngine.SDCPP,
            nativeStage = NativeGenerationStage.RUNNING,
            status = LocalDiffusionStatus(8, 20)
        )
        // When
        val counters = modal.pair
        // Then
        assertNull(counters)
        assertNull(modal.overallPercentage)
    }
}
