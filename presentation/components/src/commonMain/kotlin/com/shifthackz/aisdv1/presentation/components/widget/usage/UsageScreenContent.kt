package com.shifthackz.aisdv1.presentation.components.widget.usage

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.platformToolbarTitleStyle
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.extensions.shimmer
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.model.UsageScreenKind
import com.shifthackz.aisdv1.presentation.components.model.UsageState
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar
import kotlin.math.floor

/**
 * Shared standalone layout for storage and network usage screens.
 *
 * The content owns the top app bar, shimmer loading state, interactive donut chart, category list,
 * and pinned primary CTA. It deliberately does not depend on app bottom navigation so callers can
 * mount it as a full-screen route like benchmark and configuration flows.
 *
 * @param modifier Compose modifier applied to the outer scaffold.
 * @param screen Storage or network flavor that controls copy, CTA, placeholders, and row actions.
 * @param usage Current dashboard data, including shimmer loading state and selected category.
 * @param onBack Called when the standalone screen back button is tapped.
 * @param onSelectCategory Receives a selected row or segment category; null restores the total on a background tap.
 * @param onClearCategory Called from storage rows when the user requests category deletion.
 * @param onPrimaryAction Called by the pinned CTA to clear storage or reset traffic statistics.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageScreenContent(
    modifier: Modifier = Modifier,
    screen: UsageScreenKind,
    usage: UsageState,
    onBack: () -> Unit = {},
    onSelectCategory: (UsageCategory?) -> Unit = {},
    onClearCategory: (UsageCategory) -> Unit = {},
    onPrimaryAction: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    PlatformScaffold(
        canUnderlapToolbar = true,
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            PlatformTopAppBar(
                navigationIcon = {
                    ToolbarBackButton(onClick = onBack, icon = Icons.AutoMirrored.Filled.ArrowBack)
                },
                title = {
                    Text(
                        text = Localization.string(screen.titleKey()),
                        style = platformToolbarTitleStyle(MaterialTheme.typography.headlineMedium),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        },
        bottomBar = {
            PrimaryButton(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .height(68.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp, top = 8.dp),
                isEnabled = !usage.loading && (usage.totalBytes > 0L || screen == UsageScreenKind.NETWORK),
                onClick = onPrimaryAction,
            ) {
                Text(
                    text = when (screen) {
                        UsageScreenKind.STORAGE -> Localization.string(
                            "settings_usage_clear_all_storage",
                            usage.totalBytes.formatBytes(),
                        )

                        UsageScreenKind.NETWORK -> Localization.string("settings_usage_reset_network")
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    ) { paddingValues ->
        ToolbarScrollObserver(scrollState.canScrollBackward)
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pointerInput(onSelectCategory) {
                    detectTapGestures { onSelectCategory(null) }
                }
                .verticalScrollbar(scrollState, topInset = LocalToolbarContentInset.current)
                .verticalScroll(scrollState)
                .padding(top = LocalToolbarContentInset.current)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (usage.loading) {
                UsageLoadingContent(screen = screen)
            } else {
                UsageDonutChart(
                    usage = usage,
                    onSelect = onSelectCategory,
                )
                Spacer(modifier = Modifier.height(22.dp))
                Text(
                    text = Localization.string(screen.titleKey()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = usage.subtitle(screen),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(22.dp))
                UsageCategoryList(
                    usage = usage,
                    showClearActions = screen == UsageScreenKind.STORAGE,
                    onSelectCategory = onSelectCategory,
                    onClearCategory = onClearCategory,
                )
                Text(
                    modifier = Modifier.padding(top = 14.dp, bottom = 24.dp),
                    text = Localization.string(screen.noteKey()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun UsageLoadingContent(
    screen: UsageScreenKind,
) {
    UsageDonutChartPlaceholder()
    Spacer(modifier = Modifier.height(22.dp))
    Text(
        text = Localization.string(screen.titleKey()),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
    ShimmerLine(
        modifier = Modifier
            .padding(top = 12.dp)
            .width(260.dp)
            .height(16.dp),
    )
    ShimmerLine(
        modifier = Modifier
            .padding(top = 8.dp)
            .width(190.dp)
            .height(16.dp),
    )
    Spacer(modifier = Modifier.height(22.dp))
    UsageCategoryListPlaceholder(rowCount = screen.placeholderRowsCount())
    ShimmerLine(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 24.dp)
            .fillMaxWidth(0.82f)
            .height(14.dp),
    )
}

@Composable
private fun UsageDonutChartPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(246.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .shimmer(),
        )
        Box(
            modifier = Modifier
                .size(128.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShimmerLine(
                    modifier = Modifier
                        .width(78.dp)
                        .height(20.dp),
                )
                ShimmerLine(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(54.dp)
                        .height(12.dp),
                )
            }
        }
    }
}

@Composable
private fun UsageCategoryListPlaceholder(
    rowCount: Int,
    modifier: Modifier = Modifier,
) {
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.78f),
    ) {
        Column {
            repeat(rowCount) { index ->
                UsageCategoryPlaceholderRow(showDivider = index < rowCount - 1)
            }
        }
    }
}

@Composable
private fun UsageCategoryPlaceholderRow(
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShimmerLine(
                modifier = Modifier
                    .size(34.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                ShimmerLine(
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(18.dp),
                )
                ShimmerLine(
                    modifier = Modifier
                        .fillMaxWidth(0.38f)
                        .height(12.dp),
                )
            }
            ShimmerLine(
                modifier = Modifier
                    .width(74.dp)
                    .height(18.dp),
            )
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 60.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
            )
        }
    }
}

@Composable
private fun UsageCategoryList(
    usage: UsageState,
    showClearActions: Boolean,
    onSelectCategory: (UsageCategory) -> Unit,
    onClearCategory: (UsageCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val percentLabels = remember(usage.items, usage.totalBytes) {
        usage.items.percentLabels(usage.totalBytes)
    }
    PlatformSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.78f),
    ) {
        Column {
            usage.items.forEachIndexed { index, item ->
                UsageCategoryRow(
                    item = item,
                    selected = item.category == usage.selectedCategory,
                    percentLabel = percentLabels[item.category].orEmpty(),
                    showClearAction = showClearActions && item.enabled,
                    showDivider = index < usage.items.lastIndex,
                    onSelectCategory = onSelectCategory,
                    onClearCategory = onClearCategory,
                )
            }
        }
    }
}

@Composable
private fun UsageCategoryRow(
    item: UsageItem,
    selected: Boolean,
    percentLabel: String,
    showClearAction: Boolean,
    showDivider: Boolean,
    onSelectCategory: (UsageCategory) -> Unit,
    onClearCategory: (UsageCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = item.enabled) {
                    onSelectCategory(item.category)
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        color = item.category.color(),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = CircleShape,
                            ),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.category.title(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.enabled) {
                    Text(
                        text = percentLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                    )
                }
            }
            Text(
                modifier = Modifier.widthIn(min = 68.dp),
                text = if (item.enabled) {
                    item.bytes.formatBytes()
                } else {
                    Localization.string("settings_usage_empty")
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
            )
            if (showClearAction) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = {
                        onClearCategory(item.category)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = Localization.string(
                            "settings_usage_clear_category",
                            item.category.title(),
                        ),
                    )
                }
            }
        }
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 60.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f)),
            )
        }
    }
}

@Composable
private fun ShimmerLine(
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .shimmer(),
    )
}

private fun UsageScreenKind.placeholderRowsCount(): Int = when (this) {
    UsageScreenKind.STORAGE -> 6
    UsageScreenKind.NETWORK -> 3
}

/**
 * Formats category percentages so visible rows always add up to 100%.
 *
 * Labels are calculated in tenths to keep tiny non-empty categories visible as at least 0.1% without
 * losing the leftover percent to rounding.
 *
 * @param totalBytes Sum of enabled category bytes used as the raw percentage denominator.
 * @receiver Ordered usage rows rendered by the current dashboard.
 *
 * @author Dmitriy Moroz
 */
private fun List<UsageItem>.percentLabels(totalBytes: Long): Map<UsageCategory, String> {
    val enabledItems = filter(UsageItem::enabled)
    if (totalBytes <= 0L || enabledItems.isEmpty()) return emptyMap()
    if (enabledItems.size == 1) {
        return mapOf(enabledItems.first().category to "100%")
    }

    val rawUnits = enabledItems.map { item ->
        item.bytes.toDouble() / totalBytes.toDouble() * PERCENT_TENTHS_TOTAL
    }
    val units = rawUnits
        .map { raw -> floor(raw).toInt().coerceAtLeast(MIN_PERCENT_TENTH) }
        .toMutableList()

    var delta = PERCENT_TENTHS_TOTAL - units.sum()
    if (delta > 0) {
        val order = rawUnits.indices
            .sortedByDescending { index -> rawUnits[index] - floor(rawUnits[index]) }
        var cursor = 0
        while (delta > 0 && order.isNotEmpty()) {
            units[order[cursor % order.size]] += 1
            cursor++
            delta--
        }
    } else if (delta < 0) {
        val order = units.indices.sortedByDescending { index -> units[index] - MIN_PERCENT_TENTH }
        var cursor = 0
        while (delta < 0 && order.isNotEmpty()) {
            val index = order[cursor % order.size]
            if (units[index] > MIN_PERCENT_TENTH) {
                units[index] -= 1
                delta++
            }
            cursor++
        }
    }

    return enabledItems
        .mapIndexed { index, item -> item.category to units[index].formatPercentUnits() }
        .toMap()
}

private fun Int.formatPercentUnits(): String {
    val whole = this / 10
    val decimal = this % 10
    return if (decimal == 0) {
        "$whole%"
    } else {
        "$whole.$decimal%"
    }
}

private fun UsageScreenKind.titleKey(): String = when (this) {
    UsageScreenKind.STORAGE -> "settings_usage_storage_title"
    UsageScreenKind.NETWORK -> "settings_usage_network_title"
}

private fun UsageScreenKind.subtitleKey(): String = when (this) {
    UsageScreenKind.STORAGE -> "settings_usage_storage_subtitle"
    UsageScreenKind.NETWORK -> "settings_usage_network_subtitle"
}

private fun UsageState.subtitle(screen: UsageScreenKind): String = when (screen) {
    UsageScreenKind.STORAGE -> storageSubtitle()
    UsageScreenKind.NETWORK -> Localization.string(
        screen.subtitleKey(),
        totalBytes.formatBytes(),
    )
}

private fun UsageState.storageSubtitle(): String {
    val names = items
        .filter(UsageItem::enabled)
        .map { item -> item.category.summaryTitle() }
    if (totalBytes <= 0L || names.isEmpty()) {
        return Localization.string("settings_usage_storage_subtitle_empty")
    }
    return Localization.string(
        "settings_usage_storage_subtitle",
        totalBytes.formatBytes(),
        names.joinLocalized(),
    )
}

private fun List<String>.joinLocalized(): String = when (size) {
    0 -> ""
    1 -> first()
    2 -> first() +
            Localization.string("settings_usage_list_last_separator") +
            last()

    else -> dropLast(1).joinToString(Localization.string("settings_usage_list_separator")) +
            Localization.string("settings_usage_list_last_separator") +
            last()
}

private fun UsageScreenKind.noteKey(): String = when (this) {
    UsageScreenKind.STORAGE -> "settings_usage_storage_note"
    UsageScreenKind.NETWORK -> "settings_usage_network_note"
}

/**
 * User-facing category title used in rows and clear confirmation dialogs.
 *
 * @author Dmitriy Moroz
 */
fun UsageCategory.title(): String = Localization.string(
    when (this) {
        UsageCategory.CACHE -> "settings_usage_category_cache"
        UsageCategory.GALLERY -> "settings_usage_category_gallery"
        UsageCategory.MODELS_ONNX -> "settings_usage_category_models_onnx"
        UsageCategory.MODELS_MEDIAPIPE -> "settings_usage_category_models_mediapipe"
        UsageCategory.MODELS_SDXL -> "settings_usage_category_models_sdxl"
        UsageCategory.MODELS_CORE_ML -> "settings_usage_category_models_core_ml"
        UsageCategory.MODELS_MNN -> "srv_type_mnn"
        UsageCategory.MODELS_BONSAI -> "settings_usage_category_models_bonsai"
        UsageCategory.TRAFFIC_MODELS -> "settings_usage_category_traffic_models"
        UsageCategory.TRAFFIC_CONFIGS -> "settings_usage_category_traffic_configs"
        UsageCategory.TRAFFIC_INFERENCE -> "settings_usage_category_traffic_inference"
        UsageCategory.TRAFFIC_CIVITAI -> "civitai_title"
    },
)

/**
 * Short noun phrase used when summarizing which storage categories currently occupy space.
 *
 * @author Dmitriy Moroz
 */
fun UsageCategory.summaryTitle(): String = Localization.string(
    when (this) {
        UsageCategory.CACHE -> "settings_usage_summary_cache"
        UsageCategory.GALLERY -> "settings_usage_summary_gallery"
        UsageCategory.MODELS_ONNX -> "settings_usage_summary_models_onnx"
        UsageCategory.MODELS_MEDIAPIPE -> "settings_usage_summary_models_mediapipe"
        UsageCategory.MODELS_SDXL -> "settings_usage_summary_models_sdxl"
        UsageCategory.MODELS_CORE_ML -> "settings_usage_summary_models_core_ml"
        UsageCategory.MODELS_MNN -> "settings_usage_summary_models_mnn"
        UsageCategory.MODELS_BONSAI -> "settings_usage_summary_models_bonsai"
        UsageCategory.TRAFFIC_MODELS -> "settings_usage_category_traffic_models"
        UsageCategory.TRAFFIC_CONFIGS -> "settings_usage_category_traffic_configs"
        UsageCategory.TRAFFIC_INFERENCE -> "settings_usage_category_traffic_inference"
        UsageCategory.TRAFFIC_CIVITAI -> "civitai_title"
    },
)

/**
 * Keeps category colors consistent between the chart and its legend.
 *
 * @author Dmitriy Moroz
 */
internal fun UsageCategory.color(): Color = when (this) {
    UsageCategory.CACHE -> Color(0xFF4C7EF3)
    UsageCategory.GALLERY -> Color(0xFF6EC6F2)
    UsageCategory.MODELS_ONNX -> Color(0xFFF59F32)
    UsageCategory.MODELS_MEDIAPIPE -> Color(0xFFF04462)
    UsageCategory.MODELS_SDXL -> Color(0xFF59C96B)
    UsageCategory.MODELS_CORE_ML -> Color(0xFF7E66E8)
    UsageCategory.MODELS_MNN -> Color(0xFF577CAD)
    UsageCategory.MODELS_BONSAI -> Color(0xFF2DB7A3)
    UsageCategory.TRAFFIC_MODELS -> Color(0xFF3D8BFF)
    UsageCategory.TRAFFIC_CONFIGS -> Color(0xFF65C96F)
    UsageCategory.TRAFFIC_INFERENCE -> Color(0xFFF0A13A)
    UsageCategory.TRAFFIC_CIVITAI -> Color(0xFF8B7FE8)
}

/**
 * Formats a nonnegative byte count in decimal units for chart labels and category rows.
 *
 * @author Dmitriy Moroz
 */
internal fun Long.formatBytes(): String {
    val value = coerceAtLeast(0L)
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var unitIndex = 0
    var display = value.toDouble()
    while (display >= 1000.0 && unitIndex < units.lastIndex) {
        display /= 1000.0
        unitIndex++
    }

    return if (unitIndex == 0 || display >= 100.0) {
        "${display.toLong()} ${units[unitIndex]}"
    } else {
        "${(display * 10.0).toInt() / 10.0} ${units[unitIndex]}"
    }
}

private const val MIN_PERCENT_TENTH = 1
private const val PERCENT_TENTHS_TOTAL = 1000
