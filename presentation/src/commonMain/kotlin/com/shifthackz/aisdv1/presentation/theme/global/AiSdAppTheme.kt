package com.shifthackz.aisdv1.presentation.theme.global

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.di.initKoin

/**
 * Observes persisted theme selection and provides it to the application content.
 *
 * @param content Application UI rendered inside the selected Material theme.
 * @author Dmitriy Moroz
 */
@Composable
fun AiSdAppTheme(
    content: @Composable () -> Unit,
) {
    val koin = remember { initKoin() }
    val viewModel = remember(koin) {
        koin.get<AiSdAppThemeViewModel>()
    }
    MviComponent(viewModel = viewModel) { state ->
        AiSdAppTheme(state = state, content = content)
    }
}

/**
 * Applies the selected Material theme, retaining palette and typography objects while their
 * inputs remain unchanged. System appearance still participates in dark-mode selection.
 *
 * @param state Persisted theme preferences, independent of other application settings.
 * @param applySystemBars Whether platform system bars follow the selected appearance.
 * @param content Application UI rendered inside the selected Material theme.
 * @author Dmitriy Moroz
 */
@Composable
fun AiSdAppTheme(
    state: AiSdAppThemeState,
    applySystemBars: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isDark = if (state.shouldUseSystemDarkTheme) {
        isSystemInDarkTheme()
    } else {
        state.isDarkTheme
    }
    val dynamicColorScheme = dynamicColorSchemeOrNull(
        useDynamicColors = state.shouldUseSystemColorPalette,
        isDark = isDark,
    )
    val staticColorScheme = remember(state.colorToken, state.darkThemeToken, isDark) {
        catppuccinColorScheme(
            token = state.colorToken,
            darkThemeToken = state.darkThemeToken,
            isDark = isDark,
        )
    }
    val typography = remember(isDark, state.darkThemeToken) {
        catppuccinTypography(isDark, state.darkThemeToken)
    }
    MaterialTheme(
        colorScheme = dynamicColorScheme ?: staticColorScheme,
        typography = typography,
    ) {
        if (applySystemBars) {
            ApplySystemBarTheme(
                colorScheme = MaterialTheme.colorScheme,
                isDark = isDark,
            )
        }
        content()
    }
}
