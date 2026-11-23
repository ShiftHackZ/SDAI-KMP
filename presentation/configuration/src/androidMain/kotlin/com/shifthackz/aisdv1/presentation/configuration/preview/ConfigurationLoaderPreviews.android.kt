package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderScreenContent
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderState
import com.shifthackz.aisdv1.presentation.configuration.loader.toContentState

/**
 * Shows localized startup progress for all configuration-loading phases in both palettes.
 *
 * @param state Fixed startup stage supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Configuration · Loader", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    group = "Configuration · Loader",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ConfigurationLoaderStatesPreview(
    @PreviewParameter(ConfigurationLoaderPreviewParameterProvider::class) state: ConfigurationLoaderState,
) {
    ConfigurationPreviewTheme { ConfigurationLoaderScreenContent(state.toContentState()) }
}

/**
 * Shows fetching progress on narrow and landscape viewports and with enlarged status text.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Configuration · Loader", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark",
    group = "Configuration · Loader",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Configuration · Loader", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Configuration · Loader",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun ConfigurationLoaderCompactPreview() {
    ConfigurationPreviewTheme {
        ConfigurationLoaderScreenContent(ConfigurationLoaderState(ConfigurationLoaderState.Status.Fetching).toContentState())
    }
}
