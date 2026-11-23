package com.shifthackz.aisdv1.presentation.generation.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import kotlin.math.roundToInt

/**
 * Displays a fixed resource history using the same app/device graphs as the live generation overlay.
 * Empty histories produce no panel and unavailable readings retain their graph gaps.
 *
 * @param samples Oldest-to-newest resource readings supplied by the caller.
 * @param modifier Placement of the CPU/RAM panel.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GenerationPerformanceContent(
    samples: List<PerformanceSample>,
    modifier: Modifier = Modifier,
) {
    val latest = samples.lastOrNull() ?: return
    val capacity = latest.totalMemoryBytes
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PerformanceResourceGraph(
            label = Localization.string("performance_cpu"),
            appValue = latest.cpuPercent?.let { "${it.roundToInt()}%" },
            totalValue = latest.deviceCpuPercent?.let { "${it.roundToInt()}%" },
            appValues = samples.map { it.cpuPercent?.div(100f) },
            totalValues = samples.map { it.deviceCpuPercent?.div(100f) },
        )
        PerformanceResourceGraph(
            label = Localization.string("performance_ram"),
            appValue = memoryValue(latest.residentBytes, capacity),
            totalValue = memoryValue(latest.deviceUsedMemoryBytes, capacity),
            appValues = samples.map { fraction(it.residentBytes, it.totalMemoryBytes) },
            totalValues = samples.map { fraction(it.deviceUsedMemoryBytes, it.totalMemoryBytes) },
        )
        PerformanceLegend()
    }
}

/**
 * Formats a known used/capacity pair in binary megabytes, preserving unavailable samples.
 *
 * @param used Resident app bytes or device-wide used bytes.
 * @param total Device physical capacity, not a per-process allocation limit.
 * @author Dmitriy Moroz
 */
private fun memoryValue(used: Long?, total: Long?): String? =
    if (used != null && total != null) Localization.string(
        "performance_memory_value", (used / 1_048_576).toString(), (total / 1_048_576).toString(),
    ) else null

/**
 * Maps byte counters to the graph's shared physical-capacity scale.
 *
 * @param used RAM in use for one series, or null for an unavailable reading.
 * @param total Device capacity in bytes.
 * @author Dmitriy Moroz
 */
private fun fraction(used: Long?, total: Long?): Float? =
    if (used != null && total != null && total > 0) used.toFloat() / total else null
