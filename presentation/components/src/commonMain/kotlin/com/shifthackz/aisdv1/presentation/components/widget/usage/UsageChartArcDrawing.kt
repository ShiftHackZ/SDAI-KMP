package com.shifthackz.aisdv1.presentation.components.widget.usage

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/**
 * Paints a hollow ring segment with an iOS translucent finish confined to its actual thickness.
 * Highlights follow both curved edges without adding a backdrop, filled center or cast shadow.
 *
 * @param arc Category tint, angles and selected or normal thickness.
 * @param topLeft Bounds of the shared ring centerline.
 * @param arcSize Diameter of the shared ring centerline.
 * @param shouldShowGlass Whether to add the iOS translucent finish instead of flat Android colors.
 * @receiver Canvas drawing the chart at its measured density.
 *
 * @author Dmitriy Moroz
 */
internal fun DrawScope.drawUsageChartArc(
    arc: UsageChartArc,
    topLeft: Offset,
    arcSize: Size,
    shouldShowGlass: Boolean,
) {
    val strokeWidth = arc.strokeWidth.toPx()
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
    if (!shouldShowGlass) {
        drawArc(
            color = arc.color,
            startAngle = arc.startAngle,
            sweepAngle = arc.sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = stroke,
        )
        return
    }
    drawArc(
        brush = Brush.linearGradient(
            colors = listOf(
                lerp(arc.color, Color.White.copy(alpha = arc.color.alpha), 0.24f),
                arc.color,
                lerp(arc.color, Color.White.copy(alpha = arc.color.alpha), 0.08f),
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
        startAngle = arc.startAngle,
        sweepAngle = arc.sweepAngle,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = stroke,
    )
    val edgeWidth = 1.dp.toPx()
    val edgeBrush = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = 0.48f), Color.White.copy(alpha = 0.02f)),
        start = Offset.Zero,
        end = Offset(size.width, size.height),
    )
    val edgeOffset = (strokeWidth - edgeWidth) / 2f
    listOf(-edgeOffset, edgeOffset).forEach { inset ->
        drawArc(
            brush = edgeBrush,
            startAngle = arc.startAngle,
            sweepAngle = arc.sweepAngle,
            useCenter = false,
            topLeft = topLeft + Offset(inset, inset),
            size = Size(arcSize.width - inset * 2f, arcSize.height - inset * 2f),
            style = Stroke(width = edgeWidth, cap = StrokeCap.Butt),
        )
    }
}
