package com.shifthackz.aisdv1.presentation.debug.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.debug.DebugMenuScreenContent
import com.shifthackz.aisdv1.presentation.debug.debugMenuScreenStrings
import com.shifthackz.aisdv1.presentation.debug.toContentState

/**
 * Applies the application palette without system-bar effects or dynamic system colors.
 *
 * @param content Menu or scheduler choices rendered without the application graph.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DebugMenuPreviewTheme(content: @Composable () -> Unit) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

/**
 * Renders localized developer controls without resolving a ViewModel or executing debug operations.
 *
 * @param scenario Fixed capabilities, preferences and initial list position.
 * @author Dmitriy Moroz
 */
@Composable
internal fun DebugMenuPreview(scenario: DebugMenuPreviewCase) {
    DebugMenuPreviewTheme {
        DebugMenuScreenContent(
            strings = debugMenuScreenStrings(),
            state = scenario.state.toContentState(),
            processIntent = {},
            modifier = Modifier.fillMaxSize(),
            scrollState = rememberScrollState(scenario.scrollOffset),
        )
    }
}
