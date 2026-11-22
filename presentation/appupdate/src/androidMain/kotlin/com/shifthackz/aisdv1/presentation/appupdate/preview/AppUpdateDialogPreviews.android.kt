package com.shifthackz.aisdv1.presentation.appupdate.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.presentation.appupdate.AppUpdateDialog
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

/**
 * Renders the Full offer in the light application theme without launch effects.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Full — Light",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun FullUpdateLightPreview() {
    AppUpdatePreview(version = "1.3.0", isDarkTheme = false)
}

/**
 * Renders the Full offer in the dark application theme without launch effects.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Full — Dark",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun FullUpdateDarkPreview() {
    AppUpdatePreview(version = "1.3.0", isDarkTheme = true)
}

/**
 * Renders the Nightly offer in the light application theme without launch effects.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Nightly — Light",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun NightlyUpdateLightPreview() {
    AppUpdatePreview(version = "1.2.0-dev.20261122.a1b2c3d4", isDarkTheme = false)
}

/**
 * Renders the Nightly offer in the dark application theme without launch effects.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Nightly — Dark",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun NightlyUpdateDarkPreview() {
    AppUpdatePreview(version = "1.2.0-dev.20261122.a1b2c3d4", isDarkTheme = true)
}

/**
 * Renders the AppStore offer in the light application theme without launch effects.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "AppStore — Light",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun AppStoreUpdateLightPreview() {
    AppUpdatePreview(version = "1.3.0", isDarkTheme = false)
}

/**
 * Renders the AppStore offer in the dark application theme without launch effects.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "AppStore — Dark",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun AppStoreUpdateDarkPreview() {
    AppUpdatePreview(version = "1.3.0", isDarkTheme = true)
}

/**
 * Checks wrapping for a long nightly identifier on a compact screen with enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Nightly — compact, large text",
    group = "App updates",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f,
    showBackground = true
)
@Composable
private fun CompactNightlyUpdatePreview() {
    AppUpdatePreview(version = "1.2.0-dev.20261122.a1b2c3d4", isDarkTheme = false)
}

/**
 * Shows the Ukrainian catalog with the same dialog used by the application.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Full — Ukrainian",
    group = "App updates",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
private fun UkrainianUpdatePreview() {
    AppUpdatePreview(version = "1.3.0", isDarkTheme = false, languageCode = "uk")
}

/**
 * Supplies explicit theme and locale inputs without Koin, HTTP or native update services.
 *
 * @param version Published version to illustrate.
 * @param isDarkTheme Whether to use the application's dark palette.
 * @param languageCode Bundled catalog selected for the preview.
 * @author Dmitriy Moroz
 */
@Composable
private fun AppUpdatePreview(version: String, isDarkTheme: Boolean, languageCode: String = "en") {
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isDarkTheme,
            shouldUseSystemColorPalette = false,
            colorToken = ColorToken.MAUVE,
        ),
        applySystemBars = false,
    ) {
        AppUpdateDialog(
            version = version,
            onUpdate = {},
            onDismiss = {},
            languageCode = languageCode
        )
    }
}
