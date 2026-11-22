package com.shifthackz.aisdv1.presentation.generation.performance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Shares one accessible legend across CPU and RAM, using the same colors and stroke styles.
 *
 * @param modifier Layout applied to the legend row.
 * @author Dmitriy Moroz
 */
@Composable
internal fun PerformanceLegend(modifier: Modifier = Modifier) {
    val appColor = MaterialTheme.colorScheme.primary
    val totalColor = MaterialTheme.colorScheme.onBackground
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(Modifier.size(18.dp, 8.dp)) {
                drawLine(
                    appColor,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    2.dp.toPx()
                )
            }
            Text(
                Localization.string("performance_this_app"),
                style = MaterialTheme.typography.bodySmall,
                color = appColor
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Canvas(Modifier.size(18.dp, 8.dp)) {
                drawLine(
                    totalColor,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    2.dp.toPx()
                )
            }
            Text(
                Localization.string("performance_total"),
                style = MaterialTheme.typography.bodySmall,
                color = totalColor
            )
        }
    }
}
