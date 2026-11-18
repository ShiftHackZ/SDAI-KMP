package com.shifthackz.aisdv1.presentation.gallery.detail

/**
 * Reports unsupported native detail actions without side effects.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpGalleryDetailPlatformActions : GalleryDetailPlatformActions {

    override suspend fun saveImage(base64: String): GalleryDetailActionResult =
        GalleryDetailActionResult.Unsupported


    override suspend fun shareImage(base64: String): GalleryDetailActionResult =
        GalleryDetailActionResult.Unsupported


    override suspend fun shareText(text: String): GalleryDetailActionResult =
        GalleryDetailActionResult.Unsupported


    override suspend fun copyText(text: String): GalleryDetailActionResult =
        GalleryDetailActionResult.Unsupported
}
