package com.shifthackz.aisdv1.presentation.report.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Shows loading, editing, every reason, completion and both error presentations in both palettes.
 *
 * @param scenario Named report state supplied by the preview parameter provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Report", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Report", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ReportStatesPreview(
    @PreviewParameter(ReportPreviewParameterProvider::class) scenario: ReportPreviewCase,
) {
    ReportPreview(scenario.state)
}

/**
 * Checks report reason wrapping and thumbnail geometry on narrow and landscape viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Report", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Report", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Report", widthDp = 800, heightDp = 360)
@Composable
private fun ReportCompactPreview() {
    ReportPreview(ReportPreviewFixtures.ready.copy(text = ReportPreviewFixtures.Description))
}

/**
 * Checks the description editor and persistent submit action with a long draft and enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Long draft · Large text",
    group = "Report",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun ReportLargeTextPreview() {
    ReportPreview(ReportPreviewFixtures.ready.copy(text = ReportPreviewFixtures.longDescription))
}
