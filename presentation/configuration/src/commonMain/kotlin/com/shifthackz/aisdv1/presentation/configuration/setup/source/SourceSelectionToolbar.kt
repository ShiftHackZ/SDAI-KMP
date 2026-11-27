package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchFilterToolbar
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchToolbarAction
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings

/**
 * Compact toolbar for provider search, filter, and sort actions.
 *
 * @param state current query/filter state used to show active indicators.
 * @param strings localized hints and action labels.
 * @param processIntent sends toolbar events to the setup MVI pipeline.
 * @param modifier Layout modifiers for the provider search/filter/sort row.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceSelectionToolbar(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProviderDiscoveryToolbar(
        query = state.sourceSearchQuery,
        hasActiveFilters = state.hasActiveSourceFilters,
        strings = strings,
        onQueryChange = { processIntent(ServerSetupIntent.UpdateSourceSearchQuery(it)) },
        onOpenFilters = { processIntent(ServerSetupIntent.ShowSourceFilters) },
        onOpenSort = { processIntent(ServerSetupIntent.ShowSourceSort) },
        modifier = modifier,
    )
}

/**
 * Shared search, active-filter indicator and sort action for both provider pickers.
 *
 * @param query Current search editor contents.
 * @param hasActiveFilters Whether any capability, hosting or readiness filter is active.
 * @param strings Localized search and action labels.
 * @param onQueryChange Publishes edits immediately to the owning draft.
 * @param onOpenFilters Opens the owning filter sheet.
 * @param onOpenSort Opens the owning sort sheet.
 * @param modifier Layout of the complete discovery row.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderDiscoveryToolbar(
    query: String, hasActiveFilters: Boolean,
    strings: ServerSetupStrings, onQueryChange: (String) -> Unit,
    onOpenFilters: () -> Unit, onOpenSort: () -> Unit, modifier: Modifier = Modifier
) {
    SearchFilterToolbar(
        query = query,
        searchHint = strings.sourceSearchHint,
        clearLabel = strings.clear,
        filterLabel = strings.sourceFilterAction,
        hasActiveFilters = hasActiveFilters,
        onQueryChange = onQueryChange,
        onOpenFilters = onOpenFilters,
        modifier = modifier,
    ) {
        SearchToolbarAction(
            contentDescription = strings.sourceSortAction,
            shouldShowDot = false,
            icon = MobileDataArrowsIcon,
            onClick = onOpenSort,
        )
    }
}

private val ServerSetupState.hasActiveSourceFilters: Boolean
    get() = sourceTypeFilter != null || sourceReadinessFilters.isNotEmpty() || sourceTagFilters.isNotEmpty()

private val MobileDataArrowsIcon: ImageVector
    get() {
        if (_mobileDataArrowsIcon != null) return _mobileDataArrowsIcon!!
        _mobileDataArrowsIcon = ImageVector.Builder(
            name = "MobileDataArrows",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.4f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(8f, 19.5f)
                lineTo(8f, 4.5f)
                moveTo(8f, 4.5f)
                lineTo(4.8f, 7.7f)
                moveTo(8f, 4.5f)
                lineTo(11.2f, 7.7f)
                moveTo(16f, 4.5f)
                lineTo(16f, 19.5f)
                moveTo(16f, 19.5f)
                lineTo(12.8f, 16.3f)
                moveTo(16f, 19.5f)
                lineTo(19.2f, 16.3f)
            }
        }.build()
        return _mobileDataArrowsIcon!!
    }

private var _mobileDataArrowsIcon: ImageVector? = null
