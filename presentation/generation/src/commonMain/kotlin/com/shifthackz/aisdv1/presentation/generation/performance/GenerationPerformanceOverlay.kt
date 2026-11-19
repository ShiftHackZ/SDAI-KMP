package com.shifthackz.aisdv1.presentation.generation.performance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

/**
 * Collects resource history only while the generation dialog's tab is visible and resumed.
 * Removing the dialog or backgrounding the app cancels collection and discards its history.
 *
 * @param history Cold opt-in sampler supplied by the application composition root.
 * @param modifier Layout applied to the resource panel.
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationPerformanceOverlay(
    history: Flow<List<PerformanceSample>>,
    modifier: Modifier = Modifier,
) {
    val isVisible = LocalMviVisibility.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var samples by remember(history) { mutableStateOf(emptyList<PerformanceSample>()) }
    LaunchedEffect(history, isVisible, lifecycle) {
        samples = emptyList()
        if (isVisible) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                history.collect { samples = it }
            } finally {
                samples = emptyList()
            }
        }
    }
    val latest = samples.lastOrNull() ?: return
    val capacity = latest.totalMemoryBytes
    Column(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            Localization.string("performance_app_usage"),
            style = MaterialTheme.typography.labelMedium
        )
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
 * @param used RAM in use for one series, or null for an unavailable reading.
 * @param total Device capacity in bytes.
 * @author Dmitriy Moroz
 */
private fun fraction(used: Long?, total: Long?): Float? =
    if (used != null && total != null && total > 0) used.toFloat() / total else null
