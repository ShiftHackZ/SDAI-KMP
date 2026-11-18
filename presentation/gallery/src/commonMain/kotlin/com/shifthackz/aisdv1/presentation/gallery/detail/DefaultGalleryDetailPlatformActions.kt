package com.shifthackz.aisdv1.presentation.gallery.detail

/**
 * Creates the platform default detail-action adapter; Android DI replaces its unsupported default.
 *
 * @author Dmitriy Moroz
 */
expect fun createDefaultGalleryDetailPlatformActions(): GalleryDetailPlatformActions
