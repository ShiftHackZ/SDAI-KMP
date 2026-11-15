package com.shifthackz.aisdv1.presentation.mocks

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.presentation.widget.image.GalleryMemoryBudget

/**
 * Shared parameter fixture mirroring the explicit production budget configuration.
 *
 * @author Dmitriy Moroz
 */
val mockGalleryMemoryBudget = GalleryMemoryBudget(
    32,
    24L * 1024 * 1024,
    2,
    32 * 1024 * 1024,
    2048,
    3,
    2L * 1024 * 1024,
    120,
    30
)

/**
 * Converts existing result fixtures to the image-free projection.
 *
 * @receiver Existing test result.
 * @author Dmitriy Moroz
 */
fun AiGenerationResult.toGalleryMetadata() = GalleryItemMetadata(
    id = id,
    imageRevision = imageRevision,
    createdAt = createdAt,
    type = type,
    prompt = prompt,
    negativePrompt = negativePrompt,
    width = width,
    height = height,
    samplingSteps = samplingSteps,
    cfgScale = cfgScale,
    shouldRestoreFaces = restoreFaces,
    sampler = sampler,
    seed = seed,
    subSeed = subSeed,
    subSeedStrength = subSeedStrength,
    denoisingStrength = denoisingStrength,
    isHidden = hidden,
    isLiked = liked,
    modelName = modelName,
)
