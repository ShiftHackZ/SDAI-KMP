package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Shows local catalogs, download controls and custom-model forms.
 *
 * @param scenario Named wizard state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Configuration · Local setup", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Configuration · Local setup", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LocalSetupStatesPreview(
    @PreviewParameter(LocalSetupPreviewParameterProvider::class) scenario: ServerSetupPreviewCase,
) {
    ServerSetupPreview(scenario)
}

/**
 * Shows the local setup layout on narrow, landscape and enlarged-text viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Configuration · Local setup",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark", group = "Configuration · Local setup", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Configuration · Local setup", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Configuration · Local setup",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun LocalSetupCompactPreview() {
    ServerSetupPreview(
        ServerSetupPreviewCase(
            "Compact",
            ConfigurationPreviewFixtures.configure(ServerSource.LOCAL_MNN)
        )
    )
}
