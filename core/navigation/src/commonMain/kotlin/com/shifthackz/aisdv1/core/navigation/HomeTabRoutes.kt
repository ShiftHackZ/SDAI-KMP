package com.shifthackz.aisdv1.core.navigation

/**
 * Maps a route back to the home tab that should stay selected underneath it.
 *
 * @author Dmitriy Moroz
 */
fun AppRoute.asHomeTabRoute(): AppRoute? = when (this) {
    AppRoute.Home,
    AppRoute.TextToImage,
        -> AppRoute.TextToImage

    AppRoute.ImageToImage -> AppRoute.ImageToImage
    AppRoute.Gallery -> AppRoute.Gallery
    AppRoute.Settings -> AppRoute.Settings

    AppRoute.ImageInPaint,
    AppRoute.Benchmark,
    AppRoute.StorageUsage,
    AppRoute.NetworkUsage,
    AppRoute.BackupWelcome,
    is AppRoute.Backup,
    AppRoute.Splash,
    is AppRoute.OnBoarding,
    is AppRoute.Setup,
    AppRoute.ConfigurationLoader,
    is AppRoute.GalleryDetail,
    AppRoute.History,
    is AppRoute.Report,
    AppRoute.Debug,
    AppRoute.Logger,
    AppRoute.Donate,
    AppRoute.WebUi,
        -> null
}
