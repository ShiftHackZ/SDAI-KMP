package com.shifthackz.aisdv1.presentation.settings

/**
 * Allows compatible Settings items to share recycled composition slots and prefetch history.
 * Distinct stable row keys still isolate remembered state and user actions between preferences.
 *
 * @receiver Logical Settings item selected by current provider and appearance visibility.
 * @return Structural category of the row's rendered content.
 * @author Dmitriy Moroz
 */
internal fun SettingsListItem.contentType(): SettingsContentType = when (this) {
    SettingsListItem.INTEGRATIONS_HEADER,
    SettingsListItem.SERVER_HEADER,
    SettingsListItem.APP_HEADER,
    SettingsListItem.LOOK_HEADER,
    SettingsListItem.COMMUNITY_HEADER,
    SettingsListItem.INFO_HEADER -> SettingsContentType.HEADER

    SettingsListItem.PERFORMANCE_OVERLAY,
    SettingsListItem.DIM_SCREEN,
    SettingsListItem.NNAPI,
    SettingsListItem.BACKGROUND_GENERATION,
    SettingsListItem.MONITOR_CONNECTION,
    SettingsListItem.AUTO_SAVE,
    SettingsListItem.MEDIA_STORE,
    SettingsListItem.TAGGED_INPUT,
    SettingsListItem.ADVANCED_DEFAULT,
    SettingsListItem.DYNAMIC_COLORS,
    SettingsListItem.SYSTEM_DARK,
    SettingsListItem.MANUAL_DARK -> SettingsContentType.SWITCH

    SettingsListItem.DIM_SCREEN_WARNING,
    SettingsListItem.NNAPI_WARNING,
    SettingsListItem.BACKGROUND_WARNING,
    SettingsListItem.AUTO_SAVE_WARNING -> SettingsContentType.WARNING

    SettingsListItem.DARK_PALETTE,
    SettingsListItem.ACCENT_COLOR -> SettingsContentType.COLOR_SELECTOR

    SettingsListItem.VERSION -> SettingsContentType.FOOTER

    SettingsListItem.CIVITAI,
    SettingsListItem.WEB_UI,
    SettingsListItem.CONFIGURATION,
    SettingsListItem.BACKUP,
    SettingsListItem.BENCHMARK,
    SettingsListItem.CREDITS,
    SettingsListItem.CONNECTION_TIMEOUT,
    SettingsListItem.GENERATION_TIMEOUT,
    SettingsListItem.MODEL,
    SettingsListItem.DEBUG_MENU,
    SettingsListItem.STORAGE_USAGE,
    SettingsListItem.NETWORK_USAGE,
    SettingsListItem.LANGUAGE,
    SettingsListItem.GALLERY_GRID,
    SettingsListItem.TELEGRAM,
    SettingsListItem.DISCORD,
    SettingsListItem.LINKED_IN,
    SettingsListItem.PROJECT_WEBSITE,
    SettingsListItem.DEVELOPER_WEBSITE,
    SettingsListItem.DONATE,
    SettingsListItem.ONBOARDING,
    SettingsListItem.REPORT_PROBLEM,
    SettingsListItem.PRIVACY,
    SettingsListItem.LICENSE,
    SettingsListItem.SOURCE_CODE -> SettingsContentType.ROW

}
