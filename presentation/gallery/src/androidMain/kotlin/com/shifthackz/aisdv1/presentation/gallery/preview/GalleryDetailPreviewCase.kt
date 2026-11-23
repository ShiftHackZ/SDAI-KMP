package com.shifthackz.aisdv1.presentation.gallery.preview

import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailState

/**
 * Fixed detail metadata, tab and image presentation without lifecycle-driven loading.
 *
 * @param name Describes the active tab, metadata or operation overlay.
 * @param state Current item, tab, capabilities and dialog.
 * @param metadataOffset Initial vertical metadata-table offset in pixels.
 * @param imageWidth Width of the fixed checkerboard sample in pixels.
 * @param imageHeight Height of the fixed checkerboard sample in pixels.
 * @param isImageLoading Whether the image viewport displays the actual loading shimmer.
 * @param isImageUnavailable Whether the image viewport retains the actual unavailable-image blank state.
 * @author Dmitriy Moroz
 */
internal data class GalleryDetailPreviewCase(
    val name: String,
    val state: GalleryDetailState,
    val metadataOffset: Int = 0,
    val imageWidth: Int = 32,
    val imageHeight: Int = 32,
    val isImageLoading: Boolean = false,
    val isImageUnavailable: Boolean = false,
)
