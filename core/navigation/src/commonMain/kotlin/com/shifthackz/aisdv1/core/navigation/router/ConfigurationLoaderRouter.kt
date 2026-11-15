package com.shifthackz.aisdv1.core.navigation.router

/**
 * Navigation actions consumed by the configuration loader screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface ConfigurationLoaderRouter {
    /**
     * Replaces configuration loading with the text-to-image home destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToHomeScreen()
}
