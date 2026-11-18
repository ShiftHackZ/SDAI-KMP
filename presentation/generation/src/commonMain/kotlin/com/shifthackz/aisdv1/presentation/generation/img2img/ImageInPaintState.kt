package com.shifthackz.aisdv1.presentation.generation.img2img

import androidx.compose.runtime.Immutable

/**
 * Mask strokes and request parameters shared by the source form and full inpaint editor.
 *
 * @param strokes Completed strokes retaining their captured canvas coordinate spaces.
 * @param brushSize Current editor brush diameter.
 * @param maskBlur Mask feathering amount sent to the provider.
 * @param maskMode Whether the masked or unmasked area is regenerated.
 * @param maskContent Initial pixel/latent content used in the affected region.
 * @param area Whether generation processes the whole image or a mask-focused area.
 * @param onlyMaskedPaddingPx Context padding around the mask-focused area in pixels.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class ImageInPaintState(
    val strokes: List<InPaintStroke> = emptyList(),
    val brushSize: Int = DEFAULT_IN_PAINT_BRUSH_SIZE,
    val maskBlur: Int = DEFAULT_IN_PAINT_MASK_BLUR,
    val maskMode: MaskMode = MaskMode.InPaintMasked,
    val maskContent: MaskContent = MaskContent.Original,
    val area: Area = Area.WholePicture,
    val onlyMaskedPaddingPx: Int = DEFAULT_IN_PAINT_MASK_PADDING,
) {
    val hasMask: Boolean
        get() = strokes.isNotEmpty()

    enum class MaskMode(val inverse: Int) {
        InPaintMasked(0),
        InPaintNotMasked(1);
    }

    enum class MaskContent(val fill: Int) {
        Fill(0),
        Original(1),
        LatentNoise(2),
        LatentNothing(3);
    }

    enum class Area(val fullRes: Boolean) {
        WholePicture(true),
        OnlyMasked(false);
    }
}

/**
 * A completed mask gesture with enough geometry to scale it into another canvas.
 *
 * @param points Ordered untransformed points captured during the gesture.
 * @param brushSize Brush diameter captured when this stroke was drawn.
 * @param canvasWidth Original capture canvas width in pixels.
 * @param canvasHeight Original capture canvas height in pixels.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class InPaintStroke(

    val points: List<InPaintPoint>,

    val brushSize: Int,

    val canvasWidth: Int,

    val canvasHeight: Int,
)

/**
 * One untransformed point in a mask stroke's original canvas space.
 *
 * @param x Horizontal canvas coordinate in pixels.
 * @param y Vertical canvas coordinate in pixels.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class InPaintPoint(

    val x: Float,

    val y: Float,
)


internal const val IN_PAINT_BRUSH_SIZE_MIN = 1


internal const val IN_PAINT_BRUSH_SIZE_MAX = 60


internal const val IN_PAINT_MASK_BLUR_MIN = 1


internal const val IN_PAINT_MASK_BLUR_MAX = 64


internal const val IN_PAINT_MASK_PADDING_MIN = 0


internal const val IN_PAINT_MASK_PADDING_MAX = 256


internal const val DEFAULT_IN_PAINT_BRUSH_SIZE = 16


internal const val DEFAULT_IN_PAINT_MASK_BLUR = 4


internal const val DEFAULT_IN_PAINT_MASK_PADDING = 32
