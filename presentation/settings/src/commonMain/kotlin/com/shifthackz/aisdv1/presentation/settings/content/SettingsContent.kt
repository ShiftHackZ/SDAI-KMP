package com.shifthackz.aisdv1.presentation.settings.content

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsHeader
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsList
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsListDefaults
import com.shifthackz.aisdv1.core.model.asUiText
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.icon.BrandIcons
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalFloatingNavigationBarInset
import com.shifthackz.aisdv1.presentation.settings.SettingsIntent
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.SettingsState
import com.shifthackz.aisdv1.presentation.settings.contentType
import com.shifthackz.aisdv1.presentation.settings.platform.NoOpSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.section.SettingsCommunitySection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsInfoSection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsLookAndFeelSection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsServerSection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsVersionFooter
import com.shifthackz.aisdv1.presentation.settings.settingsListItems
import com.shifthackz.aisdv1.presentation.settings.startIcon
import com.shifthackz.aisdv1.presentation.components.widget.source.getName

/**
 * Renders visible Settings rows in one lazy list. Provider, appearance, community, information
 * and version entries retain stable identities while offscreen rows avoid composition on theme edits.
 *
 * @param state MVI state that controls section visibility and item values.
 * @param modifier Layout applied to the scrollable Settings body.
 * @param platformActions platform capabilities needed by settings rows.
 * @param processIntent sends user actions back to the Settings MVI pipeline.
 * @param listState Caller-owned scroll position retained while preference rows change.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ContentSettingsState(
    modifier: Modifier = Modifier,
    state: SettingsState,
    platformActions: SettingsPlatformActions = NoOpSettingsPlatformActions,
    processIntent: (SettingsIntent) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
) {
    val isDark = if (state.useSystemDarkTheme) {
        isSystemInDarkTheme()
    } else {
        state.darkTheme
    }
    val rows = settingsListItems(
        state,
        isDark,
        platformActions.supportsConnectionTimeout,
        platformActions.isNnapiAvailable,
    )
    SettingsList(
        modifier = modifier,
        bottomPadding = LocalFloatingNavigationBarInset.current,
        state = listState,
    ) {
        items(items = rows, key = { it.name }, contentType = { it.contentType() }) { row ->
            SettingsItemHelp(
                row = row,
                isLoading = state.loading,
                modifier = Modifier.animateItem()
            ) {
                val headerModifier = Modifier.padding(SettingsListDefaults.headerPadding)
                val itemModifier = Modifier.fillMaxWidth().padding(SettingsListDefaults.itemPadding)
                val warningModifier =
                    Modifier.fillMaxWidth().padding(SettingsListDefaults.supportingTextPadding)
                when (row) {
                    SettingsListItem.INTEGRATIONS_HEADER -> SettingsHeader(
                        text = Localization.string("settings_header_integrations").asUiText(),
                        modifier = headerModifier,
                    )

                    SettingsListItem.CIVITAI -> SettingsItem(
                        text = Localization.string("civitai_title").asUiText(),
                        startIconContent = {
                            Image(
                                BrandIcons.Civitai,
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 8.dp).size(24.dp)
                            )
                        },
                        modifier = itemModifier,
                        onClick = { processIntent(SettingsIntent.NavigateCivitai) },
                    )

                    SettingsListItem.WEB_UI -> SettingsItem(
                        text = "${Localization.string("drawer_web_ui")} (${
                            state.serverSource.getName(
                                state.platform
                            )
                        })".asUiText(),
                        startIcon = row.startIcon,
                        modifier = itemModifier,
                        onClick = { processIntent(SettingsIntent.NavigateWebUi) },
                    )

                    SettingsListItem.SERVER_HEADER,
                    SettingsListItem.CONFIGURATION,
                    SettingsListItem.UPSCALER_CONFIGURATION,
                    SettingsListItem.BACKUP,
                    SettingsListItem.BENCHMARK,
                    SettingsListItem.CREDITS,
                    SettingsListItem.CONNECTION_TIMEOUT,
                    SettingsListItem.GENERATION_TIMEOUT,
                    SettingsListItem.MODEL,
                    SettingsListItem.NNAPI,
                    SettingsListItem.NNAPI_WARNING,
                    SettingsListItem.BACKGROUND_GENERATION,
                    SettingsListItem.BACKGROUND_WARNING,
                    SettingsListItem.DEBUG_MENU,
                    SettingsListItem.APP_HEADER,
                    SettingsListItem.PERFORMANCE_OVERLAY,
                    SettingsListItem.DIM_SCREEN,
                    SettingsListItem.DIM_SCREEN_WARNING,
                    SettingsListItem.MONITOR_CONNECTION,
                    SettingsListItem.AUTO_SAVE,
                    SettingsListItem.AUTO_SAVE_WARNING,
                    SettingsListItem.MEDIA_STORE,
                    SettingsListItem.TAGGED_INPUT,
                    SettingsListItem.ADVANCED_DEFAULT,
                    SettingsListItem.STORAGE_USAGE,
                    SettingsListItem.NETWORK_USAGE -> SettingsServerSection(
                        row = row,
                        state = state,
                        headerModifier = headerModifier,
                        itemModifier = itemModifier,
                        warningModifier = warningModifier,
                        backgroundGenerationWarningKey = platformActions.backgroundGenerationWarningKey,
                        processIntent = processIntent,
                    )

                    SettingsListItem.LOOK_HEADER,
                    SettingsListItem.LANGUAGE,
                    SettingsListItem.GALLERY_GRID,
                    SettingsListItem.DYNAMIC_COLORS,
                    SettingsListItem.SYSTEM_DARK,
                    SettingsListItem.MANUAL_DARK,
                    SettingsListItem.DARK_PALETTE,
                    SettingsListItem.ACCENT_COLOR -> SettingsLookAndFeelSection(
                        row = row,
                        isLoading = state.loading,
                        galleryGrid = state.galleryGrid,
                        shouldUseSystemColorPalette = state.useSystemColorPalette,
                        shouldUseSystemDarkTheme = state.useSystemDarkTheme,
                        isDarkTheme = state.darkTheme,
                        darkThemeToken = state.darkThemeToken,
                        colorToken = state.colorToken,
                        isDark = isDark,
                        headerModifier = headerModifier,
                        itemModifier = itemModifier,
                        processIntent = processIntent,
                    )

                    SettingsListItem.COMMUNITY_HEADER,
                    SettingsListItem.TELEGRAM,
                    SettingsListItem.DISCORD,
                    SettingsListItem.LINKED_IN,
                    SettingsListItem.PROJECT_WEBSITE,
                    SettingsListItem.DEVELOPER_WEBSITE -> SettingsCommunitySection(
                        row = row,
                        isLoading = state.loading,
                        headerModifier = headerModifier,
                        itemModifier = itemModifier,
                        processIntent = processIntent,
                    )

                    SettingsListItem.INFO_HEADER,
                    SettingsListItem.DONATE,
                    SettingsListItem.ONBOARDING,
                    SettingsListItem.FAQ,
                    SettingsListItem.BLOG,
                    SettingsListItem.REPORT_PROBLEM,
                    SettingsListItem.PRIVACY,
                    SettingsListItem.LICENSE,
                    SettingsListItem.SOURCE_CODE -> SettingsInfoSection(
                        row = row,
                        isLoading = state.loading,
                        headerModifier = headerModifier,
                        itemModifier = itemModifier,
                        processIntent = processIntent,
                    )

                    SettingsListItem.VERSION -> SettingsVersionFooter(
                        appVersion = state.appVersion,
                        processIntent = processIntent,
                    )
                }
            }
        }
    }
}
