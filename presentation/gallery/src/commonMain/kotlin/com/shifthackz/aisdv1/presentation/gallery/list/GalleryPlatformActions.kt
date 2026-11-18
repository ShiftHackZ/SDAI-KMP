package com.shifthackz.aisdv1.presentation.gallery.list

/**
 * Native destination and archive-sharing actions emitted by gallery export flows.
 *
 * @author Dmitriy Moroz
 */
interface GalleryPlatformActions {
    /**
     * Opens the configured media-export destination in the platform file browser.
     *
     * @param folderUri Native destination URI, such as an Android document-tree/content URI.
     *
     * @author Dmitriy Moroz
     */
    fun openMediaStoreFolder(folderUri: String)

    /**
     * Hands a completed gallery ZIP to native file sharing.
     *
     * @param filePath Absolute local path to the completed app-owned ZIP artifact.
     *
     * @author Dmitriy Moroz
     */
    fun shareExport(filePath: String)
}
