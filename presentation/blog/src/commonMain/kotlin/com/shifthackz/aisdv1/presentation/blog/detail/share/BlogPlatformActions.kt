package com.shifthackz.aisdv1.presentation.blog.detail.share

/**
 * Opens the device's share sheet for a published blog article.
 *
 * @author Dmitriy Moroz
 */
internal interface BlogPlatformActions {
    /**
     * Presents the system chooser from the current screen. Must be called on the UI thread.
     *
     * @param text Article title followed by the canonical link on a new line.
     * @return Whether a share sheet could be presented.
     * @author Dmitriy Moroz
     */
    fun sharePost(text: String): Boolean
}
