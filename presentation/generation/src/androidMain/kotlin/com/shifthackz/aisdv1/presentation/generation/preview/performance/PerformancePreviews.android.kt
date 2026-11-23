package com.shifthackz.aisdv1.presentation.generation.preview.performance

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Displays app/device resource histories and missing readings in both application palettes.
 *
 * @param scenario Named fixed-data snapshot supplied by the preview picker.
 * @author Dmitriy Moroz
 */
@Preview(name = "Performance · Light", group = "Performance", widthDp = 360, heightDp = 400)
@Preview(
    name = "Performance · Dark",
    group = "Performance",
    widthDp = 360,
    heightDp = 400,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PerformanceStatesPreview(
    @PreviewParameter(PerformancePreviewParameterProvider::class) scenario: PerformancePreviewCase,
) {
    PerformancePreview(scenario)
}

/**
 * Displays the same production content at compact, wide and enlarged-text sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Performance sizes", widthDp = 320, heightDp = 360)
@Preview(
    name = "Compact · Dark",
    group = "Performance sizes",
    widthDp = 320,
    heightDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Performance sizes", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Performance sizes",
    widthDp = 360,
    heightDp = 400,
    fontScale = 1.5f
)
@Composable
private fun PerformanceSizesPreview() {
    PerformancePreview(PerformancePreviewCase("Resources", GenerationPreviewFixtures.samples))
}
