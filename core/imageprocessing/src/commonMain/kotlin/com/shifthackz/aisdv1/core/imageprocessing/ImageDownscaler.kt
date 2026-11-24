package com.shifthackz.aisdv1.core.imageprocessing

/**
 * Fits encoded images within pixel bounds while preserving their complete composition and aspect ratio.
 *
 * @author Dmitriy Moroz
 */
interface ImageDownscaler {
    /**
     * Preserves smaller images byte-for-byte and proportionally reduces larger images without cropping.
     * Invalid images and native decoding/encoding failures propagate to the caller.
     *
     * @param image Encoded source bytes, in their stored pixel orientation.
     * @param maxWidth Positive maximum output width in pixels.
     * @param maxHeight Positive maximum output height in pixels.
     * @return Original encoded bytes or a PNG containing the complete downscaled image.
     * @author Dmitriy Moroz
     */
    fun downscale(image: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray
}
