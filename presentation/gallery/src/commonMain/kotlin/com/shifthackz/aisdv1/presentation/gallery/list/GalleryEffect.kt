package com.shifthackz.aisdv1.presentation.gallery.list

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Native actions emitted after gallery destination lookup or archive creation.
 *
 * @author Dmitriy Moroz
 */
sealed interface GalleryEffect : MviEffect {
    /**
     * Opens the native media destination selected for saved images.
     *
     * @param folderUri Native content/document URI of the export folder.
     *
     * @author Dmitriy Moroz
     */
    data class OpenMediaStoreFolder(val folderUri: String) : GalleryEffect

    /**
     * Shares a fully written gallery archive.
     *
     * @param filePath Absolute local path to the completed app-owned ZIP artifact.
     *
     * @author Dmitriy Moroz
     */
    data class ShareExport(val filePath: String) : GalleryEffect
}
