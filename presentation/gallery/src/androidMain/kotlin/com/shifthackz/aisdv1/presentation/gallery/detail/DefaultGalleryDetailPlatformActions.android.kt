package com.shifthackz.aisdv1.presentation.gallery.detail

/**
 * Returns an unsupported placeholder; application DI installs the context/file-provider Android adapter.
 *
 * @author Dmitriy Moroz
 */
actual fun createDefaultGalleryDetailPlatformActions(): GalleryDetailPlatformActions =
    NoOpGalleryDetailPlatformActions
