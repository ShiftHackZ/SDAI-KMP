package com.shifthackz.aisdv1.presentation.app

import com.shifthackz.aisdv1.presentation.home.AppScaffold
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.presentation.backup.BackupScreen
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode
import com.shifthackz.aisdv1.presentation.backup.BackupViewModel
import com.shifthackz.aisdv1.presentation.backup.BackupIntent
import com.shifthackz.aisdv1.presentation.backup.BackupEffect
import com.shifthackz.aisdv1.presentation.backup.platform.rememberBackupDocumentLauncher

import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.presentation.components.platform.ExternalUrlLauncher
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkScreen
import com.shifthackz.aisdv1.presentation.debug.DebugMenuScreen
import com.shifthackz.aisdv1.presentation.donate.DonateScreen
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailScreen
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryScreen
import com.shifthackz.aisdv1.presentation.history.HistoryScreenContent
import com.shifthackz.aisdv1.presentation.history.HistoryViewModel
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageInPaintScreenContent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageContent
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderScreenContent
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderViewModel
import com.shifthackz.aisdv1.presentation.configuration.loader.toContentState
import com.shifthackz.aisdv1.presentation.logger.LoggerScreen
import com.shifthackz.aisdv1.presentation.statistics.networkusage.NetworkUsageScreen
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingContent
import com.shifthackz.aisdv1.presentation.onboarding.OnBoardingViewModel
import com.shifthackz.aisdv1.presentation.report.ReportScreen
import com.shifthackz.aisdv1.presentation.civitai.upload.CivitaiUploadViewModel
import com.shifthackz.aisdv1.presentation.civitai.upload.CivitaiUploadAction
import com.shifthackz.aisdv1.presentation.civitai.settings.CivitaiScreen
import com.shifthackz.aisdv1.presentation.civitai.history.CivitaiHistoryScreen
import com.shifthackz.aisdv1.presentation.settings.SettingsScreen
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupViewModel
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupContent
import com.shifthackz.aisdv1.presentation.configuration.setup.platform.rememberServerSetupEffectHandler
import com.shifthackz.aisdv1.presentation.splash.SplashScreenContent
import com.shifthackz.aisdv1.presentation.splash.SplashViewModel
import com.shifthackz.aisdv1.presentation.statistics.storageusage.StorageUsageScreen
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageContent
import com.shifthackz.aisdv1.presentation.generation.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.web.WebUiScreen
import com.shifthackz.aisdv1.presentation.components.theme.global.RetainedHomeRouteTheme
import com.shifthackz.aisdv1.presentation.components.widget.navigation.ProgrammaticPager
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.image.DecodedImageBitmap
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.rememberOriginalImage
import com.shifthackz.aisdv1.presentation.app.onboarding.FormPageContent
import com.shifthackz.aisdv1.presentation.app.onboarding.ProviderPageContent
import com.shifthackz.aisdv1.presentation.app.onboarding.LocalDiffusionPageContent
import com.shifthackz.aisdv1.presentation.app.onboarding.LookAndFeelPageContent
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationPerformanceUseCase
import com.shifthackz.aisdv1.presentation.generation.performance.GenerationPerformanceOverlay
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
            listOf(AppRoute.TextToImage, AppRoute.ImageToImage, AppRoute.Gallery, AppRoute.Settings)
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
        AppRoute.Gallery,
        AppRoute.Settings,
            -> Unit

        AppRoute.BackupWelcome, is AppRoute.Backup -> {
            val mode = when (route) {
                AppRoute.BackupWelcome -> BackupScreenMode.WELCOME
                is AppRoute.Backup -> if (route.source == LaunchSource.SPLASH) BackupScreenMode.FIRST_LAUNCH else BackupScreenMode.SETTINGS
            }
            val viewModel = remember(koin, router, mode) {
                koin.get<BackupViewModel> {
                    parametersOf(
                        mode,
                        router
                    )
                }
            }
            val launcher = rememberBackupDocumentLauncher(
                onPicked = { viewModel.processIntent(BackupIntent.DocumentSelected(it)) },
                onExported = { destination, hasExported, handle ->
                    viewModel.processIntent(
                        BackupIntent.ExportFinished(
                            destination,
                            hasExported,
                            handle
                        )
                    )
                },
            )
            DisposableEffect(viewModel) { onDispose { viewModel.onRouteDisposed() } }
            MviComponent(viewModel = viewModel, processEffect = { effect ->
                when (effect) {
                    BackupEffect.OpenDocument -> launcher.open()
                    is BackupEffect.ExportDocument -> launcher.export(effect.backup)
                }
            }) { state, processIntent ->
                BackupScreen(
                    mode,
                    state,
                    processIntent,
                    Modifier.fillMaxSize()
                )
            }
        }

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

        AppRoute.Splash -> {
            val splashViewModel = remember(koin, router) {
                koin.get<SplashViewModel> {
                    parametersOf(router)
                }
            }
            MviComponent(viewModel = splashViewModel) {
                SplashScreenContent()
            }
        }

        is AppRoute.OnBoarding -> {
            val source = route.source
            val onBoardingViewModel = remember(koin, router, source) {
                koin.get<OnBoardingViewModel> {
                    parametersOf(source, router)
                }
            }
            MviComponent(
                viewModel = onBoardingViewModel,
            ) { state, processIntent ->
                OnBoardingContent(
                    formPreview = { FormPageContent() },
                    providerPreview = { ProviderPageContent(isPageVisible = it) },
                    localPreview = { LocalDiffusionPageContent() },
                    settingsPreview = { token, version, isVisible ->
                        LookAndFeelPageContent(
                            darkThemeToken = token,
                            appVersion = version,
                            isPageVisible = isVisible
                        )
                    },
                    launchSource = onBoardingViewModel.launchSource,
                    state = state,
                    processIntent = processIntent,
                )
            }
        }

        is AppRoute.Setup -> {
            val source = route.source
            val setupViewModel = remember(koin, router, source) {
                koin.get<ServerSetupViewModel> {
                    parametersOf(source, router)
                }
            }
            val effectHandler = rememberServerSetupEffectHandler(urlLauncher)
            DisposableEffect(setupViewModel) {
                onDispose { setupViewModel.onRouteDisposed() }
            }
            MviComponent(
                viewModel = setupViewModel,
                processEffect = effectHandler,
            ) { state, processIntent ->
                ServerSetupContent(
                    state = state,
                    processIntent = processIntent,
                )
            }
        }

        AppRoute.ConfigurationLoader -> {
            val loaderViewModel = remember(koin, router) {
                koin.get<ConfigurationLoaderViewModel> {
                    parametersOf(router)
                }
            }
            MviComponent(
                viewModel = loaderViewModel,
            ) { state ->
                ConfigurationLoaderScreenContent(
                    state = state.toContentState(),
                )
            }
        }

        AppRoute.ImageInPaint -> {
            val state by imageToImageViewModel.state.collectAsState()
            val decoded = rememberOriginalImage(state.imageBase64)
            val image = (decoded as? DecodedImageBitmap.Ready)?.image
            if (decoded == DecodedImageBitmap.Unavailable) {
                ErrorDialog(
                    text = Localization.string("error_generic").asUiText(),
                    onDismissRequest = router::navigateBack
                )
            } else if (image != null) {
                ImageInPaintScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    image = image,
                    state = state.inPaint,
                    processIntent = imageToImageViewModel::processIntent,
                    onClose = router::navigateBack,
                )
            }
        }

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
            val historyViewModel = remember(koin, router) {
                koin.get<HistoryViewModel> {
                    parametersOf(router)
                }
            }
            MviComponent(
                viewModel = historyViewModel,
            ) { state, processIntent ->
                HistoryScreenContent(
                    imageLoader = remember(koin) { koin.get<GalleryImageLoader>() },
                    state = state,
                    processIntent = processIntent,
                )
            }
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

        AppRoute.WebUi -> {
            WebUiScreen(router = router)
        }
    }
}
