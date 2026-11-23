package com.shifthackz.aisdv1.presentation.backup.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.backup.BackupScreenContent
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

/**
 * Shows the actual backup renderer with fixed nonsecret state and independent page scroll positions.
 * No ViewModel, Koin graph, document picker, archive operation or navigation is started.
 *
 * @param mode Welcome, restore-only or normal two-tab chrome to display.
 * @param scenario Fixed selection, operation feedback and vertical position.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackupPreview(mode: BackupScreenMode, scenario: BackupPreviewCase) {
    val createScrollState = rememberScrollState(initial = scenario.scrollOffset)
    val restoreScrollState = rememberScrollState(initial = scenario.scrollOffset)
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        BackupScreenContent(
            mode = mode,
            state = scenario.state,
            processIntent = {},
            modifier = Modifier.fillMaxSize(),
            createScrollState = createScrollState,
            restoreScrollState = restoreScrollState,
        )
    }
}
