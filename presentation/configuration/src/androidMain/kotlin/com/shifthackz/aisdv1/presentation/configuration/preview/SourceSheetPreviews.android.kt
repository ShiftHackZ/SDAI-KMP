package com.shifthackz.aisdv1.presentation.configuration.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceFiltersBottomSheet
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceSortBottomSheet

/**
 * Shows the real filter/sort sheet bodies at the bottom of the preview without modal animation.
 *
 * @param scenario Named sheet type and choices supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Configuration · Discovery sheets", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    group = "Configuration · Discovery sheets",
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SourceSheetStatesPreview(
    @PreviewParameter(SourceSheetPreviewParameterProvider::class) scenario: ServerSetupPreviewCase,
) {
    ConfigurationPreviewTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if (scenario.state.modal == ServerSetupState.Modal.SourceFilters) {
                SourceFiltersBottomSheet(
                    state = scenario.state,
                    strings = ServerSetupStrings(),
                    processIntent = {}
                )
            } else {
                SourceSortBottomSheet(
                    state = scenario.state,
                    strings = ServerSetupStrings(),
                    processIntent = {}
                )
            }
        }
    }
}
