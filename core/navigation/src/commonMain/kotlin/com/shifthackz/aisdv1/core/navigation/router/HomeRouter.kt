package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the home screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface HomeRouter {
    /**
     * Opens provider/server setup while preserving the source used for its completion behavior.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToServerSetup()

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
     * Selects the gallery home tab without stacking repeated home-tab selections.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToGallery()

    /**
     * Selects the Settings home tab without stacking repeated home-tab selections.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToSettings()

    /**
     * Opens the generation history destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToHistory()
}
