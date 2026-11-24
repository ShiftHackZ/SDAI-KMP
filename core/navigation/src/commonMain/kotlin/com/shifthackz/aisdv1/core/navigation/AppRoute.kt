package com.shifthackz.aisdv1.core.navigation

/**
 * Platform-neutral application destinations and navigation payloads. Diagnostics consume only [destination], never a route instance or its arguments.
 *
 * @author Dmitriy Moroz
 */
sealed interface AppRoute {
    /**
     * Fixed destination identity without route arguments; safe for aggregate diagnostics.
     *
     * @author Dmitriy Moroz
     */
    val destination: NavigationDestination

    /**
     * Reusable backup route carrying settings or first-launch completion behavior.
     *
     * @param source Caller context; SPLASH requires configuration on exit.
     * @author Dmitriy Moroz
     */
    data class Backup(val source: LaunchSource = LaunchSource.SETTINGS) : AppRoute {
        override val destination = NavigationDestination.BACKUP
    }

    /**
     * First-launch restore invitation, which cannot return to completed onboarding.
     *
     * @author Dmitriy Moroz
     */
    data object BackupWelcome : AppRoute {
        override val destination = NavigationDestination.BACKUP_WELCOME
    }

    /**
     * Initial launch gate before onboarding, setup or configuration loading.
     *
     * @author Dmitriy Moroz
     */
    data object Splash : AppRoute {
        override val destination = NavigationDestination.SPLASH
    }

    /**
     * Onboarding flow carrying the caller context for completion behavior.
     *
     * @param source Launch context controlling setup/onboarding completion behavior.
     * @author Dmitriy Moroz
     */
    data class OnBoarding(val source: LaunchSource = LaunchSource.SPLASH) : AppRoute {
        override val destination = NavigationDestination.ON_BOARDING
    }

    /**
     * Provider/server configuration flow carrying its caller context.
     *
     * @param source Launch context controlling setup/onboarding completion behavior.
     * @author Dmitriy Moroz
     */
    data class Setup(val source: LaunchSource = LaunchSource.SPLASH) : AppRoute {
        override val destination = NavigationDestination.SERVER_SETUP
    }

    /**
     * Configuration loading gate before the generation home screen.
     *
     * @author Dmitriy Moroz
     */
    data object ConfigurationLoader : AppRoute {
        override val destination = NavigationDestination.CONFIGURATION_LOADER
    }

    /**
     * Legacy home entry rendered as the text-to-image home screen.
     *
     * @author Dmitriy Moroz
     */
    data object Home : AppRoute {
        override val destination = NavigationDestination.HOME
    }

    /**
     * Text-to-image home tab.
     *
     * @author Dmitriy Moroz
     */
    data object TextToImage : AppRoute {
        override val destination = NavigationDestination.TEXT_TO_IMAGE
    }

    /**
     * Image-to-image home tab.
     *
     * @author Dmitriy Moroz
     */
    data object ImageToImage : AppRoute {
        override val destination = NavigationDestination.IMAGE_TO_IMAGE
    }

    /**
     * Inpainting editor opened from image-to-image.
     *
     * @author Dmitriy Moroz
     */
    data object ImageInPaint : AppRoute {
        override val destination = NavigationDestination.IMAGE_IN_PAINT
    }

    /**
     * Gallery home tab.
     *
     * @author Dmitriy Moroz
     */
    data object Gallery : AppRoute {
        override val destination = NavigationDestination.GALLERY
    }

    /**
     * Detail view for a persisted gallery result.
     *
     * @param itemId Persisted gallery result identity.
     * @author Dmitriy Moroz
     */
    data class GalleryDetail(val itemId: Long) : AppRoute {
        override val destination = NavigationDestination.GALLERY_DETAIL
    }

    /**
     * Settings home tab.
     *
     * @author Dmitriy Moroz
     */
    data object Settings : AppRoute {
        override val destination = NavigationDestination.SETTINGS
    }

    /**
     * Standalone storage usage screen.
     *
     * @author Dmitriy Moroz
     */
    data object Civitai : AppRoute {
        override val destination = NavigationDestination.CIVITAI
    }

    /**
     * Paginated local Civitai receipts, available independently of account authorization.
     *
     * @author Dmitriy Moroz
     */
    data object CivitaiHistory : AppRoute {
        override val destination = NavigationDestination.CIVITAI_HISTORY
    }

    /**
     * Standalone storage usage screen.
     *
     * @author Dmitriy Moroz
     */
    data object StorageUsage : AppRoute {
        override val destination = NavigationDestination.STORAGE_USAGE
    }

    /**
     * Standalone network usage screen.
     *
     * @author Dmitriy Moroz
     */
    data object NetworkUsage : AppRoute {
        override val destination = NavigationDestination.NETWORK_USAGE
    }

    /**
     * Hardware benchmark screen.
     *
     * @author Dmitriy Moroz
     */
    data object Benchmark : AppRoute {
        override val destination = NavigationDestination.BENCHMARK
    }

    /**
     * Generation request history screen.
     *
     * @author Dmitriy Moroz
     */
    data object History : AppRoute {
        override val destination = NavigationDestination.HISTORY
    }

    /**
     * Reporting flow for a persisted gallery result.
     *
     * @param itemId Persisted gallery result identity.
     * @author Dmitriy Moroz
     */
    data class Report(val itemId: Long) : AppRoute {
        override val destination = NavigationDestination.REPORT
    }

    /**
     * Developer debug menu.
     *
     * @author Dmitriy Moroz
     */
    data object Debug : AppRoute {
        override val destination = NavigationDestination.DEBUG_MENU
    }

    /**
     * Application log viewer.
     *
     * @author Dmitriy Moroz
     */
    data object Logger : AppRoute {
        override val destination = NavigationDestination.LOGGER
    }

    /**
     * Donation screen.
     *
     * @author Dmitriy Moroz
     */
    data object Donate : AppRoute {
        override val destination = NavigationDestination.DONATE
    }

    /**
     * Configured server web interface.
     *
     * @author Dmitriy Moroz
     */
    data object WebUi : AppRoute {
        override val destination = NavigationDestination.WEB_UI
    }

    /**
     * Searchable help content opened from Settings.
     *
     * @author Dmitriy Moroz
     */
    data object Faq : AppRoute {
        override val destination = NavigationDestination.FAQ
    }

    /**
     * Native list of published articles and release notes.
     *
     * @author Dmitriy Moroz
     */
    data object Blog : AppRoute {
        override val destination = NavigationDestination.BLOG
    }

    /**
     * Native reading destination for one published article.
     *
     * @param slug Identifies the article in the current feed or offline snapshot.
     * @author Dmitriy Moroz
     */
    data class BlogDetail(val slug: String) : AppRoute {
        override val destination = NavigationDestination.BLOG_DETAIL
    }

}
