package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem

/**
 * Bottom sheet with provider list sort order options.
 *
 * @param state current selected sort order.
 * @param strings localized sort option labels.
 * @param processIntent sends sort changes back to the setup MVI pipeline.
 * @param modifier Layout modifiers for the sort-order choice list.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceSortBottomSheet(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProviderSortBottomSheet(
        selected = state.sourceSortOrder, strings = strings,
        onSelect = {
            processIntent(ServerSetupIntent.UpdateSourceSortOrder(it))
            processIntent(ServerSetupIntent.DismissDialog)
        },
        modifier = modifier,
    )
}

/**
 * Shared provider date-order choices for either configuration picker.
 *
 * @param selected Current ordering.
 * @param strings Localized title and ordering labels.
 * @param onSelect Publishes selection and dismisses the owning sheet.
 * @param modifier Layout of the complete sheet.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderSortBottomSheet(
    selected: ServerSetupState.SourceSortOrder,
    strings: ServerSetupStrings, onSelect: (ServerSetupState.SourceSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = strings.sourceSortTitle,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ServerSetupState.SourceSortOrder.entries.forEach { sortOrder ->
            SettingsItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                selected = selected == sortOrder,
                showChevron = false,
                startIcon = if (selected == sortOrder) Icons.Default.Check else null,
                text = sortOrder.mapToUi(strings).asUiText(),
                onClick = {
                    onSelect(sortOrder)
                },
            )
        }
    }
}
