package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises fixed-profile admission before dispatching immutable MNN requests.
 *
 * @author Dmitriy Moroz
 */
class MnnTextToImageUseCaseTest {
    private val repository = mockk<MnnGenerationRepository>()
    private val preferences = mockk<PreferenceManager>()
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
    private val useCase = TextToImageUseCaseImpl(
        stableDiffusionGenerationRepository = mockk(), hordeGenerationRepository = mockk(),
        huggingFaceGenerationRepository = mockk(), openAiGenerationRepository = mockk(),
        openAiCompatibleGenerationRepository = mockk(), openAiOAuthGenerationRepository = mockk(),
        stabilityAiGenerationRepository = mockk(), falAiGenerationRepository = mockk(),
        arliAiGenerationRepository = mockk(), localDiffusionGenerationRepository = mockk(),
        swarmUiGenerationRepository = mockk(), mediaPipeGenerationRepository = mockk(),
        stableDiffusionCppGenerationRepository = mockk(), coreMlGenerationRepository = mockk(),
        bonsaiGenerationRepository = mockk(), mnnGenerationRepository = repository,
        preferenceManager = preferences, generateEasyDiffusionUseCase = mockk(),
        captureEasyDiffusionConnectionUseCase = mockk()
    )

    @Test
    fun `given MNN generation, fixed profile is valid, expected one unchanged request`() = runTest {
        // Given
        every { preferences.source } returns ServerSource.LOCAL_MNN
        coEvery { repository.generateFromText(payload) } returns mockAiGenerationResult

        // When
        val result = useCase(payload)

        // Then
        assertEquals(listOf(mockAiGenerationResult), result)
        coVerify(exactly = 1) { repository.generateFromText(payload) }
    }

    @Test
    fun `given MNN generation, provider changed after submission, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.AUTOMATIC1111

            // When
            val failure = runCatching { useCase(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, multiple images is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(batchCount = 2)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, negative prompt is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(negativePrompt = "avoid")

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, unsupported width is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(width = 256)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, unsupported height is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(height = 768)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, unsupported guidance is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(cfgScale = 1f)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, zero steps is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(samplingSteps = 0)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, excessive steps is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(samplingSteps = 51)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, different sampler is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(sampler = "Euler")

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, missing profile is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(mnnProfile = null)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, missing model is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(mnnModelId = null)

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, missing activation is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(mnnActivationId = "")

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, oversized seed is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(seed = "2147483648")

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, negative seed is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(seed = "-2")

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, non numeric seed is submitted, expected rejection before dispatch`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val invalid = payload.copy(seed = "invalid")

            // When
            val failure = runCatching { useCase(invalid) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            coVerify(exactly = 0) { repository.generateFromText(any()) }
        }

    @Test
    fun `given MNN generation, blank seed is submitted, expected resolved bounded seed`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val captured = slot<TextToImagePayload>()
            coEvery { repository.generateFromText(capture(captured)) } returns mockAiGenerationResult

            // When
            useCase(payload.copy(seed = ""))

            // Then
            assertTrue(captured.captured.seed.toLong() in 0..Int.MAX_VALUE.toLong())
            assertEquals(payload.copy(seed = captured.captured.seed), captured.captured)
        }

    @Test
    fun `given MNN generation, random sentinel seed is submitted, expected resolved bounded seed`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val captured = slot<TextToImagePayload>()
            coEvery { repository.generateFromText(capture(captured)) } returns mockAiGenerationResult

            // When
            useCase(payload.copy(seed = "-1"))

            // Then
            assertTrue(captured.captured.seed.toLong() in 0..Int.MAX_VALUE.toLong())
            assertEquals(payload.copy(seed = captured.captured.seed), captured.captured)
        }

    @Test
    fun `given MNN generation, repository cancels the operation, expected same cancellation`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MNN
            val cancellation = CancellationException("cancelled")
            coEvery { repository.generateFromText(any()) } throws cancellation

            // When
            val failure = runCatching { useCase(payload) }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
        }
}
