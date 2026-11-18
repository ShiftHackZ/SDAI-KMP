package com.shifthackz.aisdv1.presentation.components.widget.image

/**
 * Display and metadata retention limits, configured explicitly in presentation DI.
 * These are bounded starting limits; device calibration is a separate real-application measurement.
 *
 * @param cacheEntries Maximum shared decoded-image references.
 * @param cacheBytes Maximum shared decoded pixel bytes.
 * @param concurrentDecodes Maximum image reads/decodes in flight.
 * @param encodedChars Maximum single Base64 source characters.
 * @param maxDisplayEdge Maximum sampled bitmap edge.
 * @param detailEntries Maximum detail metadata entries.
 * @param detailMetadataBytes Maximum retained detail text bytes.
 * @param metadataWindow Maximum gallery metadata rows.
 * @param metadataPrefetch Positions retained before the first visible row.
 *
 * @author Dmitriy Moroz
 */
data class GalleryMemoryBudget(
    val cacheEntries: Int,
    val cacheBytes: Long,
    val concurrentDecodes: Int,
    val encodedChars: Int,
    val maxDisplayEdge: Int,
    val detailEntries: Int,
    val detailMetadataBytes: Long,
    val metadataWindow: Int,
    val metadataPrefetch: Int,
) {
    init {
        require(cacheEntries > 0 && cacheBytes > 0 && concurrentDecodes > 0)
        require(encodedChars in 1..32 * 1024 * 1024 && maxDisplayEdge in 64..2048)
        require(detailEntries >= 3 && detailMetadataBytes > 0 && metadataWindow in 1..120 && metadataPrefetch >= 0)
    }
}
