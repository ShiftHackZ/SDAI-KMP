package com.shifthackz.aisdv1.core.navigation.router


/**
 * Ignores navigation actions when a screen is rendered without a host router.
 *
 * @author Dmitriy Moroz
 */
object NoOpGalleryDetailRouter : GalleryDetailRouter {
    override fun navigateToCivitai() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateBack() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToTextToImage() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToImageToImage() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @param itemId Persisted gallery result identity; never a diagnostic label.
     * @author Dmitriy Moroz
     */
    override fun navigateToReportImage(itemId: Long) = Unit
}
