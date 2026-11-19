package com.shifthackz.aisdv1.presentation.generation.performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Displays two resource series on a fixed 60-sample, zero-to-capacity scale. The app uses the
 * theme accent; device totals use foreground color. Both series use solid traces.
 * @param label Localized resource name.
 * @param appValue Current app reading with its unit; null hides an unavailable value.
 * @param totalValue Current device-wide reading with its unit; null hides an unavailable value.
 * @param appValues Oldest-to-newest normalized app points, at most 60; nulls leave gaps.
 * @param totalValues Device-wide points in the same time window and scale.
 * @param modifier Layout applied to the metric and plot.
 * @author Dmitriy Moroz
 */
@Composable
internal fun PerformanceResourceGraph(
    label: String,
    appValue: String?,
    totalValue: String?,
    appValues: List<Float?>,
    totalValues: List<Float?>,
    modifier: Modifier = Modifier,
) {
    val appColor = MaterialTheme.colorScheme.primary
    val totalColor = MaterialTheme.colorScheme.onBackground
    val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
    val appLabel = Localization.string("performance_this_app")
    val totalLabel = Localization.string("performance_total")
    Column(modifier = modifier.semantics {
        contentDescription = listOfNotNull(
            label,
            appValue?.let { "$appLabel: $it" },
            totalValue?.let { "$totalLabel: $it" },
        ).joinToString(", ")
    }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Column(horizontalAlignment = Alignment.End) {
                appValue?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = appColor
                    )
                }
                totalValue?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = totalColor
                    )
                }
            }
        }
        Canvas(Modifier.fillMaxWidth().height(48.dp)) {
            for (line in 0..4) {
                val y = size.height * line / 4
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            for (line in 0..6) {
                val x = size.width * line / 6
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
            }
            drawSeries(totalValues, totalColor)
            drawSeries(appValues, appColor)
        }
    }
}

/**
 * Draws a translucent area beneath known points and a crisp trace above it.
 * Each contiguous segment closes to the baseline separately, preserving gaps for missing readings.
 * @param values Normalized series within the fixed 60-point viewport.
 * @param color Theme role shared with the numeric value and legend.
 * @author Dmitriy Moroz
 */
private fun DrawScope.drawSeries(
    values: List<Float?>,
    color: Color,
) {
    val path = Path()
    val area = Path()
    var lastX = 0f
    var hasPreviousPoint = false
    values.forEachIndexed { index, value ->
        if (value == null) {
            if (hasPreviousPoint) {
                area.lineTo(lastX, size.height)
                area.close()
            }
            hasPreviousPoint = false
        } else {
            val x = size.width * (60 - values.size + index) / 59f
            val y = size.height * (1 - value.coerceIn(0f, 1f))
            if (hasPreviousPoint) {
                path.lineTo(x, y)
                area.lineTo(x, y)
            } else {
                path.moveTo(x, y)
                area.moveTo(x, size.height)
                area.lineTo(x, y)
            }
            lastX = x
            hasPreviousPoint = true
        }
    }
    if (hasPreviousPoint) {
        area.lineTo(lastX, size.height)
        area.close()
    }
    drawPath(area, color.copy(alpha = 0.16f))
    drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}
