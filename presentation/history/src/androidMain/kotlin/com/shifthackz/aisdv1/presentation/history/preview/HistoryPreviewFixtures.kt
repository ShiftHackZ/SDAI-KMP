package com.shifthackz.aisdv1.presentation.history.preview

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata

/**
 * Fixed metadata and tiny bundled pixels for history previews without persistent gallery reads.
 *
 * @author Dmitriy Moroz
 */
internal object HistoryPreviewFixtures {
    /**
     * A 32-pixel checkerboard PNG decoded synchronously by the Android preview wrapper.
     *
     * @author Dmitriy Moroz
     */
    const val ImageBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAQklEQVR4nGO4c+IrVpQXugorIlU9w6gFoxYMAQuoZRAu9aMWjFowFCyglkG41I9aMGrBULCAWgbhUj9qwagFQ8ACAP32DHmKhrEjAAAAAElFTkSuQmCC"

    val square = GalleryItemMetadata(
        id = 1,
        imageRevision = "history-preview-1",
        createdAt = 1_700_000_000_000L,
        type = AiGenerationResult.Type.TEXT_TO_IMAGE,
        prompt = "A mountain village beneath a clear starry sky",
        negativePrompt = "",
        width = 512,
        height = 512,
        samplingSteps = 30,
        cfgScale = 7f,
        shouldRestoreFaces = false,
        sampler = "Euler",
        seed = "123456789",
        subSeed = "",
        subSeedStrength = 0f,
        denoisingStrength = 0.75f,
        isHidden = false,
        isLiked = false,
        modelName = "Sample diffusion",
    )

    val portrait =
        square.copy(width = 512, height = 896, prompt = "A watercolor portrait in soft light")
    val landscape =
        square.copy(width = 1024, height = 512, prompt = "A wide coastal panorama at sunset")
    val longPrompt = square.copy(
        prompt = List(5) {
            "A detailed mountain village, winding cobblestone paths, warm window lights and distant snowy peaks."
        }.joinToString(" "),
    )
    val mixedItems = (1..12).map { index ->
        val source = when (index % 3) {
            1 -> square
            2 -> landscape
            else -> portrait
        }
        source.copy(id = index.toLong(), imageRevision = "history-preview-$index")
    }
}
