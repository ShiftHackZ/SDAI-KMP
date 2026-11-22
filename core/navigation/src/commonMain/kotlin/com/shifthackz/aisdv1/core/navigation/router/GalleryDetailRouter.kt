package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the gallery detail screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface GalleryDetailRouter {
    /**
     * Opens account authorization, automatic publishing and Civitai upload history.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToCivitai()

    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()

    /**
     * Selects text-to-image, replacing the active home tab or consumed gallery-detail route.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToTextToImage()

    /**
     * Selects image-to-image, replacing the active home tab or consumed gallery-detail route.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToImageToImage()

    /**
     * Opens the reporting flow for the persisted gallery result.
     *
     * @param itemId Persisted gallery result identity; never a diagnostic label.
     * @author Dmitriy Moroz
     */
    fun navigateToReportImage(itemId: Long)
}
