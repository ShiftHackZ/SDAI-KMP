package com.shifthackz.aisdv1.data.export

/**
 * Identifies supported export formats from encoded bytes rather than provider labels or filenames.
 * Recognition checks the format signature; it does not validate the complete image file.
 *
 * @author Dmitriy Moroz
 */
internal interface MediaStoreImageFormatResolver {

    /**
     * Recognizes PNG, JPEG or WebP without decoding or transforming the image.
     *
     * @param content Original image file bytes, including the format header.
     * @return The matching filename extension and MIME type.
     * @throws IllegalArgumentException When the header is absent, too short or unsupported.
     * @author Dmitriy Moroz
     */
    fun resolve(content: ByteArray): MediaStoreImageFormat
}
