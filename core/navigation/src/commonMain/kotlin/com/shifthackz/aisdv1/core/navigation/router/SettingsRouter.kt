package com.shifthackz.aisdv1.core.navigation.router

import com.shifthackz.aisdv1.core.navigation.LaunchSource

/**
 * Navigation actions consumed by the settings screen. The host owns route and back-stack mutation.
 *
 * @author Dmitriy Moroz
 */
interface SettingsRouter {
    /**
     * Opens the configured server's web interface inside the application.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToWebUi()

    /**
     * Opens account authorization, automatic publishing and Civitai upload history.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToCivitai()

    /**
     * Opens the standalone Create/Restore backup tabs.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToBackup()

    /**
     * Opens the application's searchable FAQ.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToFaq()

    /**
     * Opens the application drawer without changing the active route.
     *
     * @author Dmitriy Moroz
     */
    fun openDrawer()

    /**
     * Closes the application drawer without changing the active route.
     *
     * @author Dmitriy Moroz
     */
    fun closeDrawer()

    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    fun navigateBack()

    /**
     * Opens provider/server setup while preserving the source used for its completion behavior.
     *
     * @param source Launch context retained for setup/onboarding completion and Back behavior.
     * @author Dmitriy Moroz
     */
    fun navigateToServerSetup(source: LaunchSource)

    /**
     * Opens the hardware benchmark destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToBenchmark()

    /**
     * Opens the standalone storage usage destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToStorageUsage()

    /**
     * Opens the standalone network usage destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToNetworkUsage()

    /**
     * Opens the developer debug menu destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToDebugMenu()

    /**
     * Opens the donation destination.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToDonate()

    /**
     * Opens onboarding with the supplied launch source for its completion behavior.
     *
     * @param source Launch context retained for setup/onboarding completion and Back behavior.
     * @author Dmitriy Moroz
     */
    fun navigateToOnBoarding(source: LaunchSource)
}
