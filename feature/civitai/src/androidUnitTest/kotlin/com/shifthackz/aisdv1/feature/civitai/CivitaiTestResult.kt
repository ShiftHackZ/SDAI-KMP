package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Saved provider output used by Civitai domain-operation scenarios.
 *
 * @author Dmitriy Moroz
 */
internal val civitaiTestResult = AiGenerationResult(
    id = 12,
    image = "output",
    inputImage = "private input",
    createdAt = 100,
    type = AiGenerationResult.Type.TEXT_TO_IMAGE,
    prompt = "a cat",
    negativePrompt = "blur",
    width = 512,
    height = 768,
    samplingSteps = 20,
    cfgScale = 7f,
    restoreFaces = false,
    sampler = "Euler",
    seed = "42",
    subSeed = "9",
    subSeedStrength = 0f,
    denoisingStrength = 0f,
    hidden = false,
    modelName = "Recorded model",
    imageRevision = "revision",
    providerKey = "local_mnn",
    acceleration = "Metal",
    deviceName = "Original iPhone",
    osVersion = "iOS 27",
    generationDurationMillis = 1234,
)
