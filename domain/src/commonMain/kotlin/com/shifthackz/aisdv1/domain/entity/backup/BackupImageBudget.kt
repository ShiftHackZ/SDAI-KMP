package com.shifthackz.aisdv1.domain.entity.backup

/**
 * Shared gallery-backup admission applied before encoded allocation and native pixel decoding.
 *
 * @param maxEncodedBytes Maximum combined output/input encoded image bytes in one gallery record.
 * @param maxPixels Maximum original pixel count accepted from native metadata.
 * @param maxDecodedBytes Maximum conservative original decoded size, calculated with eight bytes per pixel.
 * @param validationEdge Maximum edge of the sampled native content-validation bitmap.
 * @author Dmitriy Moroz
 */
data class BackupImageBudget(
    val maxEncodedBytes: Int,
    val maxPixels: Long,
    val maxDecodedBytes: Long,
    val validationEdge: Int,
) {
    init {
        require(maxEncodedBytes > 0 && maxPixels > 0 && maxDecodedBytes > 0)
        require(validationEdge in 1..1024)
    }
}
