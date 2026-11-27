package com.shifthackz.aisdv1.core.imageprocessing

/**
 * Uses bounded native decoding and proportional resampling behind the shared encoded-image contract.
 *
 * @author Dmitriy Moroz
 */
internal expect class PlatformImageDownscaler() : ImageDownscaler {
    /**
     * Fits the entire source within the requested bounds without enlarging smaller images.
     *
     * @param image Original encoded bytes.
     * @param maxWidth Positive maximum output width.
     * @param maxHeight Positive maximum output height.
     * @author Dmitriy Moroz
     */
    override fun downscale(image: ByteArray, maxWidth: Int, maxHeight: Int): ByteArray
}
