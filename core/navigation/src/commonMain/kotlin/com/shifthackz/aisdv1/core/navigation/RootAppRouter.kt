package com.shifthackz.aisdv1.core.navigation

import com.shifthackz.aisdv1.core.navigation.router.BenchmarkRouter
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import com.shifthackz.aisdv1.core.navigation.router.DebugMenuRouter
import com.shifthackz.aisdv1.core.navigation.router.DonateRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryDetailRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryRouter
import com.shifthackz.aisdv1.core.navigation.router.HistoryRouter
import com.shifthackz.aisdv1.core.navigation.router.HomeRouter
import com.shifthackz.aisdv1.core.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import com.shifthackz.aisdv1.core.navigation.router.NetworkUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.core.navigation.router.ReportRouter
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.core.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import com.shifthackz.aisdv1.core.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.TextToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Owns the in-process route stack and drawer state for one application host. Call navigation actions on the host UI thread. Consecutive home-tab selections replace each other; standalone screens retain Back history.
 *
 * @param initialRoute First route retained when the host creates its navigation controller.
 * @author Dmitriy Moroz
 */
class RootAppRouter(
    initialRoute: AppRoute = AppRoute.Splash,
) : ServerSetupRouter,
    ConfigurationLoaderRouter,
    SplashRouter,
    DonateRouter,
    HomeRouter,
    TextToImageRouter,
    ImageToImageRouter,
    HistoryRouter,
    GalleryRouter,
    GalleryDetailRouter,
    DebugMenuRouter,
    LoggerRouter,
    ReportRouter,
    SettingsRouter,
    BenchmarkRouter,
    StorageUsageRouter,
    NetworkUsageRouter,
    WebUiRouter,
    OnBoardingRouter {

    private val routeState = MutableStateFlow(initialRoute)

    /**
     * Current route observed by the host renderer. Arguments remain in memory, outside diagnostic identities.
     *
     * @author Dmitriy Moroz
     */
    val route = routeState.asStateFlow()

    private val drawerOpenState = MutableStateFlow(false)

    /**
     * Whether the application drawer is currently open.
     *
     * @author Dmitriy Moroz
     */
    val drawerOpen = drawerOpenState.asStateFlow()

    private val routeStack = mutableListOf(initialRoute)
    private val canNavigateBackState = MutableStateFlow(canNavigateBackFrom(initialRoute))

    /**
     * Whether Back can pop a route or return to text-to-image. Drawer dismissal is handled before route changes.
     *
     * @author Dmitriy Moroz
     */
    val canNavigateBack = canNavigateBackState.asStateFlow()

    /**
     * Closes the drawer first, otherwise returns to the previous route or the generation-home fallback.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateBack() {
        if (drawerOpenState.value) {
            closeDrawer()
            return
        }

        closeDrawer()
        if (routeStack.size > 1) {
            routeStack.removeAt(routeStack.lastIndex)
            setRoute(routeStack.last())
        } else if (routeStack.lastOrNull()?.canFallBackToTextToImage() == true) {
            replaceRoute(AppRoute.TextToImage)
        }
    }

    /**
     * Replaces the completed setup route with configuration loading so Back cannot reopen setup.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToPostSetupConfigLoader() {
        replaceRoute(AppRoute.ConfigurationLoader)
    }

    /**
     * Replaces configuration loading with the text-to-image home destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToHomeScreen() {
        replaceRoute(AppRoute.TextToImage)
    }

    /**
     * Replaces splash with onboarding using the initial-launch source.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToOnBoardingFromSplash() {
        replaceRoute(AppRoute.OnBoarding(LaunchSource.SPLASH))
    }

    /**
     * Replaces splash with server setup using the initial-launch source.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToServerSetupFromSplash() {
        replaceRoute(AppRoute.Setup(LaunchSource.SPLASH))
    }

    /**
     * Replaces splash with configuration loading after launch requirements are satisfied.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToPostSplashConfigLoader() {
        replaceRoute(AppRoute.ConfigurationLoader)
    }

    /**
     * Opens provider/server setup while preserving the source used for its completion behavior.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToServerSetup() {
        navigateTo(AppRoute.Setup(LaunchSource.SETTINGS))
    }

    /**
     * Opens provider/server setup while preserving the source used for its completion behavior.
     *
     * @param source Launch context for setup/onboarding completion.
     * @author Dmitriy Moroz
     */
    override fun navigateToServerSetup(source: LaunchSource) {
        navigateTo(AppRoute.Setup(source))
    }

    /**
     * Replaces completed onboarding with initial-launch server setup.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToServerSetupAfterOnBoarding() {
        replaceRoute(AppRoute.Setup(LaunchSource.SPLASH))
    }

    /**
     * Selects text-to-image, replacing the active home tab or consumed gallery-detail route.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToTextToImage() {
        navigateToGenerationHomeRoute(AppRoute.TextToImage)
    }

    /**
     * Selects image-to-image, replacing the active home tab or consumed gallery-detail route.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToImageToImage() {
        navigateToGenerationHomeRoute(AppRoute.ImageToImage)
    }

    /**
     * Opens the image inpainting editor from image-to-image.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToImageInPaint() {
        navigateTo(AppRoute.ImageInPaint)
    }

    /**
     * Selects the gallery home tab without stacking repeated home-tab selections.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToGallery() {
        navigateToHomeRoute(AppRoute.Gallery)
    }

    /**
     * Selects the Settings home tab without stacking repeated home-tab selections.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToSettings() {
        navigateToHomeRoute(AppRoute.Settings)
    }

    /**
     * Opens the hardware benchmark destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToBenchmark() {
        navigateTo(AppRoute.Benchmark)
    }

    /**
     * Opens the standalone storage usage destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToStorageUsage() {
        navigateTo(AppRoute.StorageUsage)
    }

    /**
     * Opens the standalone network usage destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToNetworkUsage() {
        navigateTo(AppRoute.NetworkUsage)
    }

    /**
     * Opens the generation history destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToHistory() {
        navigateTo(AppRoute.History)
    }

    /**
     * Opens the application drawer without changing the active route.
     *
     * @author Dmitriy Moroz
     */
    override fun openDrawer() {
        drawerOpenState.value = true
    }

    /**
     * Closes the application drawer without changing the active route.
     *
     * @author Dmitriy Moroz
     */
    override fun closeDrawer() {
        drawerOpenState.value = false
    }

    /**
     * Opens the developer debug menu destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToDebugMenu() {
        navigateTo(AppRoute.Debug)
    }

    /**
     * Opens the donation destination.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToDonate() {
        navigateTo(AppRoute.Donate)
    }

    /**
     * Opens onboarding with the supplied launch source for its completion behavior.
     *
     * @param source Launch context for setup/onboarding completion.
     * @author Dmitriy Moroz
     */
    override fun navigateToOnBoarding(source: LaunchSource) {
        navigateTo(AppRoute.OnBoarding(source))
    }

    /**
     * Replaces completed onboarding with configuration loading.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToPostOnBoardingConfigLoader() {
        replaceRoute(AppRoute.ConfigurationLoader)
    }

    /**
     * Opens the gallery result identified by its persisted item ID.
     *
     * @param itemId Persisted gallery result identity.
     * @author Dmitriy Moroz
     */
    override fun navigateToGalleryDetails(itemId: Long) {
        navigateTo(AppRoute.GalleryDetail(itemId))
    }

    /**
     * Opens the reporting flow for the persisted gallery result.
     *
     * @param itemId Persisted gallery result identity.
     * @author Dmitriy Moroz
     */
    override fun navigateToReportImage(itemId: Long) {
        navigateTo(AppRoute.Report(itemId))
    }

    /**
     * Opens the application log viewer.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToLogger() {
        navigateTo(AppRoute.Logger)
    }

    /**
     * Opens the configured server web interface.
     *
     * @author Dmitriy Moroz
     */
    fun navigateToWebUi() {
        navigateTo(AppRoute.WebUi)
    }

    private fun navigateTo(route: AppRoute) {
        closeDrawer()
        if (routeState.value == route) return
        routeStack.add(route)
        setRoute(route)
    }

    private fun navigateToHomeRoute(route: AppRoute) {
        closeDrawer()
        if (routeState.value == route) return
        if (routeStack.lastOrNull()?.isHomeRoute() == true) {
            replaceRoute(route)
        } else {
            navigateTo(route)
        }
    }

    private fun navigateToGenerationHomeRoute(route: AppRoute) {
        if (routeStack.lastOrNull() is AppRoute.GalleryDetail) {
            replaceRoute(route)
        } else {
            navigateToHomeRoute(route)
        }
    }

    private fun replaceRoute(route: AppRoute) {
        closeDrawer()
        if (routeStack.isEmpty()) {
            routeStack.add(route)
        } else {
            routeStack[routeStack.lastIndex] = route
        }
        setRoute(route)
    }

    private fun setRoute(route: AppRoute) {
        routeState.value = route
        canNavigateBackState.value = canNavigateBackFrom(route)
    }

    private fun canNavigateBackFrom(route: AppRoute): Boolean {
        return routeStack.size > 1 || route.canFallBackToTextToImage()
    }
}

/**
 * Returns whether a destination participates in the retained generation/gallery/Settings home tabs.
 *
 * @author Dmitriy Moroz
 */
fun AppRoute.isHomeRoute(): Boolean = when (this) {
    AppRoute.Home,
    AppRoute.TextToImage,
    AppRoute.ImageToImage,
    AppRoute.Gallery,
    AppRoute.Settings,
        -> true

    AppRoute.Splash,
    is AppRoute.OnBoarding,
    is AppRoute.Setup,
    AppRoute.ConfigurationLoader,
    is AppRoute.GalleryDetail,
    AppRoute.ImageInPaint,
    AppRoute.History,
    AppRoute.Benchmark,
    AppRoute.StorageUsage,
    AppRoute.NetworkUsage,
    is AppRoute.Report,
    AppRoute.Debug,
    AppRoute.Logger,
    AppRoute.Donate,
    AppRoute.WebUi,
        -> false
}

/**
 * Returns whether a standalone initial route can return to text-to-image without a previous stack entry.
 *
 * @author Dmitriy Moroz
 */
private fun AppRoute.canFallBackToTextToImage(): Boolean = when (this) {
    AppRoute.ImageToImage,
    AppRoute.ImageInPaint,
    AppRoute.Gallery,
    AppRoute.Settings,
    AppRoute.History,
    AppRoute.Benchmark,
    AppRoute.StorageUsage,
    AppRoute.NetworkUsage,
    is AppRoute.GalleryDetail,
    is AppRoute.Report,
    AppRoute.Debug,
    AppRoute.Logger,
    AppRoute.Donate,
    AppRoute.WebUi,
        -> true

    AppRoute.Home,
    AppRoute.TextToImage,
    AppRoute.Splash,
    is AppRoute.OnBoarding,
    is AppRoute.Setup,
    AppRoute.ConfigurationLoader,
        -> false
}
