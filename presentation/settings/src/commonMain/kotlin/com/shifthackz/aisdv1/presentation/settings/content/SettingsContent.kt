package com.shifthackz.aisdv1.presentation.settings.content

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.settings.SettingsIntent
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.SettingsState
import com.shifthackz.aisdv1.presentation.settings.contentType
import com.shifthackz.aisdv1.presentation.settings.settingsListItems
import com.shifthackz.aisdv1.presentation.settings.platform.NoOpSettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.settings.section.SettingsCommunitySection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsInfoSection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsLookAndFeelSection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsServerSection
import com.shifthackz.aisdv1.presentation.settings.section.SettingsVersionFooter
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar

/**
 * Renders visible Settings rows in one lazy list. Provider, appearance, community, information
 * and version entries retain stable identities while offscreen rows avoid composition on theme edits.
 *
 * @param state MVI state that controls section visibility and item values.
 * @param modifier Layout applied to the scrollable Settings body.
 * @param platformActions platform capabilities needed by settings rows.
 * @param processIntent sends user actions back to the Settings MVI pipeline.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun ContentSettingsState(
    modifier: Modifier = Modifier,
    state: SettingsState,
    platformActions: SettingsPlatformActions = NoOpSettingsPlatformActions,
    processIntent: (SettingsIntent) -> Unit = {},
) {
    val isDark = if (state.useSystemDarkTheme) {
        isSystemInDarkTheme()
    } else {
        state.darkTheme
    }
    val listState = rememberLazyListState()
    val rows = settingsListItems(
        state,
        isDark,
        platformActions.supportsConnectionTimeout,
        platformActions.isNnapiAvailable,
    )
    LazyColumn(
        modifier = modifier.verticalScrollbar(listState).padding(horizontal = 16.dp),
        state = listState,
    ) {
        items(items = rows, key = { it.name }, contentType = { it.contentType() }) { row ->
            Box(Modifier.animateItem()) {
                val headerModifier = Modifier.padding(top = 28.dp, bottom = 8.dp)
                val itemModifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                val warningModifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 4.dp)
                when (row) {
                    SettingsListItem.SERVER_HEADER,
                    SettingsListItem.CONFIGURATION,
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
