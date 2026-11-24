@file:OptIn(ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.components.model.readinessFor
import com.shifthackz.aisdv1.presentation.components.widget.input.FilterOptionChip
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.mappers.mapToUi

/**
 * Bottom sheet for provider type, readiness, and capability tag filters.
 *
 * Disabled chips represent impossible set intersections, so users can see
 * available providers without creating an empty result set by accident.
 *
 * @param state current filter state and available providers.
 * @param strings localized filter labels.
 * @param processIntent sends filter changes back to the setup MVI pipeline.
 * @param modifier Layout modifiers for the provider-filter sheet content.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceFiltersBottomSheet(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetPadding = 16.dp
    val horizontalItemSpacing = 8.dp
    val verticalItemSpacing = 0.dp
    val availableTags = state.allowedModes
        .flatMap(ServerSource::featureTags)
        .distinct()
        .sortedBy(FeatureTag::ordinal)
    val availableReadiness = state.allowedModes
        .map { source -> source.readinessFor(state.platform) }
        .distinct()
        .sortedBy(ServerSourceReadiness::ordinal)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .navigationBarsPadding()
            .padding(
                start = sheetPadding,
                top = sheetPadding,
                end = sheetPadding,
                bottom = sheetPadding,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(horizontalItemSpacing),
        ) {
            Text(
                text = strings.sourceOptionsTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing),
                verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
            ) {
                FilterOptionChip(
                    label = strings.sourceFilterAll,
                    isSelected = state.sourceTypeFilter == null,
                    onClick = {
                        processIntent(ServerSetupIntent.UpdateSourceTypeFilter(null))
                    },
                )
                ServerSourceType.entries.forEach { type ->
                    FilterOptionChip(
                        label = type.mapToUi(strings),
                        isSelected = state.sourceTypeFilter == type,
                        isEnabled = state.isSourceTypeEnabled(type),
                        onClick = {
                            processIntent(ServerSetupIntent.UpdateSourceTypeFilter(type))
                        },
                    )
                }
            }
            Text(
                text = strings.sourceFilterReadiness,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing),
                verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
            ) {
                availableReadiness.forEach { readiness ->
                    FilterOptionChip(
                        label = readiness.mapToUi(strings),
                        isSelected = readiness in state.sourceReadinessFilters,
                        isEnabled = state.isSourceReadinessEnabled(readiness),
                        onClick = {
                            processIntent(ServerSetupIntent.ToggleSourceReadinessFilter(readiness))
                        },
                    )
                }
            }
            Text(
                text = strings.sourceFilterTags,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalItemSpacing),
                verticalArrangement = Arrangement.spacedBy(verticalItemSpacing),
            ) {
                availableTags.forEach { tag ->
                    FilterOptionChip(
                        label = tag.mapToUi(),
                        isSelected = tag in state.sourceTagFilters,
                        isEnabled = state.isSourceTagEnabled(tag),
                        onClick = {
                            processIntent(ServerSetupIntent.ToggleSourceTagFilter(tag))
                        },
                    )
                }
            }
        }
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = horizontalItemSpacing),
            isEnabled = state.sourceTypeFilter != null ||
                    state.sourceReadinessFilters.isNotEmpty() ||
                    state.sourceTagFilters.isNotEmpty(),
            onClick = {
                processIntent(ServerSetupIntent.ResetSourceFilters)
            },
        ) {
            Text(text = strings.reset)
        }
    }
}
