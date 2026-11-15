package com.shifthackz.aisdv1.core.navigation.router

/**
 * Ignores navigation actions when a screen is rendered without a host router.
 *
 * @author Dmitriy Moroz
 */
object NoOpSplashRouter : SplashRouter {
    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToOnBoardingFromSplash() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToServerSetupFromSplash() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToPostSplashConfigLoader() = Unit
}
