package com.shifthackz.aisdv1.presentation.history.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.history.HistoryState

/**
 * Shows loading, failures, empty history and representative metadata/image rows in both palettes.
 *
 * @param scenario Named history snapshot supplied by the parameter provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "History", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "History", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HistoryStatesPreview(
    @PreviewParameter(HistoryPreviewParameterProvider::class) scenario: HistoryPreviewCase,
) {
    HistoryPreview(scenario)
}

/**
 * Shows mixed thumbnail sizes and unavailable images in narrow and landscape lists.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "History", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "History", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "History", widthDp = 800, heightDp = 360)
@Composable
private fun HistoryCompactPreview() {
    HistoryPreview(
        HistoryPreviewCase(
            name = "Mixed history list",
            state = HistoryState(loading = false, items = HistoryPreviewFixtures.mixedItems),
            unavailableImageIds = setOf(2, 5),
        )
    )
}

/**
 * Shows prompt truncation and single-line generation metadata with enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Long prompt · Large text",
    group = "History",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun HistoryLargeTextPreview() {
    HistoryPreview(
        HistoryPreviewCase(
            name = "Long prompt",
            state = HistoryState(
                loading = false,
                items = listOf(HistoryPreviewFixtures.longPrompt)
            ),
        )
    )
}
