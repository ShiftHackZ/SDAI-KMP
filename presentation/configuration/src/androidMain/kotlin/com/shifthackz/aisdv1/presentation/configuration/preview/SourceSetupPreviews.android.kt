package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Shows provider discovery, selection and search/filter/sort results.
 *
 * @param scenario Named wizard state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Light",
    group = "Configuration · Provider discovery",
    widthDp = 360,
    heightDp = 800
)
@Preview(
    name = "Dark", group = "Configuration · Provider discovery", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SourceSetupStatesPreview(
    @PreviewParameter(SourceSetupPreviewParameterProvider::class) scenario: ServerSetupPreviewCase,
) {
    ServerSetupPreview(scenario)
}

/**
 * Shows the provider discovery layout on narrow, landscape and enlarged-text viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Configuration · Provider discovery",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark",
    group = "Configuration · Provider discovery",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Landscape",
    group = "Configuration · Provider discovery",
    widthDp = 800,
    heightDp = 360
)
@Preview(
    name = "Large text",
    group = "Configuration · Provider discovery",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun SourceSetupCompactPreview() {
    ServerSetupPreview(ServerSetupPreviewCase("Compact", ConfigurationPreviewFixtures.source))
}
