package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Platform decoder implementation.
 *
 * @author Dmitriy Moroz
 */
internal class PlatformGalleryImageDecoder : GalleryImageDecoder {
    /**
     * Samples an admitted encoded image to the requested display edge, or returns null when unavailable.
     *
     * @param base64 Budgeted encoded image.
     * @param maxDimension Display edge.
     * @author Dmitriy Moroz
     */
    override fun decode(base64: String, maxDimension: Int) =
        decodeSampledGalleryImage(base64, maxDimension)
}

/**
 * Platform sampling entry point.
 *
 * @param base64 Budgeted source.
 * @param maxDimension Maximum decoded edge.
 * @author Dmitriy Moroz
 */
internal expect fun decodeSampledGalleryImage(base64: String, maxDimension: Int): ImageBitmap?
