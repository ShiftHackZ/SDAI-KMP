package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.configuration.modal.download.DownloadDialogContent
import com.shifthackz.aisdv1.presentation.configuration.modal.download.DownloadDialogState

/**
 * Shows the actual source-selection dialog without its model-loading ViewModel.
 *
 * @param scenario Named mirror-list and selection snapshot.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Configuration · Download sources", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    group = "Configuration · Download sources",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DownloadSourceStatesPreview(
    @PreviewParameter(DownloadSourcePreviewParameterProvider::class) scenario: Pair<String, DownloadDialogState>,
) {
    ConfigurationPreviewTheme { DownloadDialogContent(state = scenario.second, processIntent = {}) }
}

/**
 * Shows the source dialog on a narrow phone viewport in both palettes.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Configuration · Download sources",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark",
    group = "Configuration · Download sources",
    widthDp = 320,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DownloadSourceCompactPreview() {
    ConfigurationPreviewTheme {
        DownloadDialogContent(
            state = DownloadDialogState(listOf("https://models.example/model.zip" to true)),
            processIntent = {})
    }
}
