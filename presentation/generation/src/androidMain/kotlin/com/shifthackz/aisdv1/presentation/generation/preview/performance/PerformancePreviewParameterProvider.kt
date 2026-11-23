package com.shifthackz.aisdv1.presentation.generation.preview.performance

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.PerformanceSample

/**
 * Supplies full, partial, missing and saturated histories without starting the live resource sampler.
 *
 * @author Dmitriy Moroz
 */
internal class PerformancePreviewParameterProvider :
    PreviewParameterProvider<PerformancePreviewCase> {
    private val samples = GenerationPreviewFixtures.samples
    private val cases = listOf(
        PerformancePreviewCase("Full resource history", samples),
        PerformancePreviewCase("First reading", samples.take(1)),
        PerformancePreviewCase("No readings", emptyList()),
        PerformancePreviewCase("Unavailable readings", List(5) { PerformanceSample() }),
        PerformancePreviewCase(
            "App readings only",
            samples.map { it.copy(deviceCpuPercent = null, deviceUsedMemoryBytes = null) }),
        PerformancePreviewCase(
            "Device readings only",
            samples.map { it.copy(cpuPercent = null, residentBytes = null) }),
        PerformancePreviewCase(
            "Gaps in readings",
            samples.mapIndexed { index, sample -> if (index % 5 == 0) PerformanceSample() else sample }),
        PerformancePreviewCase(
            "Saturated resources",
            samples.map {
                it.copy(
                    cpuPercent = 100f,
                    deviceCpuPercent = 100f,
                    residentBytes = it.totalMemoryBytes,
                    deviceUsedMemoryBytes = it.totalMemoryBytes
                )
            }),
    )

    /**
     * Fixed resource series displayed without lifecycle collection or preference access.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<PerformancePreviewCase> = cases.asSequence()

    /**
     * Identifies the history length, availability or graph-bound condition.
     *
     * @param index Position in the ordered resource-history list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
