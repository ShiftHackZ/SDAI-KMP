package com.shifthackz.aisdv1.core.navigation.router


/**
 * Navigation actions consumed by the splash screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface SplashRouter {
    /**
     * Resumes first-launch restore-only content before mandatory setup.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToInitialRestore()

    /**
     * Resumes pending restore work before normal configuration publication.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToBackupRestore()

    /**
     * Resumes the first-launch restore invitation.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToBackupWelcome()

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
