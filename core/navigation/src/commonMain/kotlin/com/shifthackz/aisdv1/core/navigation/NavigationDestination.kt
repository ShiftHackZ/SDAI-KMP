package com.shifthackz.aisdv1.core.navigation

/**
 * Code-owned destination identities shared by routes and UI diagnostics. Never include navigation arguments or user data.
 *
 * @author Dmitriy Moroz
 */
enum class NavigationDestination {
    SPLASH,
    BACKUP,
    BACKUP_WELCOME,
    ON_BOARDING,
    SERVER_SETUP,
    CONFIGURATION_LOADER,
    HOME,
    TEXT_TO_IMAGE,
    IMAGE_TO_IMAGE,
    IMAGE_IN_PAINT,
    GALLERY,
    GALLERY_DETAIL,
    SETTINGS,
    CIVITAI,
    CIVITAI_HISTORY,
    STORAGE_USAGE,
    NETWORK_USAGE,
    BENCHMARK,
    HISTORY,
    REPORT,
    DEBUG_MENU,
    LOGGER,
    DONATE,
    WEB_UI,
}
