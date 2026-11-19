package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.mocks.ImmediateGenerationRestoreGate
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase
import io.mockk.slot
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.mocks.mockTextToImagePayload
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.OpenAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies provider dispatch using application payloads, including captured Easy Diffusion batches.
 *
 * @author Dmitriy Moroz
 */
class TextToImageUseCaseImplTest {

    private val stubException = RuntimeException("Unable to generate image.")
    private val stubStableDiffusionGenerationRepository =
        mockk<StableDiffusionGenerationRepository>()
    private val stubHordeGenerationRepository = mockk<HordeGenerationRepository>()
    private val stubHuggingFaceGenerationRepository = mockk<HuggingFaceGenerationRepository>()
    private val stubOpenAiGenerationRepository = mockk<OpenAiGenerationRepository>()
    private val stubStabilityAiGenerationRepository = mockk<StabilityAiGenerationRepository>()
    private val stubFalAiGenerationRepository = mockk<FalAiGenerationRepository>()
    private val stubArliAiGenerationRepository = mockk<ArliAiGenerationRepository>()
    private val stubSwarmUiGenerationRepository = mockk<SwarmUiGenerationRepository>()
    private val stubLocalDiffusionGenerationRepository = mockk<LocalDiffusionGenerationRepository>()
    private val stubMediaPipeGenerationRepository = mockk<MediaPipeGenerationRepository>()
    private val stubStableDiffusionCppGenerationRepository =
        mockk<StableDiffusionCppGenerationRepository>()
    private val stubCoreMlGenerationRepository = mockk<CoreMlGenerationRepository>()
    private val stubBonsaiGenerationRepository = mockk<BonsaiGenerationRepository>()
    private val stubPreferenceManager = mockk<PreferenceManager>()

    private val generateEasyDiffusion = mockk<GenerateEasyDiffusionUseCase>()
    private val captureEasyDiffusion = mockk<CaptureEasyDiffusionConnectionUseCase>()
    private val useCase = TextToImageUseCaseImpl(
        generationRestoreGate = ImmediateGenerationRestoreGate(),
        generateEasyDiffusionUseCase = generateEasyDiffusion,
        captureEasyDiffusionConnectionUseCase = captureEasyDiffusion,
        stableDiffusionGenerationRepository = stubStableDiffusionGenerationRepository,
        hordeGenerationRepository = stubHordeGenerationRepository,
        huggingFaceGenerationRepository = stubHuggingFaceGenerationRepository,
        openAiGenerationRepository = stubOpenAiGenerationRepository,
        openAiCompatibleGenerationRepository = mockk(),
        openAiOAuthGenerationRepository = mockk(),
        stabilityAiGenerationRepository = stubStabilityAiGenerationRepository,
        falAiGenerationRepository = stubFalAiGenerationRepository,
        arliAiGenerationRepository = stubArliAiGenerationRepository,
        localDiffusionGenerationRepository = stubLocalDiffusionGenerationRepository,
        swarmUiGenerationRepository = stubSwarmUiGenerationRepository,
        mediaPipeGenerationRepository = stubMediaPipeGenerationRepository,
        stableDiffusionCppGenerationRepository = stubStableDiffusionCppGenerationRepository,
        coreMlGenerationRepository = stubCoreMlGenerationRepository,
        bonsaiGenerationRepository = stubBonsaiGenerationRepository,
        mnnGenerationRepository = mockk(),
        preferenceManager = stubPreferenceManager,
    )

    @Test
    fun `given Easy Diffusion generation, complete batch is submitted, generation is requested, expected one immutable connection capture`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.EASY_DIFFUSION
            val connection = EasyDiffusionConnection("https://server.example", "folder/model", 7)
            coEvery { captureEasyDiffusion(7, null) } returns connection
            val request = slot<EasyDiffusionRequest>()
            val results = List(3) { mockAiGenerationResult }
            coEvery { generateEasyDiffusion(capture(request)) } returns results
            val payload = mockTextToImagePayload.copy(
                sourceSnapshot = ServerSource.EASY_DIFFUSION, providerSelectionRevision = 7,
                batchCount = 3, seed = "4294967295"
            )
            // When
            val actual = useCase(payload)
            // Then
            assertSame(results, actual)
            assertEquals(connection.url, request.captured.url)
            assertEquals(connection.model, request.captured.model)
            assertEquals(7L, request.captured.selectionRevision)
            assertEquals(3, request.captured.batchCount)
            coVerify(exactly = 1) { captureEasyDiffusion(7, null) }
            coVerify(exactly = 1) { generateEasyDiffusion(any()) }
        }

    @Test
    fun `given Easy Diffusion generation, captured connection is stale, generation is requested, expected no generation dispatch`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.EASY_DIFFUSION
            coEvery { captureEasyDiffusion(6, null) } throws stubException
            // When
            val actual = runCatching {
                useCase(
                    mockTextToImagePayload.copy(
                        sourceSnapshot = ServerSource.EASY_DIFFUSION,
                        providerSelectionRevision = 6
                    )
                )
            }
            // Then
            assertSame(stubException, actual.exceptionOrNull())
            coVerify(exactly = 0) { generateEasyDiffusion(any()) }
        }

    @Test
    fun `given source is AUTOMATIC1111, generation is requested, expected stable diffusion generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
            coEvery { stubStableDiffusionGenerationRepository.generateFromText(any()) } returns listOf(
                mockAiGenerationResult
            )

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is HORDE, generation is requested, expected horde generation`() = runTest {
        // Given
        every { stubPreferenceManager.source } returns ServerSource.HORDE
        coEvery { stubHordeGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

        // When

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        // Then

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubHordeGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is HUGGING_FACE, generation is requested, expected hugging face generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.HUGGING_FACE
            coEvery { stubHuggingFaceGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubHuggingFaceGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is OPEN_AI, generation is requested, expected open ai generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.OPEN_AI
            coEvery { stubOpenAiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubOpenAiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is STABILITY_AI, generation is requested, expected stability ai generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.STABILITY_AI
            coEvery { stubStabilityAiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubStabilityAiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is FAL_AI, generation is requested, expected fal ai generation`() = runTest {
        // Given
        every { stubPreferenceManager.source } returns ServerSource.FAL_AI
        coEvery { stubFalAiGenerationRepository.generateFromText(any()) } returns listOf(
            mockAiGenerationResult
        )

        // When

        val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

        // Then

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubFalAiGenerationRepository.generateFromText(any()) }
    }

    @Test
    fun `given source is ARLI_AI, generation is requested, expected arli ai generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.ARLI_AI
            coEvery { stubArliAiGenerationRepository.generateFromText(any()) } returns listOf(
                mockAiGenerationResult
            )

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubArliAiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is SWARM_UI, generation is requested, expected swarm ui generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
            coEvery { stubSwarmUiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubSwarmUiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is LOCAL_MICROSOFT_ONNX, generation is requested, expected local diffusion generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_MICROSOFT_ONNX
            coEvery { stubLocalDiffusionGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubLocalDiffusionGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is LOCAL_GOOGLE_MEDIA_PIPE, generation is requested, expected media pipe generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_GOOGLE_MEDIA_PIPE
            coEvery { stubMediaPipeGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubMediaPipeGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is LOCAL_STABLE_DIFFUSION_CPP, generation is requested, expected stable diffusion cpp generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_STABLE_DIFFUSION_CPP
            coEvery { stubStableDiffusionCppGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubStableDiffusionCppGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is LOCAL_APPLE_CORE_ML, generation is requested, expected core ml generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_APPLE_CORE_ML
            coEvery { stubCoreMlGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubCoreMlGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given source is LOCAL_APPLE_BONSAI, generation is requested, expected bonsai generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_APPLE_BONSAI
            coEvery { stubBonsaiGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubBonsaiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given automatic1111 batch count is 10, generation is requested, expected batch generated by repository`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
            coEvery { stubStableDiffusionGenerationRepository.generateFromText(any()) } returns List(
                10
            ) { mockAiGenerationResult }

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 10))

            // Then

            assertEquals(List(10) { mockAiGenerationResult }, actual)
            coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given non batch source batch count is 10, generation is requested, expected generation repeated 10 times`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.HORDE
            coEvery { stubHordeGenerationRepository.generateFromText(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 10))

            // Then

            assertEquals(List(10) { mockAiGenerationResult }, actual)
            coVerify(exactly = 10) { stubHordeGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given fal ai batch count is 4, generation is requested, expected batch generated by repository`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.FAL_AI
            coEvery { stubFalAiGenerationRepository.generateFromText(any()) } returns List(4) { mockAiGenerationResult }

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 4))

            // Then

            assertEquals(List(4) { mockAiGenerationResult }, actual)
            coVerify(exactly = 1) { stubFalAiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given arli ai batch count is 4, generation is requested, expected batch generated by repository`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.ARLI_AI
            coEvery { stubArliAiGenerationRepository.generateFromText(any()) } returns List(4) { mockAiGenerationResult }

            // When

            val actual = useCase(mockTextToImagePayload.copy(batchCount = 4))

            // Then

            assertEquals(List(4) { mockAiGenerationResult }, actual)
            coVerify(exactly = 1) { stubArliAiGenerationRepository.generateFromText(any()) }
        }

    @Test
    fun `given generation fails, generation is requested, expected error propagated`() = runTest {
        // Given
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery { stubStableDiffusionGenerationRepository.generateFromText(any()) } throws stubException

        // When

        val actual = runCatching { useCase(mockTextToImagePayload.copy(batchCount = 1)) }

        // Then

        assertSame(stubException, actual.exceptionOrNull())
    }
}
