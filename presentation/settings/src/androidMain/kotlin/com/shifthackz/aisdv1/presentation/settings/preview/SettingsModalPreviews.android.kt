package com.shifthackz.aisdv1.presentation.settings.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Shows each real Settings dialog and bottom sheet above the loaded screen in both palettes.
 *
 * @param scenario Settings state containing the dialog or sheet to display.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Settings dialogs and sheets", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Settings dialogs and sheets", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsModalStatesPreview(
    @PreviewParameter(SettingsModalPreviewParameterProvider::class) scenario: SettingsPreviewCase,
) {
    SettingsPreview(scenario)
}
