package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Decodes a bounded display image before a full-size bitmap can be allocated.
 *
 * @author Dmitriy Moroz
 */
internal interface GalleryImageDecoder {
    /**
     * Samples an admitted encoded image to the requested display edge, or returns null when unavailable.
     *
     * @param base64 Encoded image within the caller's character budget.
     * @param maxDimension Maximum display edge, clamped by the loader.
     * @author Dmitriy Moroz
     */
    fun decode(base64: String, maxDimension: Int): ImageBitmap?
}
