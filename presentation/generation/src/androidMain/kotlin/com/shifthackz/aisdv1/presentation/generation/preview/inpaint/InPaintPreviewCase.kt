package com.shifthackz.aisdv1.presentation.generation.preview.inpaint

import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintState

/**
 * Captured mask and initial editor viewport/disclosure configuration.
 *
 * @param name Identifies the brush, viewport or mask-parameter condition.
 * @param state Mask strokes and generation parameters to display.
 * @param shouldShowAdjustments Whether the initial tab displays mask request parameters.
 * @param isDrawing Whether the initial Draw tab edits strokes instead of transforming the viewport.
 * @param zoom Initial image viewport scale.
 * @param adjustmentOffset Initial mask-parameter scroll offset in pixels.
 * @author Dmitriy Moroz
 */
internal data class InPaintPreviewCase(
    val name: String,
    val state: ImageInPaintState,
    val shouldShowAdjustments: Boolean = false,
    val isDrawing: Boolean = true,
    val zoom: Float = 1f,
    val adjustmentOffset: Int = 0,
)
