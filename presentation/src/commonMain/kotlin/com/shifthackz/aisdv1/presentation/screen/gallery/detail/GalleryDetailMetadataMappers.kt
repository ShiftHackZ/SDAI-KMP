package com.shifthackz.aisdv1.presentation.screen.gallery.detail

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
