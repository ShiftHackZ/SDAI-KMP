package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag
import com.shifthackz.aisdv1.presentation.components.widget.input.FilterOptionChip
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ProviderFiltersSheet
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ProviderFilterSection
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.mapToUi

/**
 * Filters independent upscalers with the generation picker's hosting, maturity and capability semantics.
 *
 * @param state Platform-allowed providers and retained discovery inputs.
 * @param strings Localized shared filter captions.
 * @param onChange Updates the discovery draft without committing provider selection.
 * @param modifier Layout of the filter sheet.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerFiltersBottomSheet(
    state: UpscalerSetupState, strings: ServerSetupStrings,
    onChange: (UpscalerDiscoveryState) -> Unit, modifier: Modifier = Modifier
) {
    val filters = state.discovery
    val hasFilters =
        filters.type != null || filters.readiness.isNotEmpty() || filters.tags.isNotEmpty()
    ProviderFiltersSheet(hasFilters, strings, onReset = {
        onChange(filters.copy(type = null, readiness = emptySet(), tags = emptySet()))
    }, modifier = modifier) {
        ProviderFilterSection(strings.sourceOptionsTitle) {
            FilterOptionChip(
                strings.sourceFilterAll, isSelected = filters.type == null,
                onClick = { onChange(filters.copy(type = null)) })
            ServerSourceType.entries.forEach { type ->
                FilterOptionChip(
                    type.mapToUi(strings), isSelected = filters.type == type,
                    isEnabled = filters.type == type || state.sources.any {
                        state.matches(it, type, filters.readiness, filters.tags)
                    }, onClick = { onChange(filters.copy(type = type)) })
            }
        }
        ProviderFilterSection(strings.sourceFilterReadiness) {
            state.sources.map { it.readiness[state.platform] }.distinct()
                .sortedBy(ServerSourceReadiness::ordinal).forEach { readiness ->
                    val isSelected = readiness in filters.readiness
                    FilterOptionChip(
                        readiness.mapToUi(strings), isSelected = isSelected,
                        isEnabled = isSelected || state.sources.any {
                            state.matches(
                                it,
                                filters.type,
                                filters.readiness + readiness,
                                filters.tags
                            )
                        }, onClick = {
                            onChange(
                                filters.copy(
                                    readiness =
                                        if (isSelected) filters.readiness - readiness else filters.readiness + readiness
                                )
                            )
                        })
                }
        }
        ProviderFilterSection(strings.sourceFilterTags) {
            state.sources.flatMap { it.featureTags }.distinct()
                .sortedBy(UpscalerFeatureTag::ordinal).forEach { tag ->
                    val isSelected = tag in filters.tags
                    FilterOptionChip(
                        tag.mapToUi(),
                        isSelected = isSelected,
                        isEnabled = isSelected || state.sources.any {
                            state.matches(it, filters.type, filters.readiness, filters.tags + tag)
                        },
                        onClick = { onChange(filters.copy(tags = if (isSelected) filters.tags - tag else filters.tags + tag)) })
                }
        }
    }
}
