package com.shifthackz.aisdv1.presentation.settings.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.settings.SettingsScreenContent
import com.shifthackz.aisdv1.presentation.settings.settingsListItems

/**
 * Renders the actual Settings scaffold, requested lazy-list section and modal with fixed preferences.
 * Background work is omitted and actions are inert, so neither the application graph nor services start.
 *
 * @param scenario Preferences, capabilities and initial visible row used by this preview.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsPreview(scenario: SettingsPreviewCase) {
    val isDark = isSystemInDarkTheme()
    val state = scenario.state.copy(darkTheme = isDark)
    val platformActions = remember(scenario.isNnapiAvailable, scenario.supportsConnectionTimeout) {
        SettingsPreviewPlatformActions(
            scenario.isNnapiAvailable,
            scenario.supportsConnectionTimeout
        )
    }
    val rows = settingsListItems(
        state,
        isDark,
        scenario.supportsConnectionTimeout,
        scenario.isNnapiAvailable
    )
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = rows.indexOf(scenario.firstVisibleRow).coerceAtLeast(0),
    )
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isDark,
            shouldUseSystemColorPalette = false,
            colorToken = state.colorToken,
            darkThemeToken = state.darkThemeToken,
        ),
        applySystemBars = false,
    ) {
        SettingsScreenContent(
            modifier = Modifier.fillMaxSize(),
            state = state,
            platformActions = platformActions,
            listState = listState,
            backgroundWorkContent = {},
        )
    }
}
