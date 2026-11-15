package com.shifthackz.aisdv1.presentation.di

import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.presentation.widget.image.OriginalImageLoader
import com.shifthackz.aisdv1.presentation.widget.image.BoundedOriginalImageLoader
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.createPlatformBuildInfoProvider
import com.shifthackz.aisdv1.core.common.links.DefaultLinksProvider
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DefaultDispatchersProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.DefaultTimeProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.presentation.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.core.NoOpGenerationPlatformServices
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import com.shifthackz.aisdv1.core.navigation.router.BenchmarkRouter
import com.shifthackz.aisdv1.core.navigation.router.DebugMenuRouter
import com.shifthackz.aisdv1.core.navigation.router.DonateRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryDetailRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryRouter
import com.shifthackz.aisdv1.core.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import com.shifthackz.aisdv1.core.navigation.router.NetworkUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpConfigurationLoaderRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpBenchmarkRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpDebugMenuRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpDonateRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpGalleryDetailRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpGalleryRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpImageToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpLoggerRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpNetworkUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpOnBoardingRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpReportRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpServerSetupRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpSettingsRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpSplashRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpStorageUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpTextToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpWebUiRouter
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.core.navigation.router.ReportRouter
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.core.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import com.shifthackz.aisdv1.core.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.TextToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter
import com.shifthackz.aisdv1.presentation.screen.benchmark.BenchmarkPlatformActions
import com.shifthackz.aisdv1.presentation.screen.benchmark.createDefaultBenchmarkPlatformActions
import com.shifthackz.aisdv1.presentation.screen.debug.UiDiagnosticReportActions
import com.shifthackz.aisdv1.presentation.screen.debug.NoOpUiDiagnosticReportActions
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuAccessor
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.screen.debug.NoOpDebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.createDefaultGalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.screen.gallery.list.GalleryExportService
import com.shifthackz.aisdv1.presentation.screen.gallery.list.GalleryPlatformActions
import com.shifthackz.aisdv1.presentation.screen.gallery.list.NoOpGalleryExportService
import com.shifthackz.aisdv1.presentation.screen.gallery.list.NoOpGalleryPlatformActions
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.img2img.NoOpImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.logger.LogReader
import com.shifthackz.aisdv1.presentation.screen.logger.LoggerPlatformActions
import com.shifthackz.aisdv1.presentation.screen.logger.NoOpLogReader
import com.shifthackz.aisdv1.presentation.screen.logger.NoOpLoggerPlatformActions
import com.shifthackz.aisdv1.presentation.screen.settings.platform.NoOpSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.screen.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.screen.setup.platform.NoOpServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.screen.setup.platform.ServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.screen.txt2img.ImageSaver
import com.shifthackz.aisdv1.presentation.screen.txt2img.ImageSharer
import com.shifthackz.aisdv1.presentation.screen.txt2img.createPlatformImageSaver
import com.shifthackz.aisdv1.presentation.screen.txt2img.createPlatformImageSharer
import com.shifthackz.aisdv1.presentation.widget.work.BackgroundWorkImageLoader
import com.shifthackz.aisdv1.presentation.widget.work.NoOpBackgroundWorkImageLoader
import com.shifthackz.aisdv1.core.mvi.diagnostics.uiDiagnosticsModule
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailContentCache
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.BoundedGalleryDetailContentCache
import org.koin.core.module.Module
import com.shifthackz.aisdv1.presentation.widget.image.GalleryMemoryBudget
import com.shifthackz.aisdv1.presentation.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.widget.image.GalleryImageDecoder
import com.shifthackz.aisdv1.presentation.widget.image.PlatformGalleryImageDecoder
import com.shifthackz.aisdv1.presentation.widget.image.BoundedGalleryImageLoader

/**
 * Registers presentation-layer singleton services and default no-op router bindings.
 *
 * Feature screens replace these no-op routers from the app shell, while tests and previews can use
 * the same module without requiring a full navigation host.
 *
 * @receiver Koin module receiving presentation-level service and router bindings.
 *
 * @author Dmitriy Moroz
 */
internal fun Module.registerPresentationCoreBindings() {
    single {
        GalleryMemoryBudget(
            cacheEntries = 32, cacheBytes = 24L * 1024 * 1024, concurrentDecodes = 2,
            encodedChars = 32 * 1024 * 1024, maxDisplayEdge = 2048, detailEntries = 3,
            detailMetadataBytes = 2L * 1024 * 1024, metadataWindow = 120, metadataPrefetch = 30
        )
    }
    factory<GalleryDetailContentCache> { BoundedGalleryDetailContentCache(get()) }
    single<GalleryImageDecoder> { PlatformGalleryImageDecoder() }
    single { OriginalImageBudget(encodedChars = 32 * 1024 * 1024, maxPixels = 16_777_216L) }
    single<OriginalImageLoader> { BoundedOriginalImageLoader(get(), get()) }
    single<GalleryImageLoader> { BoundedGalleryImageLoader(get(), get(), get(), get()) }
    includes(uiDiagnosticsModule(isEnabled = false))
    single<UiDiagnosticReportActions> { NoOpUiDiagnosticReportActions }
    single<LinksProvider> { DefaultLinksProvider }
    single<BuildInfoProvider> { createPlatformBuildInfoProvider() }
    single<DispatchersProvider> { DefaultDispatchersProvider }
    single<TimeProvider> { DefaultTimeProvider }
    single<GenerationPlatformServices> { NoOpGenerationPlatformServices }
    single<DebugMenuRouter> { NoOpDebugMenuRouter }
    single<DonateRouter> { NoOpDonateRouter }
    single<LoggerRouter> { NoOpLoggerRouter }
    single<OnBoardingRouter> { NoOpOnBoardingRouter }
    single<ReportRouter> { NoOpReportRouter }
    single<GalleryRouter> { NoOpGalleryRouter }
    single<GalleryDetailRouter> { NoOpGalleryDetailRouter }
    single<TextToImageRouter> { NoOpTextToImageRouter }
    single<ImageToImageRouter> { NoOpImageToImageRouter }
    single<SplashRouter> { NoOpSplashRouter }
    single<ConfigurationLoaderRouter> { NoOpConfigurationLoaderRouter }
    single<BenchmarkRouter> { NoOpBenchmarkRouter }
    single<BenchmarkPlatformActions> { createDefaultBenchmarkPlatformActions() }
    single<ServerSetupRouter> { NoOpServerSetupRouter }
    single<SettingsRouter> { NoOpSettingsRouter }
    single<StorageUsageRouter> { NoOpStorageUsageRouter }
    single<NetworkUsageRouter> { NoOpNetworkUsageRouter }
    single<WebUiRouter> { NoOpWebUiRouter }
    single<LogReader> { NoOpLogReader }
    single<LoggerPlatformActions> { NoOpLoggerPlatformActions }
    single<DebugMenuPlatformActions> { NoOpDebugMenuPlatformActions }
    single<SettingsPlatformActions> { NoOpSettingsPlatformActions }
    single<BackgroundWorkImageLoader> { NoOpBackgroundWorkImageLoader }
    single<GalleryExportService> { NoOpGalleryExportService }
    single<GalleryPlatformActions> { NoOpGalleryPlatformActions }
    single<GalleryDetailPlatformActions> { createDefaultGalleryDetailPlatformActions() }
    single<ImageToImagePlatformActions> { NoOpImageToImagePlatformActions }
    single<ServerSetupDownloadGuard> { NoOpServerSetupDownloadGuard }
    single<ImageSaver> { createPlatformImageSaver() }
    single<ImageSharer> { createPlatformImageSharer() }
    single { GenerationFormUpdateEvent() }
    single { DebugMenuAccessor(get()) }
}
