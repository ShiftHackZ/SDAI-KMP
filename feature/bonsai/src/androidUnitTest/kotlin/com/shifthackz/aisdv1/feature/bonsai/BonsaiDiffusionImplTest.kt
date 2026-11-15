package com.shifthackz.aisdv1.feature.bonsai

import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies that the Android host facade delegates without initializing JNI.
 *
 * @author Dmitriy Moroz
 */
class BonsaiDiffusionImplTest {
    private val gateway = mockk<NativeGenerationGateway>()
    private val diffusion = BonsaiDiffusionImpl(gateway)

    @Test
    fun `given backend and safety selections, captured request reaches gateway unchanged`() =
        runTest {
            for (backend in BonsaiBackend.entries) for (isNsfw in listOf(false, true)) {
                val payload = payload().copy(bonsaiBackend = backend, nsfw = isNsfw)
                val request = NativeGenerationRequest(
                    NativeGenerationEngine.BONSAI,
                    payload,
                    "/models/selected-bonsai"
                )
                coEvery { gateway.generate(request) } returns "encoded-image"

                assertEquals("encoded-image", diffusion.process(payload, request.modelPath))

                coVerify(exactly = 1) { gateway.generate(request) }
            }
        }

    @Test
    fun `given child failure, facade propagates it without starting a second attempt`() = runTest {
        val failure = NativeGenerationFailure(NativeGenerationFailure.Category.PROCESS_DIED)
        coEvery { gateway.generate(any()) } throws failure

        assertSame(
            failure,
            runCatching { diffusion.process(payload(), "/models/bonsai") }.exceptionOrNull()
        )
        coVerify(exactly = 1) { gateway.generate(any()) }
    }

    @Test
    fun `given coroutine cancellation, facade preserves cancellation`() = runTest {
        val cancellation = CancellationException("cancelled")
        coEvery { gateway.generate(any()) } throws cancellation

        assertSame(
            cancellation,
            runCatching { diffusion.process(payload(), "/models/bonsai") }.exceptionOrNull()
        )
        coVerify(exactly = 1) { gateway.generate(any()) }
    }

    @Test
    fun `given foreground interrupt, gateway receives unscoped active cancellation`() = runTest {
        coEvery { gateway.cancelActive(null) } returns true

        diffusion.interrupt()

        coVerify(exactly = 1) { gateway.cancelActive(null) }
    }

    @Test
    fun `given status observation, only Bonsai progress is requested`() {
        val status = flowOf(LocalDiffusionStatus(1, 4))
        every { gateway.observeStatus(NativeGenerationEngine.BONSAI) } returns status

        assertSame(status, diffusion.observeStatus())

        verify(exactly = 1) { gateway.observeStatus(NativeGenerationEngine.BONSAI) }
        coVerify(exactly = 0) { gateway.generate(any()) }
    }

    private fun payload() = TextToImagePayload(
        prompt = "a bonsai tree", negativePrompt = "blur", samplingSteps = 4, cfgScale = 1f,
        width = 128, height = 128, restoreFaces = false, seed = "42", subSeed = "",
        subSeedStrength = 0f, sampler = "Euler", nsfw = false, batchCount = 1,
        style = null, quality = null, openAiModel = null,
        stabilityAiClipGuidance = null, stabilityAiStylePreset = null,
    )
}
