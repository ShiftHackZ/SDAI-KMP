package com.shifthackz.aisdv1.presentation.gallery.preview

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailContent
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailState
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailTab
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryGridItemUi
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryState

/**
 * Supplies fixed gallery metadata and tiny bundled checkerboard pixels without persistent reads.
 *
 * @author Dmitriy Moroz
 */
internal object GalleryPreviewFixtures {
    /**
     * A 32-pixel checkerboard PNG decoded synchronously by the Android preview wrapper.
     *
     * @author Dmitriy Moroz
     */
    const val ImageBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAIAAAD8GO2jAAAAQklEQVR4nGO4c+IrVpQXugorIlU9w6gFoxYMAQuoZRAu9aMWjFowFCyglkG41I9aMGrBULCAWgbhUj9qwagFQ8ACAP32DHmKhrEjAAAAAElFTkSuQmCC"

    val items = (1L..30L).map { id ->
        GalleryGridItemUi(
            id = id,
            imageRevision = "gallery-preview-$id",
            hidden = id % 5 == 0L,
            liked = id % 3 == 0L
        )
    }
    val grid = GalleryState(loading = false, items = items, itemIds = items.map { it.id })
    val detail = GalleryDetailContent(
        generationType = AiGenerationResult.Type.TEXT_TO_IMAGE,
        id = 1L,
        imageBase64 = "",
        imageRevision = "gallery-preview-1",
        image = null,
        inputImageBase64 = null,
        inputImage = null,
        createdAt = "2026-11-23 12:30",
        type = "txt2img",
        modelName = "Sample diffusion model",
        provider = "MNN",
        acceleration = "CPU",
        generationDuration = "00:42",
        deviceName = "Sample phone",
        osVersion = "Android 16 (API 36)",
        prompt = "A quiet mountain village at sunset, watercolor, warm window lights",
        negativePrompt = "low quality, blurry",
        size = "512 X 512",
        samplingSteps = "20",
        cfgScale = "7.0",
        restoreFaces = "No",
        sampler = "Euler",
        seed = "123456789",
        subSeed = "",
        subSeedStrength = "0.0",
        denoisingStrength = "0.75",
        hidden = false,
    )

    /**
     * Creates a consistent current-item detail snapshot for an output, original or metadata tab.
     *
     * @param content Metadata for the current sample generation.
     * @param tab Active detail tab, with available tabs derived from the generation type.
     * @author Dmitriy Moroz
     */
    fun detailState(
        content: GalleryDetailContent = detail,
        tab: GalleryDetailTab = GalleryDetailTab.IMAGE,
    ) = GalleryDetailState(
        loading = false,
        tabs = GalleryDetailTab.consume(content.generationType),
        selectedTab = tab,
        galleryItemIds = listOf(content.id),
        content = content,
        pagerContents = listOf(content),
    )
}
