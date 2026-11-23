package com.shifthackz.aisdv1.presentation.benchmark.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

/**
 * Shows loading, running, results, errors and each lower benchmark section in both palettes.
 *
 * @param scenario Named screen/section state supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Benchmark", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Benchmark", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BenchmarkStatesPreview(
    @PreviewParameter(BenchmarkPreviewParameterProvider::class) scenario: BenchmarkPreviewCase,
) {
    BenchmarkPreview(scenario)
}

/**
 * Shows the score and pinned run action on narrow and landscape viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Benchmark", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Benchmark", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Benchmark", widthDp = 800, heightDp = 360)
@Composable
private fun BenchmarkCompactPreview() {
    BenchmarkPreview(BenchmarkPreviewCase("Score", BenchmarkPreviewFixtures.ready))
}

/**
 * Shows recommendation labels and values with enlarged text while retaining the run action.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Recommendations · Large text",
    group = "Benchmark",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun BenchmarkLargeTextPreview() {
    BenchmarkPreview(
        BenchmarkPreviewCase(
            "Recommendations",
            BenchmarkPreviewFixtures.ready,
            sectionIndex = 3
        )
    )
}
