package com.shifthackz.aisdv1.core.navigation.router


/**
 * Ignores navigation actions when a screen is rendered without a host router.
 *
 * @author Dmitriy Moroz
 */
object NoOpGalleryRouter : GalleryRouter {
    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun openDrawer() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @param itemId Persisted gallery result identity; never a diagnostic label.
     * @author Dmitriy Moroz
     */
    override fun navigateToGalleryDetails(itemId: Long) = Unit
}
