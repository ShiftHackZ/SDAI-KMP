package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultMetadataEntity

/**
 * Maps parameters without introducing incomplete original-image models.
 *
 * @receiver SQLite metadata projection.
 * @author Dmitriy Moroz
 */
internal fun GenerationResultMetadataEntity.mapMetadataToDomain() = GalleryItemMetadata(
    id = id,
    imageRevision = imageRevision,
    createdAt = createdAt,
    type = AiGenerationResult.Type.parse(generationType),
    prompt = prompt,
    negativePrompt = negativePrompt,
    width = width,
    height = height,
    samplingSteps = samplingSteps,
    cfgScale = cfgScale,
    shouldRestoreFaces = shouldRestoreFaces,
    sampler = sampler,
    seed = seed,
    subSeed = subSeed,
    subSeedStrength = subSeedStrength,
    denoisingStrength = denoisingStrength,
    isHidden = isHidden,
    isLiked = isLiked,
    modelName = modelName,
    providerKey = providerKey,
    acceleration = acceleration,
    generationDurationMillis = generationDurationMillis,
    civitaiUrl = civitaiUrl,
    deviceName = deviceName,
    osVersion = osVersion,
)
