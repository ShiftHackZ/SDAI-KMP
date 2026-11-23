package com.shifthackz.aisdv1.presentation.statistics.storageusage

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.model.UsageState

/**
 * Supplies named storage snapshots using the local-provider categories available on Android Full.
 * Includes each selection and delete confirmation, mixed zero rows and clear-all list formatting.
 * Fixtures never inspect files or invoke storage platform actions.
 *
 * @author Dmitriy Moroz
 */
internal class StorageUsagePreviewParameterProvider : PreviewParameterProvider<StorageUsageState> {
    private val items = listOf(
        UsageItem(UsageCategory.CACHE, 128_000_000L),
        UsageItem(UsageCategory.GALLERY, 860_000_000L),
        UsageItem(UsageCategory.MODELS_ONNX, 4_200_000_000L),
        UsageItem(UsageCategory.MODELS_MEDIAPIPE, 1_600_000_000L),
        UsageItem(UsageCategory.MODELS_SDXL, 6_800_000_000L),
        UsageItem(UsageCategory.MODELS_MNN, 2_400_000_000L),
    )
    private val cases = buildList {
        add("Initial loading" to StorageUsageState())
        add(
            "Clearing or refreshing" to StorageUsageState(
                UsageState(
                    loading = true,
                    items = items
                )
            )
        )
        add("Empty snapshot" to StorageUsageState(UsageState()))
        add("Empty categories" to StorageUsageState(UsageState(items = items.map { it.copy(bytes = 0L) })))
        add("Total" to StorageUsageState(UsageState(items = items)))
        add("Mixed active and empty rows" to StorageUsageState(UsageState(items = items.map {
            if (it.category == UsageCategory.MODELS_MEDIAPIPE || it.category == UsageCategory.MODELS_MNN) {
                it.copy(bytes = 0L)
            } else it
        })))
        items.forEach { item ->
            add(
                "Selected ${item.category}" to StorageUsageState(
                    usage = UsageState(items = items, selectedCategory = item.category),
                )
            )
        }
        add(
            "Single category · Large total" to StorageUsageState(
                UsageState(
                    items = items.map {
                        it.copy(bytes = if (it.category == UsageCategory.GALLERY) 1_250_000_000_000L else 0L)
                    },
                )
            )
        )
        items.forEach { item ->
            add(
                "Confirm delete ${item.category}" to StorageUsageState(
                    usage = UsageState(items = items, selectedCategory = item.category),
                    screenModal = StorageUsageModal.ClearCategory(item),
                )
            )
        }
        listOf(items, items.take(2), items.take(1)).forEach { nonEmptyItems ->
            add(
                "Confirm clear all · ${nonEmptyItems.size} categories" to StorageUsageState(
                    usage = UsageState(items = nonEmptyItems),
                    screenModal = StorageUsageModal.ClearAll(
                        items = nonEmptyItems,
                        totalBytes = nonEmptyItems.sumOf(UsageItem::bytes),
                    ),
                )
            )
        }
    }

    /**
     * Ordered dashboard and confirmation states used by both theme previews.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<StorageUsageState> = cases.asSequence().map { it.second }

    /**
     * Labels each preview with the storage operation or selected category it illustrates.
     *
     * @param index Position in the ordered snapshot sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].first
}
