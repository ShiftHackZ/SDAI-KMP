package com.shifthackz.aisdv1.presentation.components.widget.scrollbar

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import kotlin.math.ceil

/**
 * Draws a passive vertical scroll thumb for a nonlazy scroll container.
 *
 * @receiver Content modifier to decorate without consuming scroll gestures.
 *
 * @param state Scroll offset and maximum supplied by the container.
 *
 * @param topInset Space above the indicator track, excluding an overlapping toolbar.
 *
 * @return Modifier drawing the thumb after content.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun Modifier.verticalScrollbar(
    state: ScrollState,
    topInset: Dp = 0.dp,
): Modifier = verticalScrollbar(
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
    topInset = topInset,
) {
    val viewportHeight = size.height
    val maxValue = state.maxValue.toFloat()
    if (viewportHeight <= 0f || maxValue <= 0f) return@verticalScrollbar null

    val totalHeight = maxValue + viewportHeight
    val thumbHeight = thumbHeight(viewportHeight, totalHeight)
    val thumbOffset = state.value / maxValue * (viewportHeight - thumbHeight)
    ScrollbarThumb(offset = thumbOffset, size = thumbHeight)
}

/**
 * Draws a passive vertical thumb estimated from the visible rows of a lazy grid.
 *
 * @receiver Grid modifier to decorate without consuming gestures.
 *
 * @param state Grid viewport and visible-item measurements.
 *
 * @param topInset Space above the indicator track, excluding an overlapping toolbar.
 *
 * @return Modifier drawing an estimated scroll thumb.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun Modifier.verticalScrollbar(
    state: LazyGridState,
    topInset: Dp = 0.dp,
): Modifier = verticalScrollbar(
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
    topInset = topInset,
) {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    if (size.height <= 0f || totalItems == 0 || visibleItems.isEmpty()) {
        return@verticalScrollbar null
    }

    val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
        .toFloat()
        .takeIf { it > 0f }
        ?: size.height
    val firstRowY = visibleItems.first().offset.y
    val columnCount = visibleItems.count { it.offset.y == firstRowY }.coerceAtLeast(1)
    val rowCount = ceil(totalItems / columnCount.toFloat())
    val averageRowHeight = visibleItems
        .map { it.size.height }
        .average()
        .toFloat()
        .takeIf { it > 0f }
        ?: return@verticalScrollbar null
    val totalHeight = averageRowHeight * rowCount
    val maxScroll = totalHeight - viewportHeight
    if (maxScroll <= 0f) return@verticalScrollbar null

    val firstVisibleRow = state.firstVisibleItemIndex / columnCount
    val scrollOffset = firstVisibleRow * averageRowHeight + state.firstVisibleItemScrollOffset
    val thumbHeight = thumbHeight(viewportHeight, totalHeight)
    val progress = scrollOffset / maxScroll
    val thumbOffset = progress.coerceIn(0f, 1f) * (viewportHeight - thumbHeight)
    ScrollbarThumb(offset = thumbOffset, size = thumbHeight)
}

/**
 * Draws a passive vertical thumb estimated from the visible items of a lazy list.
 *
 * @receiver List modifier to decorate without consuming gestures.
 *
 * @param state List viewport and visible-item measurements.
 *
 * @param topInset Space above the indicator track, excluding an overlapping toolbar.
 *
 * @return Modifier drawing an estimated scroll thumb.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun Modifier.verticalScrollbar(
    state: LazyListState,
    topInset: Dp = 0.dp,
): Modifier = verticalScrollbar(
    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
    topInset = topInset,
) {
    val layoutInfo = state.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    if (size.height <= 0f || totalItems == 0 || visibleItems.isEmpty()) {
        return@verticalScrollbar null
    }

    val viewportHeight = (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset)
        .toFloat()
        .takeIf { it > 0f }
        ?: size.height
    val averageItemHeight = visibleItems
        .map { it.size }
        .average()
        .toFloat()
        .takeIf { it > 0f }
        ?: return@verticalScrollbar null
    val totalHeight = averageItemHeight * totalItems
    val maxScroll = totalHeight - viewportHeight
    if (maxScroll <= 0f) return@verticalScrollbar null

    val scrollOffset = state.firstVisibleItemIndex * averageItemHeight +
            state.firstVisibleItemScrollOffset
    val thumbHeight = thumbHeight(viewportHeight, totalHeight)
    val progress = scrollOffset / maxScroll
    val thumbOffset = progress.coerceIn(0f, 1f) * (viewportHeight - thumbHeight)
    ScrollbarThumb(offset = thumbOffset, size = thumbHeight)
}


private data class ScrollbarThumb(
    val offset: Float,
    val size: Float,
)


private fun DrawScope.thumbHeight(
    viewportHeight: Float,
    totalHeight: Float,
): Float = (viewportHeight * viewportHeight / totalHeight)
    .coerceAtLeast(28.dp.toPx())


private fun Modifier.verticalScrollbar(
    color: Color,
    topInset: Dp,
    thumbProvider: DrawScope.() -> ScrollbarThumb?,
): Modifier = drawWithContent {
    drawContent()
    val thumb = thumbProvider() ?: return@drawWithContent
    val width = 3.dp.toPx()
    val margin = 2.dp.toPx()
    val trackTop = topInset.toPx().coerceIn(0f, size.height)
    val trackHeight = size.height - trackTop
    if (trackHeight <= 0f || size.height <= 0f) return@drawWithContent
    val scale = trackHeight / size.height
    val thumbSize = thumb.size * scale
    val top =
        trackTop + (thumb.offset * scale).coerceIn(0f, (trackHeight - thumbSize).coerceAtLeast(0f))
    drawRoundRect(
        color = color,
        topLeft = Offset(x = size.width - width - margin, y = top),
        size = Size(width, thumbSize),
        cornerRadius = CornerRadius(width, width),
    )
}
