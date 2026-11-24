package com.shifthackz.aisdv1.core.navigation

import com.shifthackz.aisdv1.core.navigation.router.BackupRouter
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
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
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
    BackupRouter,
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
    CivitaiRouter,
    StorageUsageRouter,
    NetworkUsageRouter,
    WebUiRouter,
    OnBoardingRouter {

    /**
     * Opens reusable backup tabs from Settings.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToBackup() = navigateTo(AppRoute.Backup(LaunchSource.SETTINGS))

    override fun navigateToFaq() = navigateTo(AppRoute.Faq)

    override fun navigateToBlog() = navigateTo(AppRoute.Blog)

    /**
     * Opens the selected article and leaves the blog list in the back stack.
     *
     * @param slug Article identifier supplied by the selected list item.
     * @author Dmitriy Moroz
     */
    fun navigateToBlogDetail(slug: String) = navigateTo(AppRoute.BlogDetail(slug))

    /**
     * Consumes onboarding so Back cannot reopen it.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToBackupWelcome() = replaceRoute(AppRoute.BackupWelcome)

    /**
     * Rebuilds Settings as the return destination before opening a cold-start pending backup operation.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToBackupRestore() {
        replaceRoute(AppRoute.Settings)
        navigateTo(AppRoute.Backup(LaunchSource.SETTINGS))
    }

    /**
     * Opens restore-only content during first launch.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToInitialRestore() = replaceRoute(AppRoute.Backup(LaunchSource.SPLASH))

    /**
     * Consumes first-launch backup before mandatory provider setup.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToInitialConfiguration() =
        replaceRoute(AppRoute.Setup(LaunchSource.SPLASH))

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

    private var nextEntryId = 1L
    private val routeStack = mutableListOf(NavigationEntry(0L, initialRoute))
    private val backStackState = MutableStateFlow(routeStack.toList())

    /**
     * Immutable history snapshots for rendering retained screens and cancellable Back previews.
     * Entry IDs are stable across pushes and pops; replacement starts a new screen lifetime.
     *
     * @author Dmitriy Moroz
     */
    val backStack = backStackState.asStateFlow()

    private val beforeCoverObservers = mutableSetOf<(NavigationEntry) -> Unit>()

    /**
     * Observes the still-visible entry before a drawer or standalone screen covers it.
     * Used by the host to retain a visual Back preview; observers must not mutate navigation.
     *
     * @param observer Receives the outgoing entry on the host UI thread.
     * @return Detaches this observer when the host leaves composition.
     * @author Dmitriy Moroz
     */
    fun observeBeforeCover(observer: (NavigationEntry) -> Unit): () -> Unit {
        beforeCoverObservers.add(observer)
        return { beforeCoverObservers.remove(observer) }
    }

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
            setRoute(routeStack.last().route)
        } else if (routeStack.lastOrNull()?.route?.canFallBackToTextToImage() == true) {
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
     * Opens Civitai account settings and upload history from the invoking route.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToCivitai() = navigateTo(AppRoute.Civitai)

    /**
     * Opens the account-independent receipt history above Civitai settings.
     *
     * @author Dmitriy Moroz
     */
    override fun navigateToCivitaiHistory() = navigateTo(AppRoute.CivitaiHistory)

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
        if (!drawerOpenState.value) beforeCoverObservers.toList().forEach { it(routeStack.last()) }
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
    override fun navigateToWebUi() {
        navigateTo(AppRoute.WebUi)
    }

    private fun navigateTo(route: AppRoute) {
        val wasDrawerOpen = drawerOpenState.value
        closeDrawer()
        if (routeState.value == route) return
        if (!wasDrawerOpen) beforeCoverObservers.toList().forEach { it(routeStack.last()) }
        routeStack.add(NavigationEntry(nextEntryId++, route))
        setRoute(route)
    }

    private fun navigateToHomeRoute(route: AppRoute) {
        closeDrawer()
        if (routeState.value == route) return
        if (routeStack.lastOrNull()?.route?.isHomeRoute() == true) {
            replaceRoute(route)
        } else {
            navigateTo(route)
        }
    }

    private fun navigateToGenerationHomeRoute(route: AppRoute) {
        if (routeStack.lastOrNull()?.route is AppRoute.GalleryDetail) {
            replaceRoute(route)
        } else {
            navigateToHomeRoute(route)
        }
    }

    private fun replaceRoute(route: AppRoute) {
        closeDrawer()
        if (routeStack.isEmpty()) {
            routeStack.add(NavigationEntry(nextEntryId++, route))
        } else {
            routeStack[routeStack.lastIndex] = NavigationEntry(nextEntryId++, route)
        }
        setRoute(route)
    }

    private fun setRoute(route: AppRoute) {
        routeState.value = route
        canNavigateBackState.value = canNavigateBackFrom(route)
        backStackState.value = routeStack.toList()
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

    AppRoute.BackupWelcome,
    is AppRoute.Backup,
    AppRoute.Splash,
    is AppRoute.OnBoarding,
    is AppRoute.Setup,
    AppRoute.ConfigurationLoader,
    is AppRoute.GalleryDetail,
    AppRoute.ImageInPaint,
    AppRoute.History,
    AppRoute.Benchmark,
    AppRoute.Civitai,
    AppRoute.CivitaiHistory,
    AppRoute.StorageUsage,
    AppRoute.NetworkUsage,
    is AppRoute.Report,
    AppRoute.Debug,
    AppRoute.Logger,
    AppRoute.Donate,
    AppRoute.WebUi,
    AppRoute.Faq,
    AppRoute.Blog,
    is AppRoute.BlogDetail,
        -> false
}

/**
 * Returns whether a standalone initial route can return to text-to-image without a previous stack entry.
 *
 * @author Dmitriy Moroz
 */
private fun AppRoute.canFallBackToTextToImage(): Boolean = when (this) {
    is AppRoute.Backup -> source == LaunchSource.SETTINGS
    AppRoute.ImageToImage,
    AppRoute.ImageInPaint,
    AppRoute.Gallery,
    AppRoute.Settings,
    AppRoute.History,
    AppRoute.Benchmark,
    AppRoute.Civitai,
    AppRoute.CivitaiHistory,
    AppRoute.StorageUsage,
    AppRoute.NetworkUsage,
    is AppRoute.GalleryDetail,
    is AppRoute.Report,
    AppRoute.Debug,
    AppRoute.Logger,
    AppRoute.Donate,
    AppRoute.WebUi,
    AppRoute.Faq,
    AppRoute.Blog,
    is AppRoute.BlogDetail,
        -> true

    AppRoute.Home,
    AppRoute.TextToImage,
    AppRoute.BackupWelcome,
    AppRoute.Splash,
    is AppRoute.OnBoarding,
    is AppRoute.Setup,
    AppRoute.ConfigurationLoader,
        -> false
}
