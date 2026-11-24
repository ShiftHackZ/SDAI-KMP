package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation owned by the standalone upscaler.
 *
 * @author Dmitriy Moroz
 */
interface UpscalerRouter {
    /**
     * Opens the Home navigation drawer.
     *
     * @author Dmitriy Moroz
     */
    fun openDrawer()

    /**
     * Opens the independent provider/model wizard.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToUpscalerSetup()

    /**
     * Opens the saved original/upscaled result in Gallery.
     *
     * @param itemId Persisted gallery identity.
     * @author Dmitriy Moroz
     */
    fun navigateToGalleryDetails(itemId: Long)

    /**
     * Opens the normal reporting flow for a saved or cached upscaler result.
     *
     * @param itemId Persisted or temporary Gallery identity.
     * @author Dmitriy Moroz
     */
    fun navigateToReportImage(itemId: Long)
}
