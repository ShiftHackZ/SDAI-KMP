package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Shows configuration feedback, consent and model-operation dialogs.
 *
 * @param scenario Named wizard state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Configuration · Setup dialogs", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Configuration · Setup dialogs", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SetupModalStatesPreview(
    @PreviewParameter(SetupModalPreviewParameterProvider::class) scenario: ServerSetupPreviewCase,
) {
    ServerSetupPreview(scenario)
}
