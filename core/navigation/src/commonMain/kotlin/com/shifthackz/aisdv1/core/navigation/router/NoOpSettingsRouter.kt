package com.shifthackz.aisdv1.core.navigation.router

import com.shifthackz.aisdv1.core.navigation.LaunchSource

/**
 * Ignores navigation actions when a screen is rendered without a host router.
 *
 * @author Dmitriy Moroz
 */
object NoOpSettingsRouter : SettingsRouter {
    /**
     * Ignores Web UI navigation when no host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToWebUi() = Unit

    override fun navigateToCivitai() = Unit

    override fun navigateToBackup() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun openDrawer() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun closeDrawer() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateBack() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @param source Launch context retained for setup/onboarding completion and Back behavior.
     * @author Dmitriy Moroz
     */
    override fun navigateToServerSetup(source: LaunchSource) = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToBenchmark() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToStorageUsage() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToNetworkUsage() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToDebugMenu() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToDonate() = Unit

    /**
     * Ignores this action because no navigation host is attached.
     *
     * @param source Launch context retained for setup/onboarding completion and Back behavior.
     * @author Dmitriy Moroz
     */
    override fun navigateToOnBoarding(source: LaunchSource) = Unit
}
