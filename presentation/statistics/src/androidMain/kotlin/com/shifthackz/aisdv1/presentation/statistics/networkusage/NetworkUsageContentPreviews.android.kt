package com.shifthackz.aisdv1.presentation.statistics.networkusage

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.statistics.preview.StatisticsPreviewTheme

/**
 * Shows every traffic snapshot, including loading, reset, empty and selected categories in both palettes.
 *
 * @param state Fixed traffic counters and selection supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Network usage", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Network usage", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NetworkUsageStatesPreview(
    @PreviewParameter(NetworkUsagePreviewParameterProvider::class) state: NetworkUsageState,
) {
    StatisticsPreviewTheme {
        NetworkUsageScreenContent(state = state)
    }
}

/**
 * Checks selected traffic totals and category labels on a narrow screen with enlarged text.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Large text",
    group = "Network usage",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun NetworkUsageCompactPreview() {
    StatisticsPreviewTheme {
        NetworkUsageScreenContent(state = NetworkUsagePreviewParameterProvider().values.last())
    }
}
