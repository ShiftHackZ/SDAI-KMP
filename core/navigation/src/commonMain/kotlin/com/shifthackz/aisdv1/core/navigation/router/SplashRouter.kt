package com.shifthackz.aisdv1.core.navigation.router


/**
 * Navigation actions consumed by the splash screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface SplashRouter {
    /**
     * Replaces splash with onboarding using the initial-launch source.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToOnBoardingFromSplash()

    /**
     * Replaces splash with server setup using the initial-launch source.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToServerSetupFromSplash()

    /**
     * Replaces splash with configuration loading after launch requirements are satisfied.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToPostSplashConfigLoader()
}
