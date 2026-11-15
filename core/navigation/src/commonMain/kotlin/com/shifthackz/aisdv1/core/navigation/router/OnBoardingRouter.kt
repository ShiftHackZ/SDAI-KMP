package com.shifthackz.aisdv1.core.navigation.router


/**
 * Navigation actions consumed by the on boarding screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface OnBoardingRouter {
    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()

    /**
     * Replaces completed onboarding with initial-launch server setup.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToServerSetupAfterOnBoarding()

    /**
     * Replaces completed onboarding with configuration loading.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToPostOnBoardingConfigLoader()
}
