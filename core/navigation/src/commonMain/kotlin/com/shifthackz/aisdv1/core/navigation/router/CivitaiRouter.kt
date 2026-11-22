package com.shifthackz.aisdv1.core.navigation.router

/**
 * Standalone Civitai settings navigation owned by the native app host.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiRouter {
    /**
     * Opens paginated local publication receipts, including while signed out.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToCivitaiHistory()

    /**
     * Returns to the invoking settings or gallery screen.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()
}
