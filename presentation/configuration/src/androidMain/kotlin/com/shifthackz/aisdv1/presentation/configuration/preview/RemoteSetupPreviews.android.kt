package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Shows each remote form, validation and authorization phase.
 *
 * @param scenario Named wizard state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Configuration · Remote setup", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Configuration · Remote setup", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RemoteSetupStatesPreview(
    @PreviewParameter(RemoteSetupPreviewParameterProvider::class) scenario: ServerSetupPreviewCase,
) {
    ServerSetupPreview(scenario)
}

/**
 * Shows the remote setup layout on narrow, landscape and enlarged-text viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Configuration · Remote setup",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark", group = "Configuration · Remote setup", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Configuration · Remote setup", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Configuration · Remote setup",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun RemoteSetupCompactPreview() {
    ServerSetupPreview(
        ServerSetupPreviewCase(
            "Compact",
            ConfigurationPreviewFixtures.configure(ServerSource.AUTOMATIC1111)
        )
    )
}
