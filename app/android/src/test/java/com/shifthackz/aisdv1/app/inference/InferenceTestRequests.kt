package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Creates a complete domain request for host admission and exchange-file contract tests.
 *
 * @param modelPath Readable model directory when exercising file validation.
 * @author Dmitriy Moroz
 */
internal fun bonsaiRequest(modelPath: String = "/models/bonsai") = NativeGenerationRequest(
    engine = NativeGenerationEngine.BONSAI,
    modelPath = modelPath,
    payload = TextToImagePayload(
        prompt = "a bonsai tree",
        negativePrompt = "blur",
        samplingSteps = 4,
        cfgScale = 1.5f,
        width = 128,
        height = 256,
        seed = "42",
        sampler = "Euler",
        bonsaiBackend = BonsaiBackend.CPU,
        nsfw = true,
        restoreFaces = false,
        subSeed = "",
        subSeedStrength = 0f,
        batchCount = 1,
        style = null,
        quality = null,
        openAiModel = null,
        stabilityAiClipGuidance = null,
        stabilityAiStylePreset = null,
    ),
)
