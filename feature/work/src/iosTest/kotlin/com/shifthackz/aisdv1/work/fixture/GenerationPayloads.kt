package com.shifthackz.aisdv1.work.fixture

import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Captures ordinary text inputs and a fixed provider revision independently from mutable settings.
 *
 * @param source Selected provider, or null to exercise legacy settings fallback.
 * @param backend Captured MNN execution mode.
 * @author Dmitriy Moroz
 */
internal fun textPayload(
    source: ServerSource? = ServerSource.EASY_DIFFUSION,
    backend: MnnBackend = MnnBackend.CPU
) =
    TextToImagePayload(
        prompt = "cat", negativePrompt = "", samplingSteps = 20, cfgScale = 7.5f,
        width = 512, height = 512, restoreFaces = false, seed = "1", subSeed = "",
        subSeedStrength = 0f, sampler = "Euler", nsfw = false, batchCount = 1,
        style = null, quality = null, openAiModel = null,
        stabilityAiClipGuidance = null, stabilityAiStylePreset = null,
        sourceSnapshot = source, providerSelectionRevision = 7, mnnBackend = backend,
    )

/**
 * Captures an image request so retry and admission assertions can compare its original inputs.
 *
 * @param source Provider whose image route is being scheduled.
 * @author Dmitriy Moroz
 */
internal fun imagePayload(source: ServerSource = ServerSource.EASY_DIFFUSION) =
    ImageToImagePayload(
        base64Image = "input", base64MaskImage = "", denoisingStrength = 0.5f,
        prompt = "cat", negativePrompt = "", samplingSteps = 20, cfgScale = 7.5f,
        width = 512, height = 512, restoreFaces = false, seed = "1", subSeed = "",
        subSeedStrength = 0f, sampler = "Euler", nsfw = false, batchCount = 1,
        inPaintingMaskInvert = 0, inPaintFullResPadding = 0, inPaintingFill = 0,
        inPaintFullRes = false, maskBlur = 0, stabilityAiClipGuidance = null,
        stabilityAiStylePreset = null, sourceSnapshot = source, providerSelectionRevision = 7,
    )
