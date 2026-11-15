package com.shifthackz.aisdv1.data.export

/**
 * Recognizes complete PNG signatures, JPEG start markers and RIFF/WebP container markers.
 * Unknown bytes fail before an export destination can be created.
 *
 * @author Dmitriy Moroz
 */
internal class MediaStoreImageFormatResolverImpl : MediaStoreImageFormatResolver {

    /**
     * Checks the encoded header while leaving all pixels and metadata bytes untouched.
     *
     * @param content Original image file bytes to classify.
     * @return The supported format represented by the header.
     * @throws IllegalArgumentException When no supported format signature matches.
     * @author Dmitriy Moroz
     */
    override fun resolve(content: ByteArray): MediaStoreImageFormat = when {
        content.size >= 8 &&
            content[0] == 0x89.toByte() && content[1] == 0x50.toByte() &&
            content[2] == 0x4e.toByte() && content[3] == 0x47.toByte() &&
            content[4] == 0x0d.toByte() && content[5] == 0x0a.toByte() &&
            content[6] == 0x1a.toByte() && content[7] == 0x0a.toByte() -> MediaStoreImageFormat.PNG

        content.size >= 3 && content[0] == 0xff.toByte() &&
            content[1] == 0xd8.toByte() && content[2] == 0xff.toByte() -> MediaStoreImageFormat.JPEG

        content.size >= 12 &&
            content[0] == 'R'.code.toByte() && content[1] == 'I'.code.toByte() &&
            content[2] == 'F'.code.toByte() && content[3] == 'F'.code.toByte() &&
            content[8] == 'W'.code.toByte() && content[9] == 'E'.code.toByte() &&
            content[10] == 'B'.code.toByte() && content[11] == 'P'.code.toByte() -> MediaStoreImageFormat.WEBP

        else -> throw IllegalArgumentException("Unsupported image format")
    }
}
