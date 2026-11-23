package com.shifthackz.aisdv1.presentation.gallery.preview

import com.shifthackz.aisdv1.presentation.gallery.list.GalleryState

/**
 * Fixed gallery ordering, selection and pixel availability for the preview picker.
 *
 * @param name Describes the grid, selection or loading scenario.
 * @param state Metadata, identities, selection flags and overlay to render.
 * @param firstVisibleItem Initial position in the complete gallery identity list.
 * @param isImageLoading Whether populated cells display their pixel-loading shimmer.
 * @param unavailableImageIds Cells displaying the unavailable-image placeholder.
 * @author Dmitriy Moroz
 */
internal data class GalleryPreviewCase(
    val name: String,
    val state: GalleryState,
    val firstVisibleItem: Int = 0,
    val isImageLoading: Boolean = false,
    val unavailableImageIds: Set<Long> = emptySet(),
)
