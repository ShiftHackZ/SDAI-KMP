package com.shifthackz.aisdv1.presentation.generation.preview.inpaint

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintState

/**
 * Supplies mask editing, brush bounds, viewport modes and every mask-content/mode/area selection.
 *
 * @author Dmitriy Moroz
 */
internal class InPaintPreviewParameterProvider : PreviewParameterProvider<InPaintPreviewCase> {
    private val mask = GenerationPreviewFixtures.mask
    private val cases = listOf(
        InPaintPreviewCase("Draw · Empty mask", ImageInPaintState()),
        InPaintPreviewCase("Draw · Existing mask", mask),
        InPaintPreviewCase("Draw · Small brush", mask.copy(brushSize = 1)),
        InPaintPreviewCase("Draw · Large brush", mask.copy(brushSize = 60)),
        InPaintPreviewCase("Viewport transform", mask, isDrawing = false),
        InPaintPreviewCase("Zoomed viewport", mask, isDrawing = false, zoom = 2f),
        InPaintPreviewCase(
            "Adjust · Minimum blur and padding",
            mask.copy(maskBlur = 1, onlyMaskedPaddingPx = 0),
            shouldShowAdjustments = true
        ),
        InPaintPreviewCase(
            "Adjust · Maximum blur and padding",
            mask.copy(maskBlur = 64, onlyMaskedPaddingPx = 256),
            shouldShowAdjustments = true
        ),
        InPaintPreviewCase(
            "Adjust · Minimum padding",
            mask.copy(onlyMaskedPaddingPx = 0),
            shouldShowAdjustments = true,
            adjustmentOffset = 450
        ),
        InPaintPreviewCase(
            "Adjust · Maximum padding",
            mask.copy(onlyMaskedPaddingPx = 256),
            shouldShowAdjustments = true,
            adjustmentOffset = 450
        ),
    ) + ImageInPaintState.MaskMode.entries.map {
        InPaintPreviewCase(
            "Mask mode · ${it.name}",
            mask.copy(maskMode = it),
            shouldShowAdjustments = true
        )
    } + ImageInPaintState.MaskContent.entries.map {
        InPaintPreviewCase(
            "Mask content · ${it.name}",
            mask.copy(maskContent = it),
            shouldShowAdjustments = true,
            adjustmentOffset = 160,
        )
    } + ImageInPaintState.Area.entries.map {
        InPaintPreviewCase(
            "Mask area · ${it.name}",
            mask.copy(area = it),
            shouldShowAdjustments = true,
            adjustmentOffset = 350,
        )
    }

    /**
     * Fixed editor snapshots with inert drawing, encoding, parameter and navigation intents.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<InPaintPreviewCase> = cases.asSequence()

    /**
     * Identifies the active editor section and mask or viewport setting.
     *
     * @param index Position in the ordered inpaint-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
