package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.core.localization.formatter.formatGenerationDuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.shortDisplayName
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata

/**
 * Maps persisted parameters; display images are loaded by revision outside UI metadata state.
 *
 * @receiver Persisted metadata without originals.
 * @param shouldShowReportButton Whether the report action is available.
 * @author Dmitriy Moroz
 */
internal fun GalleryItemMetadata.toGalleryDetailContent(
    shouldShowReportButton: Boolean,
): GalleryDetailContent =
    GalleryDetailContent(
        showReportButton = shouldShowReportButton,
        generationType = type,
        id = id,
        imageBase64 = "",
        imageRevision = imageRevision,
        image = null,
        inputImageBase64 = "".takeIf { type == AiGenerationResult.Type.IMAGE_TO_IMAGE },
        inputImage = null,
        createdAt = formatGalleryCreatedAt(createdAt),
        type = type.key,
        modelName = modelName,
        provider = providerKey?.let { key ->
            ServerSource.entries.find { it.key == key }?.shortDisplayName() ?: key
        },
        acceleration = acceleration?.let { if (it == "None") Localization.string("gallery_info_acceleration_none") else it },
        generationDuration = generationDurationMillis?.let(::formatGenerationDuration),
        deviceName = deviceName,
        osVersion = osVersion,
        prompt = prompt,
        negativePrompt = negativePrompt,
        size = "$width X $height",
        samplingSteps = samplingSteps.toString(),
        cfgScale = cfgScale.toString(),
        restoreFaces = Localization.string(if (shouldRestoreFaces) "yes" else "no"),
        sampler = sampler,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength.toString(),
        denoisingStrength = denoisingStrength.toString(),
        hidden = isHidden,
        liked = isLiked,
    )
