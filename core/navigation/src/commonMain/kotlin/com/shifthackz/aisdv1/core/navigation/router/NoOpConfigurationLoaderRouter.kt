package com.shifthackz.aisdv1.core.navigation.router

/**
 * Ignores navigation actions when a screen is rendered without a host router.
 *
 * @author Dmitriy Moroz
 */
object NoOpConfigurationLoaderRouter : ConfigurationLoaderRouter {
    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToHomeScreen() = Unit
}
