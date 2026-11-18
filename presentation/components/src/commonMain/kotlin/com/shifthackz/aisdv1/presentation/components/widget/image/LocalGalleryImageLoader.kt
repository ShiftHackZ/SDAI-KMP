package com.shifthackz.aisdv1.presentation.components.widget.image

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Shared display owner supplied once by the application composition root.
 * Preview-only compositions without that owner omit image decoding.
 *
 * @author Dmitriy Moroz
 */
val LocalGalleryImageLoader = staticCompositionLocalOf<GalleryImageLoader?> { null }
