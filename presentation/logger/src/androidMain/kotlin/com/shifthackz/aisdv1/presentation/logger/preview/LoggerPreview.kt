package com.shifthackz.aisdv1.presentation.logger.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.logger.LoggerScreenContent
import com.shifthackz.aisdv1.presentation.logger.LoggerScreenStrings
import com.shifthackz.aisdv1.presentation.logger.LoggerState
import com.shifthackz.aisdv1.presentation.logger.toContentState

/**
 * Composes the real log viewer with localized labels and inert platform/navigation actions.
 * No ViewModel, Koin graph, log reader, clipboard or share sheet is started.
 *
 * @param state Fixed log snapshot and read status to display.
 * @author Dmitriy Moroz
 */
@Composable
internal fun LoggerPreview(state: LoggerState) {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        LoggerScreenContent(
            strings = LoggerScreenStrings(),
            state = state.toContentState(),
            processAction = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
