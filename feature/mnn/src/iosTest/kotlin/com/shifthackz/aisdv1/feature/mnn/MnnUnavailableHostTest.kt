package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.feature.mnn.di.mnnHostRuntimeModule
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Verifies the actual platform graph has a controlled runtime before host injection.
 *
 * @author Dmitriy Moroz
 */
class MnnUnavailableHostTest {
    /**
     * given iOS MNN host graph, Swift host has not been injected, expected explicit unavailable failure without DI resolution failure.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN host graph Swift host has not been injected expected explicit unavailable failure without DI resolution failure`() =
        runTest {
            // Given
            val graph = koinApplication { modules(mnnHostRuntimeModule) }
            try {
                val runtime = graph.koin.get<MnnRuntime>()

                // When
                val failure = runCatching { runtime.generate(request) {} }.exceptionOrNull()

                // Then
                assertTrue(failure is IllegalStateException)
                assertTrue(failure.message.orEmpty().contains("host is unavailable"))
            } finally {
                graph.close()
            }
        }
}

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
