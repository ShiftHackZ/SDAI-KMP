package com.shifthackz.aisdv1.presentation.gallery.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailState
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailTab

/**
 * Supplies output/source images, metadata variants and detail capability/availability conditions.
 *
 * @author Dmitriy Moroz
 */
internal class GalleryDetailPreviewParameterProvider :
    PreviewParameterProvider<GalleryDetailPreviewCase> {
    private val content = GalleryPreviewFixtures.detail
    private val image = GalleryPreviewFixtures.detailState()
    private val info = GalleryPreviewFixtures.detailState(tab = GalleryDetailTab.INFO)
    private val source = content.copy(
        generationType = AiGenerationResult.Type.IMAGE_TO_IMAGE,
        type = "img2img", inputImageBase64 = GalleryPreviewFixtures.ImageBase64,
    )
    private val cases = listOf(
        GalleryDetailPreviewCase("Initial loading", GalleryDetailState()),
        GalleryDetailPreviewCase("Output image", image),
        GalleryDetailPreviewCase("Portrait image", image, imageWidth = 24, imageHeight = 40),
        GalleryDetailPreviewCase("Landscape image", image, imageWidth = 64, imageHeight = 32),
        GalleryDetailPreviewCase(
            "Hidden output",
            GalleryPreviewFixtures.detailState(content.copy(hidden = true))
        ),
        GalleryDetailPreviewCase(
            "Liked output",
            GalleryPreviewFixtures.detailState(content.copy(liked = true))
        ),
        GalleryDetailPreviewCase(
            "Report action",
            GalleryPreviewFixtures.detailState(content.copy(showReportButton = true))
        ),
        GalleryDetailPreviewCase(
            "Image-to-image output",
            GalleryPreviewFixtures.detailState(source)
        ),
        GalleryDetailPreviewCase(
            "Original source",
            GalleryPreviewFixtures.detailState(source, GalleryDetailTab.ORIGINAL)
        ),
        GalleryDetailPreviewCase("Image pixels loading", image, isImageLoading = true),
        GalleryDetailPreviewCase("Image unavailable", image, isImageUnavailable = true),
        GalleryDetailPreviewCase("Generation metadata", info),
        GalleryDetailPreviewCase(
            "Image-to-image metadata",
            GalleryPreviewFixtures.detailState(source, GalleryDetailTab.INFO)
        ),
        GalleryDetailPreviewCase(
            "Legacy metadata", info.copy(
                content = content.copy(
                    provider = null,
                    acceleration = null,
                    generationDuration = null,
                    deviceName = null,
                    osVersion = null,
                    modelName = "",
                )
            )
        ),
        GalleryDetailPreviewCase(
            "Blank metadata values", info.copy(
                content = content.copy(
                    prompt = "", negativePrompt = "   ", seed = "", subSeed = "", sampler = "",
                )
            ), metadataOffset = 400
        ),
        GalleryDetailPreviewCase(
            "Long prompt and model", info.copy(
                content = content.copy(
                    prompt = "A detailed village, winding cobblestone paths, warm window lights and distant snowy peaks. ".repeat(
                        5
                    ),
                    modelName = "A very long sample diffusion checkpoint name with version details",
                )
            ), metadataOffset = 450
        ),
        GalleryDetailPreviewCase(
            "Civitai link",
            info.copy(content = content.copy(civitaiUrl = "https://civitai.com/posts/123456789"))
        ),
        GalleryDetailPreviewCase(
            "Remote provider metadata", info.copy(
                content = content.copy(
                    provider = "Automatic1111",
                    acceleration = "None",
                    deviceName = null,
                    osVersion = null,
                )
            )
        ),
        GalleryDetailPreviewCase("Lower generation parameters", info, metadataOffset = 1000),
    )

    /**
     * Fixed detail snapshots with inert navigation, copy and mutation callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<GalleryDetailPreviewCase> = cases.asSequence()

    /**
     * Identifies the detail tab, image geometry or metadata condition.
     *
     * @param index Position in the ordered detail-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
