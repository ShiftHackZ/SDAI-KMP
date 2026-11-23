package com.shifthackz.aisdv1.presentation.splash.preview

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState
import com.shifthackz.aisdv1.presentation.splash.SplashScreenContent

/**
 * Shows the startup indicator and app name across phone sizes, orientation, text scale and palettes.
 * Composes the existing content without starting the splash ViewModel or navigating to another route.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Splash", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Splash", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Compact · Light", group = "Splash", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Splash", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape · Light", group = "Splash", widthDp = 800, heightDp = 360)
@Preview(
    name = "Landscape · Dark", group = "Splash", widthDp = 800, heightDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Large text · Light",
    group = "Splash",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Preview(
    name = "Large text · Dark", group = "Splash", widthDp = 320, heightDp = 640,
    fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SplashScreenContentPreview() {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        SplashScreenContent()
    }
}
