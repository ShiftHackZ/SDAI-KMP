package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgress
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.CancellationException
import com.shifthackz.aisdv1.domain.mocks.ImmediateGenerationRestoreGate
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase
import io.mockk.slot
import com.shifthackz.aisdv1.domain.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.mocks.mockImageToImagePayload
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ArliAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.FalAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HordeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.HuggingFaceGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StabilityAiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import com.shifthackz.aisdv1.domain.gateway.BenchmarkDeviceProbe
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertNotSame
import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform

/**
 * Verifies provider dispatch using application payloads, including captured Easy Diffusion batches.
 *
 * @author Dmitriy Moroz
 */
class ImageToImageUseCaseImplTest {

    private val stubException = RuntimeException("Unable to generate image.")
    private val stubStableDiffusionGenerationRepository =
        mockk<StableDiffusionGenerationRepository>()
    private val stubSwarmUiGenerationRepository = mockk<SwarmUiGenerationRepository>()
    private val stubHordeGenerationRepository = mockk<HordeGenerationRepository>()
    private val stubHuggingFaceGenerationRepository = mockk<HuggingFaceGenerationRepository>()
    private val stubStabilityAiGenerationRepository = mockk<StabilityAiGenerationRepository>()
    private val stubCoreMlGenerationRepository = mockk<CoreMlGenerationRepository>()
    private val stubFalAiGenerationRepository = mockk<FalAiGenerationRepository>()
    private val stubArliAiGenerationRepository = mockk<ArliAiGenerationRepository>()
    private val stubPreferenceManager = mockk<PreferenceManager>()

    private val generateEasyDiffusion = mockk<GenerateEasyDiffusionUseCase>()
    private val captureEasyDiffusion = mockk<CaptureEasyDiffusionConnectionUseCase>()
    private val progressUpdates = mutableListOf<RemoteGenerationProgress?>()
    private val progressGateway = mockk<RemoteGenerationProgressGateway> {
        every { update(any(), any()) } answers { progressUpdates.add(secondArg()); Unit }
    }

    private val deviceProbe = mockk<BenchmarkDeviceProbe>(relaxed = true)

    private val useCase = ImageToImageUseCaseImpl(
        deviceProbe = deviceProbe,
        trackRemoteGenerationUseCase = TrackRemoteGenerationUseCaseImpl(
            progressGateway,
            mockk(relaxed = true)
        ),
        generationRestoreGate = ImmediateGenerationRestoreGate(),
        generateEasyDiffusionUseCase = generateEasyDiffusion,
        captureEasyDiffusionConnectionUseCase = captureEasyDiffusion,
        stableDiffusionGenerationRepository = stubStableDiffusionGenerationRepository,
        swarmUiGenerationRepository = stubSwarmUiGenerationRepository,
        hordeGenerationRepository = stubHordeGenerationRepository,
        huggingFaceGenerationRepository = stubHuggingFaceGenerationRepository,
        stabilityAiGenerationRepository = stubStabilityAiGenerationRepository,
        coreMlGenerationRepository = stubCoreMlGenerationRepository,
        falAiGenerationRepository = stubFalAiGenerationRepository,
        arliAiGenerationRepository = stubArliAiGenerationRepository,
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
            val payload = mockImageToImagePayload.copy(
                sourceSnapshot = ServerSource.EASY_DIFFUSION, providerSelectionRevision = 7,
                batchCount = 3, seed = "4294967295", base64Image = "AQID", denoisingStrength = 0.5f
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
                    mockImageToImagePayload.copy(
                        sourceSnapshot = ServerSource.EASY_DIFFUSION,
                        providerSelectionRevision = 6,
                        base64Image = "AQID",
                        denoisingStrength = 0.5f
                    )
                )
            }
            // Then
            assertSame(
                stubException,
                generateSequence(actual.exceptionOrNull()) { it.cause }.last()
            )
            coVerify(exactly = 0) { generateEasyDiffusion(any()) }
        }

    @Test
    fun `given source is AUTOMATIC1111, generation is requested, expected stable diffusion generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
            coEvery { stubStableDiffusionGenerationRepository.generateFromImage(any()) } returns listOf(
                mockAiGenerationResult
            )

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given source is SWARM_UI, generation is requested, expected swarm ui generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
            coEvery { stubSwarmUiGenerationRepository.generateFromImage(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubSwarmUiGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given source is HORDE, generation is requested, expected horde generation`() = runTest {
        // Given
        every { stubPreferenceManager.source } returns ServerSource.HORDE
        coEvery { stubHordeGenerationRepository.generateFromImage(any()) } returns mockAiGenerationResult

        // When

        val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

        // Then

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubHordeGenerationRepository.generateFromImage(any()) }
    }

    @Test
    fun `given source is HUGGING_FACE, generation is requested, expected hugging face generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.HUGGING_FACE
            coEvery { stubHuggingFaceGenerationRepository.generateFromImage(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubHuggingFaceGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given source is STABILITY_AI, generation is requested, expected stability ai generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.STABILITY_AI
            coEvery { stubStabilityAiGenerationRepository.generateFromImage(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubStabilityAiGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given source is LOCAL_APPLE_CORE_ML, generation is requested, expected core ml generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_APPLE_CORE_ML
            coEvery { stubCoreMlGenerationRepository.generateFromImage(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubCoreMlGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given source is FAL_AI, generation is requested, expected fal ai generation`() = runTest {
        // Given
        every { stubPreferenceManager.source } returns ServerSource.FAL_AI
        coEvery { stubFalAiGenerationRepository.generateFromImage(any()) } returns listOf(
            mockAiGenerationResult
        )

        // When

        val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

        // Then

        assertEquals(listOf(mockAiGenerationResult), actual)
        coVerify(exactly = 1) { stubFalAiGenerationRepository.generateFromImage(any()) }
    }

    @Test
    fun `given source is ARLI_AI, generation is requested, expected arli ai generation`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.ARLI_AI
            coEvery { stubArliAiGenerationRepository.generateFromImage(any()) } returns listOf(
                mockAiGenerationResult
            )

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 1))

            // Then

            assertEquals(listOf(mockAiGenerationResult), actual)
            coVerify(exactly = 1) { stubArliAiGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given automatic1111 batch count is 10, generation is requested, expected batch generated by repository`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
            coEvery { stubStableDiffusionGenerationRepository.generateFromImage(any()) } returns List(
                10
            ) { mockAiGenerationResult }

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 10))

            // Then

            assertEquals(List(10) { mockAiGenerationResult }, actual)
            coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given non batch source batch count is 10, generation is requested, expected generation repeated 10 times`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.HORDE
            coEvery { stubHordeGenerationRepository.generateFromImage(any()) } returns mockAiGenerationResult

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 10))

            // Then

            assertEquals(List(10) { mockAiGenerationResult }, actual)
            coVerify(exactly = 10) { stubHordeGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given fal ai batch count is 4, generation is requested, expected batch generated by repository`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.FAL_AI
            coEvery { stubFalAiGenerationRepository.generateFromImage(any()) } returns List(4) { mockAiGenerationResult }

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 4))

            // Then

            assertEquals(List(4) { mockAiGenerationResult }, actual)
            coVerify(exactly = 1) { stubFalAiGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given arli ai batch count is 4, generation is requested, expected batch generated by repository`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.ARLI_AI
            coEvery { stubArliAiGenerationRepository.generateFromImage(any()) } returns List(4) { mockAiGenerationResult }

            // When

            val actual = useCase(mockImageToImagePayload.copy(batchCount = 4))

            // Then

            assertEquals(List(4) { mockAiGenerationResult }, actual)
            coVerify(exactly = 1) { stubArliAiGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given generation fails, generation is requested, expected error propagated`() = runTest {
        // Given
        every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
        coEvery { stubStableDiffusionGenerationRepository.generateFromImage(any()) } throws stubException

        // When

        val actual = runCatching { useCase(mockImageToImagePayload.copy(batchCount = 1)) }

        // Then

        assertSame(stubException, generateSequence(actual.exceptionOrNull()) { it.cause }.last())
    }

    @Test
    fun `given unsupported source, generation is requested, expected unsupported error`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.LOCAL_MICROSOFT_ONNX

            // When

            val actual = runCatching { useCase(mockImageToImagePayload.copy(batchCount = 1)) }

            // Then

            assertTrue(actual.exceptionOrNull() is IllegalStateException)
            assertTrue(actual.exceptionOrNull()?.message?.startsWith("Img2Img not yet supported") == true)
        }

    @Test
    fun `given SwarmUI ImageToImage batch, each image reports half workflow completion, expected percentages span the entire batch`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
            coEvery { stubSwarmUiGenerationRepository.generateFromImage(any()) } coAnswers {
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(0.5f)
                mockAiGenerationResult
            }
            val payload =
                mockImageToImagePayload.copy(sourceSnapshot = ServerSource.SWARM_UI, batchCount = 3)
            // When
            val result = useCase(payload)
            // Then
            assertEquals(3, result.size)
            assertEquals(listOf(16, 50, 83), progressUpdates.mapNotNull { it?.percentage })
            assertEquals(null, progressUpdates.last())
            coVerify(exactly = 3) { stubSwarmUiGenerationRepository.generateFromImage(any()) }
        }

    @Test
    fun `given SwarmUI ImageToImage batch, second image fails, expected no third submission and progress cleanup`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
            var submissions = 0
            val failure = IllegalArgumentException("second image failed")
            coEvery { stubSwarmUiGenerationRepository.generateFromImage(any()) } coAnswers {
                submissions++
                if (submissions == 2) throw failure
                currentCoroutineContext()[RemoteGenerationProgressContext]!!.report(1f)
                mockAiGenerationResult
            }
            // When
            val result = runCatching {
                useCase(
                    mockImageToImagePayload.copy(
                        sourceSnapshot = ServerSource.SWARM_UI,
                        batchCount = 3
                    )
                )
            }
            // Then
            assertEquals(2, submissions)
            assertSame(failure, generateSequence(result.exceptionOrNull()) { it.cause }.last())
            assertEquals(null, progressUpdates.last())
        }

    @Test
    fun `given SwarmUI ImageToImage batch, next image is cancelled, expected cancellation without remaining submissions`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.SWARM_UI
            var submissions = 0
            coEvery { stubSwarmUiGenerationRepository.generateFromImage(any()) } coAnswers {
                submissions++
                if (submissions == 2) throw CancellationException("cancelled")
                mockAiGenerationResult
            }
            // When
            val result = runCatching {
                useCase(
                    mockImageToImagePayload.copy(
                        sourceSnapshot = ServerSource.SWARM_UI,
                        batchCount = 3
                    )
                )
            }
            // Then
            assertTrue(result.exceptionOrNull() is CancellationException)
            assertEquals(2, submissions)
            assertEquals(null, progressUpdates.last())
        }

    @Test
    fun `given AUTOMATIC1111 ImageToImage batch, provider accepts the submission, expected shared progress task identity and one provider batch call`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.AUTOMATIC1111
            val identities = mutableListOf<String>()
            coEvery { stubStableDiffusionGenerationRepository.generateFromImage(any()) } coAnswers {
                identities += currentCoroutineContext()[RemoteGenerationProgressContext]!!.taskId
                listOf(mockAiGenerationResult, mockAiGenerationResult)
            }
            // When
            val result = useCase(
                mockImageToImagePayload.copy(
                    sourceSnapshot = ServerSource.AUTOMATIC1111,
                    batchCount = 2
                )
            )
            // Then
            assertEquals(2, result.size)
            assertEquals(1, identities.size)
            assertTrue(identities.single().startsWith("task(sdai-"))
            coVerify(exactly = 1) { stubStableDiffusionGenerationRepository.generateFromImage(any()) }
            assertEquals(null, progressUpdates.last())
        }

    @Test
    fun `given sequential image batch, provider dispatch captures metadata, expected one device snapshot and independent image timing contexts`() =
        runTest {
            // Given
            every { stubPreferenceManager.source } returns ServerSource.HORDE
            val device = mockk<BenchmarkDeviceInfo> {
                every { manufacturer } returns "Google"
                every { model } returns "Pixel 7 Pro"
                every { osVersion } returns "Android 17 (37)"
                every { platform } returns BenchmarkPlatform.ANDROID
            }
            coEvery { deviceProbe.capture() } returns device
            val contexts = mutableListOf<GenerationMetadataContext>()
            coEvery { stubHordeGenerationRepository.generateFromImage(any()) } coAnswers {
                contexts += requireNotNull(currentCoroutineContext()[GenerationMetadataContext])
                mockAiGenerationResult
            }
            // When
            useCase(
                mockImageToImagePayload.copy(
                    sourceSnapshot = ServerSource.HORDE,
                    batchCount = 2
                )
            )
            // Then
            assertEquals(2, contexts.size)
            assertNotSame(contexts[0], contexts[1])
            contexts.forEach { context ->
                assertEquals(ServerSource.HORDE.key, context.providerKey)
                assertEquals("None", context.acceleration)
                assertEquals("Google Pixel 7 Pro", context.deviceName)
                assertEquals("Android 17 (API 37)", context.osVersion)
            }
            coVerify(exactly = 1) { deviceProbe.capture() }
        }

}
