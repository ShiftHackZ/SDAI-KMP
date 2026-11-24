package com.shifthackz.aisdv1.presentation.configuration.preview.upscaler

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.configuration.preview.ConfigurationPreviewTheme
import com.shifthackz.aisdv1.presentation.configuration.preview.upscaler.UpscalerSetupPreviewCase.Kind
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.ProviderSortBottomSheet
import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerFiltersBottomSheet
import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerSetupContent
import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerSourceItem

/**
 * Displays production upscaler configuration surfaces without constructing the application graph.
 *
 * @param scenario Wizard, download, row or discovery-sheet snapshot.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerSetupPreview(scenario: UpscalerSetupPreviewCase) {
    val state = scenario.state
    ConfigurationPreviewTheme {
        when (scenario.kind) {
            Kind.SCREEN -> UpscalerSetupContent(state = state, processIntent = {})
            Kind.FILTERS -> UpscalerFiltersBottomSheet(state, ServerSetupStrings(), onChange = {})
            Kind.SORT -> ProviderSortBottomSheet(
                state.discovery.sortOrder,
                ServerSetupStrings(),
                onSelect = {})

            Kind.PROVIDER -> Box(Modifier.padding(16.dp)) {
                UpscalerSourceItem(
                    source = state.source, platform = state.platform,
                    isSelected = scenario.isSelected, strings = ServerSetupStrings(), onClick = {},
                )
            }
        }
    }
}
