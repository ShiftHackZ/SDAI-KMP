package com.shifthackz.aisdv1.presentation.components.widget.usage

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory

/**
 * One ring segment shared by painting, glass highlights and hit testing.
 *
 * @param category Usage bucket selected when this painted segment is tapped.
 * @param startAngle Clockwise start in degrees from the circle's right-hand axis.
 * @param sweepAngle Clockwise extent in degrees.
 * @param strokeWidth Visible segment thickness, including selection emphasis.
 * @param color Category tint and opacity for the current platform and selection.
 *
 * @author Dmitriy Moroz
 */
internal data class UsageChartArc(
    val category: UsageCategory,
    val startAngle: Float,
    val sweepAngle: Float,
    val strokeWidth: Dp,
    val color: Color,
)
