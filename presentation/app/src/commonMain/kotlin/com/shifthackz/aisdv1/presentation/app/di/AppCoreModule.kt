package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.createPlatformBuildInfoProvider
import com.shifthackz.aisdv1.core.common.links.DefaultLinksProvider
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DefaultDispatchersProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.DefaultTimeProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
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
import com.shifthackz.aisdv1.core.mvi.diagnostics.uiDiagnosticsModule
import com.shifthackz.aisdv1.core.navigation.router.BackupRouter
import com.shifthackz.aisdv1.core.navigation.router.NoOpBackupRouter
import org.koin.dsl.module

/**
 * Registers process-shared build, links, dispatcher and clock defaults plus inert router bindings.
 * Diagnostics are disabled by default; native startup may replace platform/configuration bindings before resolution.
 *
 * @author Dmitriy Moroz
 */
val appCoreModule = module {
    single<BackupRouter> { NoOpBackupRouter }
    includes(uiDiagnosticsModule(isEnabled = false))
    single<LinksProvider> { DefaultLinksProvider }
    single<BuildInfoProvider> { createPlatformBuildInfoProvider() }
    single<DispatchersProvider> { DefaultDispatchersProvider }
    single<TimeProvider> { DefaultTimeProvider }
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
    single<ServerSetupRouter> { NoOpServerSetupRouter }
    single<SettingsRouter> { NoOpSettingsRouter }
    single<StorageUsageRouter> { NoOpStorageUsageRouter }
    single<NetworkUsageRouter> { NoOpNetworkUsageRouter }
    single<WebUiRouter> { NoOpWebUiRouter }
}
