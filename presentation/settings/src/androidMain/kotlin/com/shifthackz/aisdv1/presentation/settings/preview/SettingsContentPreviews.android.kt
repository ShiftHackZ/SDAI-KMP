package com.shifthackz.aisdv1.presentation.settings.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem

/**
 * Shows provider-specific controls, loading and preference sections in both application palettes.
 *
 * @param scenario Named Settings state and section supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Settings content", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Settings content", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsContentStatesPreview(
    @PreviewParameter(SettingsContentPreviewParameterProvider::class) scenario: SettingsPreviewCase,
) {
    SettingsPreview(scenario)
}

/**
 * Checks the server controls on a compact screen with enlarged text in both palettes.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Settings content",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Preview(
    name = "Compact · Dark", group = "Settings content", widthDp = 320, heightDp = 640,
    fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsCompactPreview() {
    SettingsPreview(SettingsPreviewCase("Compact", SettingsPreviewFixtures.ready))
}

/**
 * Checks appearance choices on a compact screen without relying on scrolling the preview manually.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Appearance · Large text",
    group = "Settings content",
    widthDp = 320,
    heightDp = 800,
    fontScale = 1.5f
)
@Composable
private fun SettingsAppearanceLargeTextPreview() {
    SettingsPreview(
        SettingsPreviewCase(
            name = "Appearance",
            state = SettingsPreviewFixtures.ready,
            firstVisibleRow = SettingsListItem.LOOK_HEADER,
        )
    )
}
