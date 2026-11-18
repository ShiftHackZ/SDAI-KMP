package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies request admission and lifecycle reporting before JNI or hardware is involved.
 *
 * @author Dmitriy Moroz
 */
class MnnInferenceEngineTest {
    private val bridge = mockk<MnnRuntime>()
    private val engine = MnnInferenceEngine(bridge)
    private val request = NativeGenerationRequest(
        NativeGenerationEngine.MNN,
        TextToImagePayload(
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
        ),
        "/owned/mnn_sd15"
    )

    @Test
    fun `given Android MNN engine, Metal request arrives, expected rejection before native admission`() =
        runTest {
            // Given
            val invalid =
                request.copy(payload = request.payload.copy(mnnBackend = MnnBackend.METAL))
            val stages = mutableListOf<NativeGenerationStage>()

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, supported request completes, expected lifecycle and opaque percentage progress`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val progress = mutableListOf<LocalDiffusionStatus>()
            val output = byteArrayOf(1, 2, 3)
            coEvery {
                bridge.generate(
                    request,
                    any()
                )
            } answers { secondArg<(Int) -> Unit>()(37); output }

            // When
            val actual = engine.generate(request, progress::add, stages::add)

            // Then
            assertArrayEquals(output, actual)
            assertEquals(
                listOf(NativeGenerationStage.PREPARING, NativeGenerationStage.RUNNING),
                stages
            )
            assertEquals(listOf(LocalDiffusionStatus(37, 100)), progress)
        }

    @Test
    fun `given MNN engine, native boundary cancels, expected original cancellation`() = runTest {
        // Given
        val cancellation = CancellationException("cancel")
        coEvery { bridge.generate(any(), any()) } throws cancellation

        // When
        val failure = runCatching { engine.generate(request, {}, {}) }.exceptionOrNull()

        // Then
        assertSame(cancellation, failure)
    }

    @Test
    fun `given MNN engine, interruption is requested, expected bridge interruption and harmless close`() {
        // Given
        coEvery { bridge.interrupt() } returns Unit

        // When
        engine.interrupt()
        engine.close()

        // Then
        coVerify(exactly = 1) { bridge.interrupt() }
        coVerify(exactly = 0) { bridge.generate(any(), any()) }
    }

    @Test
    fun `given MNN engine, unresolved seed arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(seed = "-1"))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, seed overflow arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(seed = "2147483648"))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, unsupported dimensions arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(width = 256))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, nonfixed guidance arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(cfgScale = 3f))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, invalid steps arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(samplingSteps = 51))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, unsupported sampler arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(sampler = "Euler"))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, negative conditioning arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(negativePrompt = "avoid"))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, batch request arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(batchCount = 2))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }

    @Test
    fun `given MNN engine, wrong profile arrives, expected rejection before lifecycle and native call`() =
        runTest {
            // Given
            val stages = mutableListOf<NativeGenerationStage>()
            val invalid = request.copy(payload = request.payload.copy(mnnProfile = "other"))

            // When
            val failure =
                runCatching { engine.generate(invalid, {}, stages::add) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertTrue(stages.isEmpty())
            coVerify(exactly = 0) { bridge.generate(any(), any()) }
        }
}
