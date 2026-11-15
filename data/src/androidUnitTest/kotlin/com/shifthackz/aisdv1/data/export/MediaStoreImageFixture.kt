package com.shifthackz.aisdv1.data.export

/**
 * Small encoded images with ancillary metadata that must survive decoding and export unchanged.
 * One-pixel PNG, JPEG and WebP resources carry tEXt, COM and XMP metadata respectively.
 * Expected filenames and MIME types are independent of the production format enum.
 *
 * @param extension Resource suffix and expected export suffix.
 * @param mimeType Expected media-provider content type.
 * @author Dmitriy Moroz
 */
enum class MediaStoreImageFixture(val extension: String, val mimeType: String) {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp");

    /**
     * Loads a fresh copy so tests cannot mutate another test's reference bytes.
     *
     * @return Complete encoded image including its ancillary metadata.
     * @author Dmitriy Moroz
     */
    fun bytes(): ByteArray = checkNotNull(
        javaClass.getResourceAsStream("/image-export/metadata.$extension"),
    ).use { it.readBytes() }
}
