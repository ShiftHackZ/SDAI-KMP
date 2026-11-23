package com.shifthackz.aisdv1.presentation.logger.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.logger.LoggerState

/**
 * Shows every log-viewer presentation and representative log shapes in both palettes.
 *
 * @param scenario Named snapshot supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Logger", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Logger", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LoggerStatesPreview(
    @PreviewParameter(LoggerPreviewParameterProvider::class) scenario: LoggerPreviewCase,
) {
    LoggerPreview(scenario.state)
}

/**
 * Shows toolbar actions and long log rows on narrow and landscape viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Logger", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Logger", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Logger", widthDp = 800, heightDp = 360)
@Composable
private fun LoggerCompactPreview() {
    LoggerPreview(LoggerState(loading = false, text = LoggerPreviewFixtures.longLog))
}

/**
 * Shows stack-trace indentation and toolbar spacing with enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Large text", group = "Logger", widthDp = 320, heightDp = 640, fontScale = 1.5f)
@Composable
private fun LoggerLargeTextPreview() {
    LoggerPreview(LoggerState(loading = false, text = LoggerPreviewFixtures.stackTrace))
}
