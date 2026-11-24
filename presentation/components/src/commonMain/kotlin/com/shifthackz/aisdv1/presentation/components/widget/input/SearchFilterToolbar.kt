package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.action.CircleIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative

/**
 * Height reserved for the platform search row, including its 48dp actions and vertical padding.
 *
 * @author Dmitriy Moroz
 */
val SearchFilterToolbarHeight = 58.dp

/**
 * Places a platform search field and its clear and filter actions in a fixed toolbar row.
 *
 * @param query Current search text.
 * @param searchHint Localized field placeholder.
 * @param clearLabel Localized accessible name for clearing the search.
 * @param filterLabel Localized accessible name for opening filters.
 * @param hasActiveFilters Whether to mark the filter button with an accent dot.
 * @param onQueryChange Receives edits and the empty query from the clear action.
 * @param onOpenFilters Opens the caller's filter sheet.
 * @param modifier Placement within the route toolbar.
 * @param trailingActions Optional controls following the filter button.
 * @author Dmitriy Moroz
 */
@Composable
fun SearchFilterToolbar(
    query: String,
    searchHint: String,
    clearLabel: String,
    filterLabel: String,
    hasActiveFilters: Boolean,
    onQueryChange: (String) -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
    trailingActions: @Composable RowScope.() -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isPlatformToolbarNative) Color.Transparent else MaterialTheme.colorScheme.surface)
            .padding(start = 16.dp, top = 2.dp, end = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchField(
            modifier = Modifier.weight(1f).height(if (isPlatformToolbarNative) 48.dp else 40.dp),
            value = query,
            onValueChange = onQueryChange,
            placeholder = searchHint,
        )
        if (query.isNotBlank()) {
            SearchToolbarAction(
                icon = Icons.Default.Close,
                contentDescription = clearLabel,
                shouldShowDot = false,
                onClick = { onQueryChange("") },
            )
        }
        SearchToolbarAction(
            icon = Icons.Default.FilterList,
            contentDescription = filterLabel,
            shouldShowDot = hasActiveFilters,
            onClick = {
                focusManager.clearFocus()
                onOpenFilters()
            },
        )
        trailingActions()
    }
}

/**
 * Keeps search commands compact and marks active filters beside their icon.
 *
 * @param contentDescription Localized command label.
 * @param shouldShowDot Whether an accent indicator marks an active filter.
 * @param onClick Runs the toolbar command.
 * @param modifier Button placement.
 * @param icon Shared command vector rendered by the platform control.
 * @author Dmitriy Moroz
 */
@Composable
fun SearchToolbarAction(
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
