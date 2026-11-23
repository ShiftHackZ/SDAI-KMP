package com.shifthackz.aisdv1.presentation.statistics.networkusage

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.model.UsageState

/**
 * Supplies named traffic snapshots without observing or resetting persisted network counters.
 * Includes each selectable bucket, zero rows, a single segment and large byte totals.
 *
 * @author Dmitriy Moroz
 */
internal class NetworkUsagePreviewParameterProvider : PreviewParameterProvider<NetworkUsageState> {
    private val items = listOf(
        UsageItem(UsageCategory.TRAFFIC_MODELS, 12_800_000_000L),
        UsageItem(UsageCategory.TRAFFIC_CONFIGS, 240_000L),
        UsageItem(UsageCategory.TRAFFIC_INFERENCE, 680_000_000L),
        UsageItem(UsageCategory.TRAFFIC_CIVITAI, 96_000_000L),
    )
    private val cases = buildList {
        add("Initial loading" to NetworkUsageState())
        add("Resetting statistics" to NetworkUsageState(UsageState(loading = true, items = items)))
        add("Empty snapshot" to NetworkUsageState(UsageState()))
        add("Zero counters" to NetworkUsageState(UsageState(items = items.map { it.copy(bytes = 0L) })))
        add("Total" to NetworkUsageState(UsageState(items = items)))
        add("Mixed active and empty rows" to NetworkUsageState(UsageState(items = items.map {
            if (it.category == UsageCategory.TRAFFIC_CONFIGS || it.category == UsageCategory.TRAFFIC_CIVITAI) {
                it.copy(bytes = 0L)
            } else it
        })))
        items.forEach { item ->
            add(
                "Selected ${item.category}" to NetworkUsageState(
                    UsageState(items = items, selectedCategory = item.category),
                )
            )
        }
        add(
            "Single category · Large total" to NetworkUsageState(
                UsageState(
                    items = items.map {
                        it.copy(bytes = if (it.category == UsageCategory.TRAFFIC_INFERENCE) 1_250_000_000_000L else 0L)
                    },
                    selectedCategory = UsageCategory.TRAFFIC_INFERENCE,
                )
            )
        )
    }

    /**
     * Ordered snapshots used by the light and dark screen previews.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<NetworkUsageState> = cases.asSequence().map { it.second }

    /**
     * Labels each preview with its loading, selection or counter scenario.
     *
     * @param index Position in the ordered snapshot sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].first
}
