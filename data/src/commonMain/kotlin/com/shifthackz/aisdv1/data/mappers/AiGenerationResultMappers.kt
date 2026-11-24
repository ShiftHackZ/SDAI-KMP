package com.shifthackz.aisdv1.data.mappers

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.AiGenerationResultPreview
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultPreviewEntity

//region DOMAIN --> ENTITY
/**
 * Converts completed gallery results to storage rows, preserving provenance and omitting transient account guards.
 *
 * @return Storage rows in the original order.
 * @author Dmitriy Moroz
 */
fun List<AiGenerationResult>.mapDomainToEntity(): List<GenerationResultEntity> =
    map(AiGenerationResult::mapDomainToEntity)

/**
 * Converts completed gallery results to storage rows, preserving provenance and omitting transient account guards.
 *
 * @author Dmitriy Moroz
 */
fun AiGenerationResult.mapDomainToEntity(): GenerationResultEntity = with(this) {
    GenerationResultEntity(
        id = id,
        imageBase64 = image,
        originalImageBase64 = inputImage,
        createdAt = createdAt,
        generationType = type.key,
        prompt = prompt,
        negativePrompt = negativePrompt,
        width = width,
        height = height,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        restoreFaces = restoreFaces,
        sampler = sampler,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength,
        denoisingStrength = denoisingStrength,
        hidden = hidden,
        liked = liked,
        modelName = modelName,
        providerKey = providerKey,
        acceleration = acceleration,
        generationDurationMillis = generationDurationMillis,
        deviceName = deviceName,
        osVersion = osVersion,
        imageRevision = imageRevision,
        civitaiUrl = civitaiUrl,
        upscalerModelName = upscalerModelName,
    )
}
//endregion

//region ENTITY --> DOMAIN
/**
 * Restores saved image parameters and provenance without recreating transient account guards.
 *
 * @return Gallery results in database query order.
 * @author Dmitriy Moroz
 */
fun List<GenerationResultEntity>.mapEntityToDomain(): List<AiGenerationResult> =
    map(GenerationResultEntity::mapEntityToDomain)

/**
 * Restores saved image parameters and provenance without recreating transient account guards.
 *
 * @author Dmitriy Moroz
 */
fun GenerationResultEntity.mapEntityToDomain(): AiGenerationResult = with(this) {
    AiGenerationResult(
        id = id,
        image = imageBase64,
        inputImage = originalImageBase64,
        createdAt = createdAt,
        type = AiGenerationResult.Type.parse(generationType),
        prompt = prompt,
        negativePrompt = negativePrompt,
        width = width,
        height = height,
        samplingSteps = samplingSteps,
        cfgScale = cfgScale,
        restoreFaces = restoreFaces,
        sampler = sampler,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength,
        denoisingStrength = denoisingStrength,
        hidden = hidden,
        liked = liked,
        modelName = modelName,
        providerKey = providerKey,
        acceleration = acceleration,
        generationDurationMillis = generationDurationMillis,
        deviceName = deviceName,
        osVersion = osVersion,
        imageRevision = imageRevision,
        civitaiUrl = civitaiUrl,
        upscalerModelName = upscalerModelName,
    )
}

/**
 * Builds lightweight gallery previews from identity and flags without loading image payloads.
 *
 * @return Preview identities and flags in query order.
 * @author Dmitriy Moroz
 */
fun List<GenerationResultPreviewEntity>.mapPreviewEntityToDomain(): List<AiGenerationResultPreview> =
    map(GenerationResultPreviewEntity::mapPreviewEntityToDomain)

/**
 * Builds lightweight gallery previews from identity and flags without loading image payloads.
 *
 * @author Dmitriy Moroz
 */
fun GenerationResultPreviewEntity.mapPreviewEntityToDomain(): AiGenerationResultPreview =
    with(this) {
        AiGenerationResultPreview(
            id = id,
            imageRevision = imageRevision,
            hidden = hidden,
            liked = liked,
        )
    }
//endregion
