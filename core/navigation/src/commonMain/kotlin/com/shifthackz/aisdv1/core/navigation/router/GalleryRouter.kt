package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the gallery screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface GalleryRouter {
    /**
     * Opens the application drawer without changing the active route.
     *
     * @author Dmitriy Moroz
     */
    fun openDrawer()

    /**
     * Opens the gallery result identified by its persisted item ID.
     *
     * @param itemId Persisted gallery result identity; never a diagnostic label.
     * @author Dmitriy Moroz
     */
    fun navigateToGalleryDetails(itemId: Long)
}
