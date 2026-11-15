package com.shifthackz.aisdv1.presentation.screen.settings.model

/**
 * Selects visible Settings rows without constructing their UI. The screen's lazy list composes
 * only viewport rows instead of mounting every section during a palette or preference change.
 *
 * @param state Current preferences and provider capabilities.
 * @param isDark Actual appearance including the system dark-mode selection.
 * @param shouldShowConnectionTimeout Whether this platform supports the connection timeout row.
 * @return Ordered stable row identities matching current provider and appearance visibility.
 * @author Dmitriy Moroz
 */
internal fun settingsListItems(
    state: SettingsState,
    isDark: Boolean,
    shouldShowConnectionTimeout: Boolean,
): List<SettingsListItem> = buildList {
    if (!state.onBoardingDemo) {
        add(SettingsListItem.SERVER_HEADER)
        add(SettingsListItem.CONFIGURATION)
        add(SettingsListItem.BENCHMARK)
        if (state.showStabilityAiCredits) add(SettingsListItem.CREDITS)
        if (state.showNetworkTimeoutSettings) {
            if (shouldShowConnectionTimeout) add(SettingsListItem.CONNECTION_TIMEOUT)
            add(SettingsListItem.GENERATION_TIMEOUT)
        }
        if (state.showSdModelSelector) add(SettingsListItem.MODEL)
        if (state.showLocalMicrosoftONNXUseNNAPI) {
            add(SettingsListItem.NNAPI)
            if (!state.loading) add(SettingsListItem.NNAPI_WARNING)
        }
        if (state.loading || state.backgroundGenerationAvailable) {
            add(SettingsListItem.BACKGROUND_GENERATION)
            if (!state.loading) add(SettingsListItem.BACKGROUND_WARNING)
        }
        if (!state.loading && state.developerMode) add(SettingsListItem.DEBUG_MENU)
        add(SettingsListItem.APP_HEADER)
        if (state.showMonitorConnectionOption) add(SettingsListItem.MONITOR_CONNECTION)
        add(SettingsListItem.AUTO_SAVE)
        if (!state.loading && state.backgroundGeneration) add(SettingsListItem.AUTO_SAVE_WARNING)
        add(SettingsListItem.MEDIA_STORE)
        add(SettingsListItem.TAGGED_INPUT)
        if (state.showFormAdvancedOption) add(SettingsListItem.ADVANCED_DEFAULT)
        add(SettingsListItem.STORAGE_USAGE)
        add(SettingsListItem.NETWORK_USAGE)
    }
    add(SettingsListItem.LOOK_HEADER)
    add(SettingsListItem.LANGUAGE)
    add(SettingsListItem.GALLERY_GRID)
    if (state.showUseSystemColorPalette) add(SettingsListItem.DYNAMIC_COLORS)
    add(SettingsListItem.SYSTEM_DARK)
    if (!state.useSystemDarkTheme) add(SettingsListItem.MANUAL_DARK)
    if (!state.loading && isDark) add(SettingsListItem.DARK_PALETTE)
    if (!state.loading) add(SettingsListItem.ACCENT_COLOR)
    if (!state.onBoardingDemo) {
        add(SettingsListItem.COMMUNITY_HEADER)
        add(SettingsListItem.TELEGRAM)
        add(SettingsListItem.DISCORD)
        add(SettingsListItem.PROJECT_WEBSITE)
        add(SettingsListItem.DEVELOPER_WEBSITE)
        add(SettingsListItem.INFO_HEADER)
        add(SettingsListItem.DONATE)
        add(SettingsListItem.ONBOARDING)
        add(SettingsListItem.REPORT_PROBLEM)
        add(SettingsListItem.PRIVACY)
        add(SettingsListItem.LICENSE)
        add(SettingsListItem.SOURCE_CODE)
    }
    if (state.appVersion.isNotEmpty()) add(SettingsListItem.VERSION)
}
