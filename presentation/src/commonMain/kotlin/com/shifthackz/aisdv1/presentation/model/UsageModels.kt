package com.shifthackz.aisdv1.presentation.model

import androidx.compose.runtime.Immutable

/**
 * Identifies the concrete standalone usage screen rendered by the shared usage UI.
 *
 * @author Dmitriy Moroz
 */
enum class UsageScreenKind {
    /**
     * App storage screen: cache, generated gallery, and downloaded local AI model files.
     *
     * @author Dmitriy Moroz
     */
    STORAGE,

    /**
     * Network traffic screen: AI model downloads, configuration sync, and inference traffic.
     *
     * @author Dmitriy Moroz
     */
    NETWORK,
}

/**
 * Categories shared by storage and network usage dashboards.
 *
 * Storage model categories are filtered by allowed build modes before they reach UI state, so
 * platform-specific providers such as Core ML are only displayed on builds where they are valid.
 *
 * @author Dmitriy Moroz
 */
enum class UsageCategory {
    CACHE,
    GALLERY,
    MODELS_ONNX,
    MODELS_MEDIAPIPE,
    MODELS_SDXL,
    MODELS_CORE_ML,
    MODELS_MNN,
    MODELS_BONSAI,
    TRAFFIC_MODELS,
    TRAFFIC_CONFIGS,
    TRAFFIC_INFERENCE,
}

/**
 * Shared immutable state consumed by the donut chart and category list.
 *
 * @param loading True while the screen should show shimmer placeholders instead of stale data.
 * @param items Ordered categories visible on the current usage screen.
 * @param selectedCategory Category currently highlighted in the donut, or null to show total.
 * @property loading True while the screen should show shimmer placeholders instead of stale data.
 * @property items Ordered categories visible on the current usage screen.
 * @property selectedCategory Category currently highlighted in the donut, or null to show total.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class UsageState(
    val loading: Boolean = false,
    val items: List<UsageItem> = emptyList(),
    val selectedCategory: UsageCategory? = null,
) {
    /**
     * Sum of all visible positive category byte counts.
     *
     * @author Dmitriy Moroz
     */
    val totalBytes: Long
        get() = items.sumOf { it.bytes.coerceAtLeast(0L) }

    /**
     * Selected item if it still exists and remains non-empty.
     *
     * @author Dmitriy Moroz
     */
    val selectedItem: UsageItem?
        get() = items.firstOrNull { it.category == selectedCategory }
}

/**
 * One visible usage category row and donut segment.
 *
 * @param category Semantic bucket represented by the row and donut segment.
 * @param bytes Byte count currently attributed to this category.
 * @param modelIds Local model identifiers represented by this row. Only model storage rows use it.
 * @property modelIds Local model identifiers represented by this row. Only model storage rows use it.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class UsageItem(
    val category: UsageCategory,
    val bytes: Long,
    val modelIds: List<String> = emptyList(),
) {
    /**
     * Whether this category should be selectable and drawn as an active chart segment.
     *
     * @author Dmitriy Moroz
     */
    val enabled: Boolean
        get() = bytes > 0L
}

/**
 * Keeps selection only while the category is still present and non-empty.
 *
 * @param preferred Previously selected category, or null when the donut should show total.
 * @receiver Ordered usage rows currently rendered by a storage or network screen.
 *
 * @author Dmitriy Moroz
 */
fun List<UsageItem>.resolveSelectedCategory(
    preferred: UsageCategory?,
): UsageCategory? =
    preferred?.takeIf { category ->
        any { item -> item.category == category && item.enabled }
    }

/**
 * Returns true for categories that can appear on the storage usage screen.
 *
 * @author Dmitriy Moroz
 */
fun UsageCategory.isStorageCategory(): Boolean = when (this) {
    UsageCategory.CACHE,
    UsageCategory.GALLERY,
    UsageCategory.MODELS_ONNX,
    UsageCategory.MODELS_MEDIAPIPE,
    UsageCategory.MODELS_SDXL,
    UsageCategory.MODELS_CORE_ML,
    UsageCategory.MODELS_MNN,
    UsageCategory.MODELS_BONSAI,
        -> true

    UsageCategory.TRAFFIC_MODELS,
    UsageCategory.TRAFFIC_CONFIGS,
    UsageCategory.TRAFFIC_INFERENCE,
        -> false
}

/**
 * Returns true for storage categories backed by downloaded local model files.
 *
 * @author Dmitriy Moroz
 */
fun UsageCategory.isModelCategory(): Boolean = when (this) {
    UsageCategory.MODELS_ONNX,
    UsageCategory.MODELS_MEDIAPIPE,
    UsageCategory.MODELS_SDXL,
    UsageCategory.MODELS_CORE_ML,
    UsageCategory.MODELS_MNN,
    UsageCategory.MODELS_BONSAI,
        -> true

    UsageCategory.CACHE,
    UsageCategory.GALLERY,
    UsageCategory.TRAFFIC_MODELS,
    UsageCategory.TRAFFIC_CONFIGS,
    UsageCategory.TRAFFIC_INFERENCE,
        -> false
}
