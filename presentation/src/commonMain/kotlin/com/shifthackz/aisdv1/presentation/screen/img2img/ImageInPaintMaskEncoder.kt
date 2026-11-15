package com.shifthackz.aisdv1.presentation.screen.img2img

import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageBudget

/**
 * Builds a PNG mask at the original image dimensions without resizing the generation input.
 * White strokes are scaled from their recorded canvas coordinates onto a transparent raster.
 * The caller dispatches this explicit transform off main.
 *
 * @param imageBase64 Original encoded image used to establish exact output dimensions.
 * @param strokes Brush paths and the canvas dimensions where their points were recorded.
 * @param budget Encoded/pixel admission before allocating the mask.
 * @return Base64 PNG mask, or null for invalid content or a rejected admission limit.
 * @author Dmitriy Moroz
 */
internal expect suspend fun encodeInPaintMaskBase64(
    imageBase64: String,
    strokes: List<InPaintStroke>,
    budget: OriginalImageBudget,
): String?
