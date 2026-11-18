package com.shifthackz.aisdv1.presentation.components.widget.image

/**
 * Identity of one sampled display image; no encoded payload is retained by the key.
 *
 * @param id Persisted ID or the unsaved-result sentinel.
 * @param revision Content revision allocated once per image.
 * @param isInput Whether the original input is displayed.
 * @param edge Bucketed target edge in pixels.
 *
 * @author Dmitriy Moroz
 */
internal data class GalleryImageCacheKey(
    val id: Long,
    val revision: String,
    val isInput: Boolean,
    val edge: Int
)
