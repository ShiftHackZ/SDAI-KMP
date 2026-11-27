package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the image to image screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface ImageToImageRouter {
    /**
     * Opens independent upscaler setup without changing generation selection.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToUpscalerSetup()

    /**
     * Opens the application drawer without changing the active route.
     *
     * @author Dmitriy Moroz
     */
    fun openDrawer()

    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()

    /**
     * Opens provider/server setup while preserving the source used for its completion behavior.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToServerSetup()

    /**
     * Opens the hardware benchmark destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToBenchmark()

    /**
     * Opens the image inpainting editor from image-to-image.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToImageInPaint()

    /**
     * Opens the gallery result identified by its persisted item ID.
     *
     * @param itemId Persisted gallery result identity; never a diagnostic label.
     * @author Dmitriy Moroz
     */
    fun navigateToGalleryDetails(itemId: Long)

    /**
     * Opens the reporting flow for the persisted gallery result.
     *
     * @param itemId Persisted gallery result identity; never a diagnostic label.
     * @author Dmitriy Moroz
     */
    fun navigateToReportImage(itemId: Long)
}
