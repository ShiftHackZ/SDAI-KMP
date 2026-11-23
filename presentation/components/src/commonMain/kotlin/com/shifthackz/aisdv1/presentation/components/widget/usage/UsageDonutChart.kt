package com.shifthackz.aisdv1.presentation.components.widget.usage

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.model.UsageState
import com.shifthackz.aisdv1.presentation.components.widget.surface.isPlatformSurfaceGlassAvailable
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Shows category proportions with a translucent glass finish on iOS and the original Android colors.
 * Segment taps select their category; taps in the hole or outside the ring restore the total.
 *
 * @param usage Measured category bytes and the current selection.
 * @param onSelect Receives the tapped category, or null to show the total.
 * @param modifier Chart size and placement within the usage screen.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun UsageDonutChart(
    usage: UsageState,
    onSelect: (UsageCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 720,
            easing = FastOutSlowInEasing,
        ),
        label = "usage-chart-progress",
    )
    val totalBytes = usage.totalBytes
    val segments = remember(usage.items, totalBytes) {
        usage.items.chartSegments(totalBytes)
    }
    val selectedItem = usage.selectedItem
    val centerValue = selectedItem?.bytes ?: totalBytes
    val centerLabel = selectedItem?.category?.title() ?: Localization.string("settings_usage_total")

    val shouldShowGlass = isPlatformSurfaceGlassAvailable()
    val selectedStroke = 42.dp
    val ringInset = selectedStroke / 2 + 2.dp
    val visibleAngle = 360f * progress
    val arcs = segments.mapNotNull { segment ->
        val sweep = (minOf(segment.startAngle + segment.sweepAngle, visibleAngle) -
                segment.startAngle).coerceIn(0f, segment.sweepAngle)
        if (sweep <= 0f) return@mapNotNull null
        val isSelected = segment.item.category == usage.selectedCategory
        UsageChartArc(
            category = segment.item.category,
            startAngle = -90f + segment.startAngle,
            sweepAngle = sweep,
            strokeWidth = if (isSelected) selectedStroke else 34.dp,
            color = segment.item.category.color().copy(
                alpha = if (shouldShowGlass) {
                    if (isSelected) 0.85f else 0.6f
                } else if (isSelected) 1f else 0.82f,
            ),
        )
    }
    Box(
        modifier = modifier.size(246.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(arcs, onSelect) {
                    detectTapGestures { offset ->
                        onSelect(
                            hitCategory(
                                offset = offset,
                                arcs = arcs,
                                ringInsetPx = ringInset.toPx(),
                                canvasWidth = size.width.toFloat(),
                                canvasHeight = size.height.toFloat(),
                            )
                        )
                    }
                },
        ) {
            val diameter = size.minDimension
            val inset = ringInset.toPx()
            val arcSize = Size(diameter - inset * 2f, diameter - inset * 2f)
            val topLeft = Offset(
                x = (size.width - arcSize.width) / 2f,
                y = (size.height - arcSize.height) / 2f,
            )
            if (arcs.isEmpty()) {
                drawCircle(
                    color = Color(0xFF697183).copy(alpha = 0.16f),
                    radius = arcSize.width / 2f,
                    center = center,
                    style = Stroke(width = 34.dp.toPx(), cap = StrokeCap.Round),
                )
            }
            arcs.forEach { arc ->
                drawUsageChartArc(
                    arc = arc,
                    topLeft = topLeft,
                    arcSize = arcSize,
                    shouldShowGlass = shouldShowGlass,
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = centerValue.formatBytes(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = centerLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Resolves a tap against the same angular range and thickness as the painted glass or Canvas arc.
 *
 * @param offset Tap position relative to the chart canvas.
 * @param arcs Current painted categories, angular ranges and thicknesses.
 * @param ringInsetPx Distance from the canvas edge to the ring centerline, in pixels.
 * @param canvasWidth Current measured canvas width in pixels.
 * @param canvasHeight Current measured canvas height in pixels.
 * @receiver Density converting shared segment thicknesses to canvas pixels.
 * @return Category under the tap, or null for the hole and outside the circle.
 *
 * @author Dmitriy Moroz
 */
private fun Density.hitCategory(
    offset: Offset,
    arcs: List<UsageChartArc>,
    ringInsetPx: Float,
    canvasWidth: Float,
    canvasHeight: Float,
): UsageCategory? {
    if (arcs.isEmpty()) return null

    val centerX = canvasWidth / 2f
    val centerY = canvasHeight / 2f
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val distance = hypot(dx, dy)
    val angle = ((atan2(dy, dx) * 180f / PI.toFloat()) + 450f) % 360f
    val arc = arcs.firstOrNull {
        val startAngle = it.startAngle + 90f
        angle >= startAngle && angle < startAngle + it.sweepAngle
    } ?: return null
    val centerlineRadius = minOf(canvasWidth, canvasHeight) / 2f - ringInsetPx
    val halfStroke = arc.strokeWidth.toPx() / 2f
    return arc.category.takeIf { distance in (centerlineRadius - halfStroke)..(centerlineRadius + halfStroke) }
}

/**
 * Associates one category with its contiguous angular portion of the donut.
 *
 * @param item Category and measured bytes represented by the segment.
 * @param startAngle Clockwise start in degrees from the top of the circle.
 * @param sweepAngle Angular extent in degrees, including the minimum tap target allowance.
 *
 * @author Dmitriy Moroz
 */
private data class UsageChartSegment(
    val item: UsageItem,
    val startAngle: Float,
    val sweepAngle: Float,
)

/**
 * Builds tappable donut segments that always fill exactly 360 degrees.
 *
 * Very small non-empty categories get a minimum visual sweep so users can still tap them, while the
 * largest segment absorbs floating-point correction to avoid gray gaps between arcs.
 *
 * @param totalBytes Sum of enabled category bytes used as the raw sweep denominator.
 * @receiver Ordered usage rows rendered by the current dashboard.
 *
 * @author Dmitriy Moroz
 */
private fun List<UsageItem>.chartSegments(
    totalBytes: Long,
): List<UsageChartSegment> {
    val enabledItems = filter(UsageItem::enabled)
    if (totalBytes <= 0L || enabledItems.isEmpty()) return emptyList()
    if (enabledItems.size == 1) {
        return listOf(
            UsageChartSegment(
                item = enabledItems.first(),
                startAngle = 0f,
                sweepAngle = 360f,
            ),
        )
    }

    val minSweep = MIN_CHART_SWEEP_DEGREES.coerceAtMost(360f / enabledItems.size * 0.6f)
    val rawSweeps = enabledItems.map { item ->
        item.bytes.toFloat() / totalBytes.toFloat() * 360f
    }
    val smallIndexes = rawSweeps
        .mapIndexedNotNull { index, sweep -> index.takeIf { sweep < minSweep } }
        .toSet()

    val sweeps = if (smallIndexes.size == enabledItems.size) {
        List(enabledItems.size) { 360f / enabledItems.size }
    } else {
        val reserved = smallIndexes.size * minSweep
        val remaining = (360f - reserved).coerceAtLeast(0f)
        val largeRawTotal = rawSweeps
            .filterIndexed { index, _ -> index !in smallIndexes }
            .sum()
            .takeIf { it > 0f }
            ?: 1f
        rawSweeps.mapIndexed { index, sweep ->
            if (index in smallIndexes) {
                minSweep
            } else {
                sweep / largeRawTotal * remaining
            }
        }
    }.toMutableList()

    val correction = 360f - sweeps.sum()
    val correctionIndex = sweeps.indices.maxByOrNull { sweeps[it] } ?: 0
    sweeps[correctionIndex] += correction

    var cursor = 0f
    return enabledItems.mapIndexed { index, item ->
        UsageChartSegment(
            item = item,
            startAngle = cursor,
            sweepAngle = sweeps[index],
        ).also {
            cursor += sweeps[index]
        }
    }
}

private const val MIN_CHART_SWEEP_DEGREES = 6f
