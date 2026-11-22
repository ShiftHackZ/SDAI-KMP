package com.shifthackz.aisdv1.presentation.settings

/**
 * Matches explanatory help to the same localized labels used by Settings controls.
 * Warnings already display their complete explanation; the attribution footer has no setting to explain.
 *
 * @receiver Stable Settings row identity, independent of its current value or loading state.
 * @return Canonical label key, or null for warnings and attribution.
 * @author Dmitriy Moroz
 */
internal fun SettingsListItem.helpTitleKey(): String? = when (this) {
    SettingsListItem.INTEGRATIONS_HEADER -> "settings_header_integrations"
    SettingsListItem.WEB_UI -> "drawer_web_ui"
    SettingsListItem.CIVITAI -> "civitai_title"
    SettingsListItem.SERVER_HEADER -> "settings_header_server"
    SettingsListItem.CONFIGURATION -> "settings_item_config"
    SettingsListItem.BACKUP -> "backup_title"
    SettingsListItem.BENCHMARK -> "settings_item_benchmark"
    SettingsListItem.CREDITS -> "settings_item_stability_ai_credits"
    SettingsListItem.CONNECTION_TIMEOUT -> "settings_connection_timeout"
    SettingsListItem.GENERATION_TIMEOUT -> "settings_generation_timeout"
    SettingsListItem.MODEL -> "settings_item_sd_model"
    SettingsListItem.NNAPI -> "settings_item_local_nnapi"
    SettingsListItem.BACKGROUND_GENERATION -> "settings_item_background_generation"
    SettingsListItem.DEBUG_MENU -> "title_debug_menu"
    SettingsListItem.APP_HEADER -> "settings_header_app"
    SettingsListItem.PERFORMANCE_OVERLAY -> "settings_item_performance_overlay"
    SettingsListItem.DIM_SCREEN -> "settings_item_dim_screen"
    SettingsListItem.MONITOR_CONNECTION -> "settings_item_monitor_connection"
    SettingsListItem.AUTO_SAVE -> "settings_item_auto_save"
    SettingsListItem.MEDIA_STORE -> "settings_item_auto_save_media_store"
    SettingsListItem.TAGGED_INPUT -> "settings_item_tagged_input"
    SettingsListItem.ADVANCED_DEFAULT -> "settings_item_advanced_form_default"
    SettingsListItem.STORAGE_USAGE -> "settings_item_storage_usage"
    SettingsListItem.NETWORK_USAGE -> "settings_item_network_usage"
    SettingsListItem.LOOK_HEADER -> "settings_header_look_and_feel"
    SettingsListItem.LANGUAGE -> "settings_item_lf_lang"
    SettingsListItem.GALLERY_GRID -> "settings_item_lf_gallery_grid"
    SettingsListItem.DYNAMIC_COLORS -> "settings_item_lf_dynamic_colors"
    SettingsListItem.SYSTEM_DARK -> "settings_item_lf_system_dark_theme"
    SettingsListItem.MANUAL_DARK -> "settings_item_lf_dark_theme"
    SettingsListItem.DARK_PALETTE -> "settings_item_lf_dark_theme_color"
    SettingsListItem.ACCENT_COLOR -> "settings_item_lf_accent_color"
    SettingsListItem.COMMUNITY_HEADER -> "settings_header_community"
    SettingsListItem.TELEGRAM -> "settings_item_telegram"
    SettingsListItem.DISCORD -> "settings_item_discord"
    SettingsListItem.LINKED_IN -> "settings_item_linkedin"
    SettingsListItem.PROJECT_WEBSITE -> "settings_item_project_website"
    SettingsListItem.DEVELOPER_WEBSITE -> "settings_item_developer_website"
    SettingsListItem.INFO_HEADER -> "settings_header_info"
    SettingsListItem.DONATE -> "settings_item_donate"
    SettingsListItem.ONBOARDING -> "settings_item_on_boarding"
    SettingsListItem.REPORT_PROBLEM -> "settings_item_report_problem"
    SettingsListItem.PRIVACY -> "settings_item_policy"
    SettingsListItem.LICENSE -> "settings_item_license"
    SettingsListItem.SOURCE_CODE -> "settings_item_source"
    SettingsListItem.NNAPI_WARNING,
    SettingsListItem.BACKGROUND_WARNING,
    SettingsListItem.DIM_SCREEN_WARNING,
    SettingsListItem.AUTO_SAVE_WARNING,
    SettingsListItem.VERSION -> null
}
