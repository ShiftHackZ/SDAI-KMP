package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.ProcessedImage

/**
 * Prepares encoded input through the platform image-processing implementation.
 *
 * @author Dmitriy Moroz
 */
interface ImageProcessingRepository {
    /**
     * Reads encoded-image dimensions without allocating original-size pixels.
     *
     * @param image Base64 original pixels retained unchanged in the result.
     * @return Original pixels with authoritative header dimensions.
     * @author Dmitriy Moroz
     */
    fun read(image: String): ProcessedImage

    /**
     * Proportionally downscales the complete image within the bounds, preserving smaller input bytes.
     * Invalid image, storage and native failures propagate unchanged.
     *
     * @param image Base64 encoded input pixels.
     * @param maxWidth Positive maximum prepared width.
     * @param maxHeight Positive maximum prepared height.
     * @return Prepared bytes and their actual pixel dimensions.
     * @author Dmitriy Moroz
     */
    suspend fun downscale(image: String, maxWidth: Int, maxHeight: Int): ProcessedImage
}
