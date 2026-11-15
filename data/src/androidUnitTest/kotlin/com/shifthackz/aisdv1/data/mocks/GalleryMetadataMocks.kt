package com.shifthackz.aisdv1.data.mocks

import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata

/**
 * Image-free parameter fixture for repository contracts.
 *
 * @author Dmitriy Moroz
 */
val mockGalleryItemMetadata = with(mockAiGenerationResult) {
    GalleryItemMetadata(
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
}
