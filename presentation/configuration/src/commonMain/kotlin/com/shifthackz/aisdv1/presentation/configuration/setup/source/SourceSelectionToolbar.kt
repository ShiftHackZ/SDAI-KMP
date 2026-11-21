package com.shifthackz.aisdv1.presentation.configuration.setup.source

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchField
import com.shifthackz.aisdv1.presentation.components.widget.action.CircleIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative

/** Height of the fixed 48dp actions plus the search row's vertical padding. @author Dmitriy Moroz */
internal val SourceSelectionToolbarHeight = 58.dp

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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isPlatformToolbarNative) Color.Transparent else MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, top = 2.dp, end = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            modifier = Modifier
                .weight(1f)
                .height(if (isPlatformToolbarNative) 48.dp else 40.dp),
            value = state.sourceSearchQuery,
            placeholder = strings.sourceSearchHint,
            onValueChange = { query ->
                processIntent(ServerSetupIntent.UpdateSourceSearchQuery(query))
            },
        )
        if (state.sourceSearchQuery.isNotBlank()) {
            SourceToolbarActionButton(
                icon = Icons.Default.Close,
                contentDescription = strings.clear,
                shouldShowDot = false,
                onClick = {
                    processIntent(ServerSetupIntent.UpdateSourceSearchQuery(""))
                },
            )
        }
        SourceToolbarActionButton(
            icon = Icons.Default.FilterList,
            contentDescription = strings.sourceFilterAction,
            shouldShowDot = state.hasActiveSourceFilters,
            onClick = { processIntent(ServerSetupIntent.ShowSourceFilters) },
        )
        SourceToolbarActionButton(
            contentDescription = strings.sourceSortAction,
            shouldShowDot = false,
            icon = MobileDataArrowsIcon,
            onClick = { processIntent(ServerSetupIntent.ShowSourceSort) },
        )
    }
}

/**
 * Keeps provider search/filter commands compact and marks active filters beside their icon.
 *
 * @param contentDescription Localized command label.
 * @param shouldShowDot Whether an accent indicator marks an active filter.
 * @param onClick Requests the existing setup action.
 * @param modifier Button placement.
 * @param icon Shared command vector rendered by the platform control.
 * @author Dmitriy Moroz
 */
@Composable
private fun SourceToolbarActionButton(
    contentDescription: String,
    shouldShowDot: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    if (isPlatformToolbarNative) {
        Box(modifier) {
            CircleIconButton(onClick, icon, contentDescription)
            if (shouldShowDot) {
                Box(
                    Modifier.align(Alignment.TopEnd)
                        .size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
        return
    }
    ToolbarIconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
            )
            if (shouldShowDot) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape,
                        ),
                )
            }
        }
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
