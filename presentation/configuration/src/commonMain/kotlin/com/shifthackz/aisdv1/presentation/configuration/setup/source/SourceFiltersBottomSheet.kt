package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.components.model.readinessFor
import com.shifthackz.aisdv1.presentation.components.widget.input.FilterOptionChip
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ProviderFiltersSheet
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ProviderFilterSection
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.mappers.mapToUi

/**
 * Renders generation hosting, readiness and capability filters in the shared discovery sheet.
 * Impossible intersections stay disabled while selected chips remain removable.
 *
 * @param state Current filters and platform-allowed generation providers.
 * @param strings Localized filter labels.
 * @param processIntent Sends filter changes to the generation configuration draft.
 * @param modifier Layout of the complete sheet.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceFiltersBottomSheet(
    state: ServerSetupState, strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit, modifier: Modifier = Modifier
) {
    ProviderFiltersSheet(
        hasActiveFilters = state.sourceTypeFilter != null ||
                state.sourceReadinessFilters.isNotEmpty() || state.sourceTagFilters.isNotEmpty(),
        strings = strings, onReset = { processIntent(ServerSetupIntent.ResetSourceFilters) },
        modifier = modifier,
    ) {
        ProviderFilterSection(strings.sourceOptionsTitle) {
            FilterOptionChip(
                strings.sourceFilterAll, isSelected = state.sourceTypeFilter == null,
                onClick = { processIntent(ServerSetupIntent.UpdateSourceTypeFilter(null)) })
            ServerSourceType.entries.forEach { type ->
                FilterOptionChip(
                    type.mapToUi(strings), isSelected = state.sourceTypeFilter == type,
                    isEnabled = state.isSourceTypeEnabled(type),
                    onClick = { processIntent(ServerSetupIntent.UpdateSourceTypeFilter(type)) })
            }
        }
        ProviderFilterSection(strings.sourceFilterReadiness) {
            state.allowedModes.map { it.readinessFor(state.platform) }.distinct()
                .sortedBy(ServerSourceReadiness::ordinal).forEach { readiness ->
                    FilterOptionChip(
                        readiness.mapToUi(strings),
                        isSelected = readiness in state.sourceReadinessFilters,
                        isEnabled = state.isSourceReadinessEnabled(readiness),
                        onClick = {
                            processIntent(
                                ServerSetupIntent.ToggleSourceReadinessFilter(
                                    readiness
                                )
                            )
                        })
                }
        }
        ProviderFilterSection(strings.sourceFilterTags) {
            state.allowedModes.flatMap { it.featureTags }.distinct().sortedBy(FeatureTag::ordinal)
                .forEach { tag ->
                    FilterOptionChip(
                        tag.mapToUi(), isSelected = tag in state.sourceTagFilters,
                        isEnabled = state.isSourceTagEnabled(tag),
                        onClick = { processIntent(ServerSetupIntent.ToggleSourceTagFilter(tag)) })
                }
        }
    }
}
