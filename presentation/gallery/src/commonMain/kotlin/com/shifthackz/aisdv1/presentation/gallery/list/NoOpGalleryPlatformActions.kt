package com.shifthackz.aisdv1.presentation.gallery.list

/**
 * Performs no folder-opening or archive-sharing action on unsupported hosts.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpGalleryPlatformActions : GalleryPlatformActions {

    override fun openMediaStoreFolder(folderUri: String) = Unit

    override fun shareExport(filePath: String) = Unit
}
