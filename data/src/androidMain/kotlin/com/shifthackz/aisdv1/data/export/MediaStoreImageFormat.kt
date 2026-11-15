package com.shifthackz.aisdv1.data.export

/**
 * Keeps the exported filename suffix and MIME type consistent with the encoded image format.
 *
 * @param extension Filename suffix without its leading dot.
 * @param mimeType Image media type declared to Android's media provider.
 * @author Dmitriy Moroz
 */
internal enum class MediaStoreImageFormat(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp"),
}
