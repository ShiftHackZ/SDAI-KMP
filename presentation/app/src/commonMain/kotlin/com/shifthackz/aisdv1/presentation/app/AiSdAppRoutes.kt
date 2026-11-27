package com.shifthackz.aisdv1.presentation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationPerformanceUseCase
import com.shifthackz.aisdv1.presentation.backup.BackupScreen
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkScreen
import com.shifthackz.aisdv1.presentation.blog.detail.BlogDetailScreen
import com.shifthackz.aisdv1.presentation.blog.list.BlogScreen
import com.shifthackz.aisdv1.presentation.civitai.history.CivitaiHistoryScreen
import com.shifthackz.aisdv1.presentation.civitai.settings.CivitaiScreen
import com.shifthackz.aisdv1.presentation.civitai.upload.CivitaiUploadAction
import com.shifthackz.aisdv1.presentation.civitai.upload.CivitaiUploadViewModel
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.components.theme.global.RetainedHomeRouteTheme
import com.shifthackz.aisdv1.presentation.components.widget.navigation.ProgrammaticPager
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderScreen
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupScreen
import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerSetupScreen
import com.shifthackz.aisdv1.presentation.debug.DebugMenuScreen
import com.shifthackz.aisdv1.presentation.donate.DonateScreen
import com.shifthackz.aisdv1.presentation.faq.FaqScreen
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailScreen
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryScreen
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintScreen
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageContent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.performance.GenerationPerformanceOverlay
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageContent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormState
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerScreen
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerViewModel
import com.shifthackz.aisdv1.presentation.history.HistoryScreen
import com.shifthackz.aisdv1.presentation.home.AppScaffold
import com.shifthackz.aisdv1.presentation.logger.LoggerScreen
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingScreen
import com.shifthackz.aisdv1.presentation.report.ReportScreen
import com.shifthackz.aisdv1.presentation.settings.SettingsScreen
import com.shifthackz.aisdv1.presentation.splash.SplashScreen
import com.shifthackz.aisdv1.presentation.statistics.networkusage.NetworkUsageScreen
import com.shifthackz.aisdv1.presentation.statistics.storageusage.StorageUsageScreen
import com.shifthackz.aisdv1.presentation.web.WebUiScreen
import org.koin.core.Koin
import org.koin.core.parameter.parametersOf

/**
 * Hosts the persistent bottom-tab routes inside the app scaffold.
 * Standalone overlay routes such as storage usage and network usage are deliberately excluded so
 * they can render without the bottom bar.
 *
 * @param modifier Layout modifiers for the retained home scaffold.
 * @param activeRoute Route currently visible in the home scaffold.
 * @param renderedRoutes Home routes already created and kept alive.
 * @param router Root router shared by home tabs.
 * @param buildInfoProvider Build metadata consumed by scaffold-level UI.
 * @param preferenceManager Preferences source consumed by scaffold-level UI.
 * @param shouldShowHomeChrome Whether bottom tabs and home-level chrome should be visible.
 * @param observePerformance Cold opt-in sampler shared with visible generation dialogs.
 * @param textToImageViewModel Shared Text-to-Image ViewModel kept alive with its tab.
 * @param imageToImageViewModel Shared Image-to-Image ViewModel kept alive with its tab.
 * @param upscalerFormState Shared independent catalog observed by the application root.
 * @param upscalerViewModel Shared upscaler inputs retained with the third Home tab.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeRoutesScaffold(
    activeRoute: AppRoute,
    renderedRoutes: List<AppRoute>,
    router: RootAppRouter,
    buildInfoProvider: BuildInfoProvider,
    preferenceManager: PreferenceManager,
    modifier: Modifier = Modifier,
    shouldShowHomeChrome: Boolean = true,
    observePerformance: ObserveGenerationPerformanceUseCase,
    textToImageViewModel: TextToImageViewModel,
    imageToImageViewModel: ImageToImageViewModel,
    upscalerViewModel: UpscalerViewModel,
    upscalerFormState: UpscalerFormState,
) {
    AppScaffold(
        currentRoute = activeRoute,
        router = router,
        buildInfoProvider = buildInfoProvider,
        preferenceManager = preferenceManager,
        shouldShowHomeChrome = shouldShowHomeChrome,
        modifier = modifier,
    ) { contentModifier ->
        val pages =
            listOf(
                AppRoute.TextToImage,
                AppRoute.ImageToImage,
                AppRoute.Upscaler,
                AppRoute.Gallery,
                AppRoute.Settings
            )
        ProgrammaticPager(
            selectedPage = pages.indexOf(activeRoute).coerceAtLeast(0),
            pageCount = pages.size,
            modifier = contentModifier.fillMaxSize(),
        ) { page ->
            val route = pages[page]
            if (route in renderedRoutes) RetainedHomeRouteTheme(LocalMviVisibility.current) {
                when (route) {
                    AppRoute.TextToImage -> {
                        MviComponent(viewModel = textToImageViewModel) { state, processIntent ->
                            TextToImageContent(
                                upscalerFormState = upscalerFormState,
                                modifier = Modifier.fillMaxSize(),
                                state = state,
                                processIntent = processIntent,
                                useDrawerNavigation = true,
                                performanceOverlay = {
                                    val history =
                                        remember(observePerformance) { observePerformance() }
                                    GenerationPerformanceOverlay(history)
                                },
                            )
                        }
                    }

                    AppRoute.ImageToImage -> {
                        MviComponent(viewModel = imageToImageViewModel) { state, processIntent ->
                            ImageToImageContent(
                                upscalerFormState = upscalerFormState,
                                modifier = Modifier.fillMaxSize(),
                                state = state,
                                processIntent = processIntent,
                                useDrawerNavigation = true,
                                performanceOverlay = {
                                    val history =
                                        remember(observePerformance) { observePerformance() }
                                    GenerationPerformanceOverlay(history)
                                },
                            )
                        }
                    }

                    AppRoute.Upscaler -> UpscalerScreen(
                        upscalerViewModel, upscalerFormState, Modifier.fillMaxSize(),
                        performanceOverlay = {
                            val history = remember(observePerformance) { observePerformance() }
                            GenerationPerformanceOverlay(history)
                        },
                    )

                    AppRoute.Gallery -> {
                        GalleryScreen(
                            modifier = Modifier.fillMaxSize(),
                            galleryRouter = router,
                            backHandlerEnabled = activeRoute == AppRoute.Gallery,
                        )
                    }

                    AppRoute.Settings -> {
                        SettingsScreen(
                            modifier = Modifier.fillMaxSize(),
                            router = router,
                        )
                    }

                    else -> Unit
                }
            }
        }
    }
}

/**
 * Renders routes that sit above the home scaffold and own their full-screen layout.
 *
 * @param route Route currently requested by the root router.
 * @param koin Presentation Koin instance used to resolve route dependencies.
 * @param router Root router passed to overlay screens.
 * @param urlLauncher Platform external URL launcher used by web/report flows.
 * @param imageToImageViewModel Shared Image-to-Image ViewModel reused by in-paint overlay flow.
 * @author Dmitriy Moroz
 */
@Composable
internal fun OverlayRouteContent(
    route: AppRoute,
    koin: Koin,
    router: RootAppRouter,
    urlLauncher: ExternalUrlLauncher,
    imageToImageViewModel: ImageToImageViewModel,
) {
    when (route) {
        AppRoute.Home,
        AppRoute.TextToImage,
        AppRoute.ImageToImage,
        AppRoute.Upscaler,
        AppRoute.Gallery,
        AppRoute.Settings,
            -> Unit

        AppRoute.BackupWelcome -> BackupScreen(
            mode = BackupScreenMode.WELCOME, router = router, modifier = Modifier.fillMaxSize(),
        )

        is AppRoute.Backup -> BackupScreen(
            mode = if (route.source == LaunchSource.SPLASH) BackupScreenMode.FIRST_LAUNCH else BackupScreenMode.SETTINGS,
            router = router,
            modifier = Modifier.fillMaxSize(),
        )

        AppRoute.Civitai -> CivitaiScreen(router = router)
        AppRoute.CivitaiHistory -> CivitaiHistoryScreen(router = router)

        AppRoute.StorageUsage -> {
            StorageUsageScreen(
                modifier = Modifier.fillMaxSize(),
                router = router,
            )
        }

        AppRoute.NetworkUsage -> {
            NetworkUsageScreen(
                modifier = Modifier.fillMaxSize(),
                router = router,
            )
        }

        AppRoute.Splash -> SplashScreen(router = router)

        is AppRoute.OnBoarding -> OnBoardingScreen(launchSource = route.source, router = router)

        is AppRoute.Setup -> if (route.isUpscaler) UpscalerSetupScreen(router) else ServerSetupScreen(
            launchSource = route.source,
            router = router,
            urlLauncher = urlLauncher,
        )

        AppRoute.ConfigurationLoader -> ConfigurationLoaderScreen(router = router)

        AppRoute.ImageInPaint -> ImageInPaintScreen(
            viewModel = imageToImageViewModel,
            onClose = router::navigateBack,
        )

        is AppRoute.GalleryDetail -> {
            GalleryDetailScreen(
                itemId = route.itemId,
                router = router,
                civitaiAction = { id ->
                    val uploader =
                        remember(koin, id) { koin.get<CivitaiUploadViewModel> { parametersOf(id) } }
                    CivitaiUploadAction(uploader, router::navigateToCivitai, urlLauncher::openUrl)
                },
            )
        }

        AppRoute.History -> {
            HistoryScreen(router = router)
        }

        AppRoute.Benchmark -> {
            BenchmarkScreen(router = router)
        }

        is AppRoute.Report -> {
            ReportScreen(
                itemId = route.itemId,
                router = router,
            )
        }

        AppRoute.Debug -> {
            DebugMenuScreen(router = router)
        }

        AppRoute.Logger -> {
            LoggerScreen(router = router)
        }

        AppRoute.Donate -> {
            DonateScreen(router = router)
        }

        AppRoute.Faq -> FaqScreen(onBack = router::navigateBack)
        AppRoute.Blog -> BlogScreen(
            onBack = router::navigateBack,
            onOpenPost = router::navigateToBlogDetail
        )

        is AppRoute.BlogDetail -> BlogDetailScreen(slug = route.slug, onBack = router::navigateBack)

        AppRoute.WebUi -> {
            WebUiScreen(router = router)
        }
    }
}
