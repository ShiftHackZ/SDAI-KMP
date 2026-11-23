package com.shifthackz.aisdv1.presentation.components.widget.surface

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Draws one card behind a contiguous range of independently composed lazy-list items.
 * Only the visible extent is measured; offscreen controls retain the list's normal lazy lifecycle.
 * Item surfaces share this backdrop by disabling their own background drawing.
 * Overscroll is drawn once around the backdrop and foreground, keeping their boundaries aligned.
 *
 * @param state State of the lazy list rendered by [content].
 * @param firstItemIndex Index where the card begins, after any preceding provider/image items.
 * @param modifier Bounds of the list viewport and its clipped backdrop.
 * @param lastItemIndex Inclusive card end; null extends through the list's final item.
 * @param horizontalPadding Horizontal list content padding surrounding the card.
 * @param color Card color or native glass tint, shared with the item surfaces.
 * @param cornerRadius Radius at the actual first and last card items.
 * @param tonalElevation Material tonal elevation, matching the item surfaces.
 * @param overscrollEffect Shared effect moving the backdrop and list together; the list must consume it with withoutVisualEffect().
 * @param content Lazy list whose controls remain in their existing composition.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun PlatformLazySurface(
    state: LazyListState,
    firstItemIndex: Int,
    modifier: Modifier = Modifier,
    lastItemIndex: Int? = null,
    horizontalPadding: Dp = 0.dp,
    color: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 16.dp,
    tonalElevation: Dp = 0.dp,
    overscrollEffect: OverscrollEffect? = null,
    content: @Composable () -> Unit,
) {
    require(firstItemIndex >= 0) { "First surface item index must be nonnegative" }
    require(lastItemIndex == null || lastItemIndex >= firstItemIndex) {
        "Last surface item index must not precede the first"
    }
    Box(modifier.clipToBounds().overscroll(overscrollEffect)) {
        LazySurfaceBackground(
            state, firstItemIndex, lastItemIndex, horizontalPadding, color, cornerRadius,
            tonalElevation, Modifier.matchParentSize(),
        )
        content()
    }
}

/**
 * Measures after the lazy list and places its backdrop from the same layout pass's item geometry.
 * Scrolling changes placement without waiting for recomposition; only outer-edge changes recompose.
 *
 * @param state Current visible item geometry.
 * @param firstItemIndex First item belonging to the card.
 * @param lastItemIndex Optional inclusive card end.
 * @param horizontalPadding Horizontal space outside the card.
 * @param color Shared backdrop tint.
 * @param cornerRadius Radius for visible outer card edges.
 * @param tonalElevation Material elevation of the card.
 * @param modifier Parent-sized backdrop measured after the foreground list.
 *
 * @author Dmitriy Moroz
 */
@Composable
private fun LazySurfaceBackground(
    state: LazyListState,
    firstItemIndex: Int,
    lastItemIndex: Int?,
    horizontalPadding: Dp,
    color: Color,
    cornerRadius: Dp,
    tonalElevation: Dp,
    modifier: Modifier,
) {
    val hasTopEdge by remember(state, firstItemIndex) {
        derivedStateOf { state.layoutInfo.visibleItemsInfo.any { it.index == firstItemIndex } }
    }
    val hasBottomEdge by remember(state, lastItemIndex) {
        derivedStateOf {
            val layout = state.layoutInfo
            val endIndex = lastItemIndex ?: (layout.totalItemsCount - 1)
            layout.visibleItemsInfo.any { it.index == endIndex }
        }
    }
    PlatformSurface(
        modifier = modifier.padding(horizontal = horizontalPadding)
            .layout { measurable, constraints ->
                val listLayout = state.layoutInfo
                val endIndex = lastItemIndex ?: (listLayout.totalItemsCount - 1)
                val items =
                    listLayout.visibleItemsInfo.filter { it.index in firstItemIndex..endIndex }
                val first = items.firstOrNull()
                val last = items.lastOrNull()
                val top = (first?.offset ?: 0) + listLayout.beforeContentPadding
                val height =
                    if (first != null && last != null) last.offset + last.size - first.offset else 0
                val placeable =
                    measurable.measure(constraints.copy(minHeight = height, maxHeight = height))
                layout(constraints.maxWidth, constraints.maxHeight) {
                    if (height > 0) placeable.placeRelative(0, top)
                }
            },
        shape = RoundedCornerShape(
            topStart = if (hasTopEdge) cornerRadius else 0.dp,
            topEnd = if (hasTopEdge) cornerRadius else 0.dp,
            bottomStart = if (hasBottomEdge) cornerRadius else 0.dp,
            bottomEnd = if (hasBottomEdge) cornerRadius else 0.dp,
        ),
        color = color,
        tonalElevation = tonalElevation,
        isTopEdgeVisible = hasTopEdge,
        isBottomEdgeVisible = hasBottomEdge,
    ) {}
}
