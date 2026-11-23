package com.shifthackz.aisdv1.presentation.generation.preview.performance

import com.shifthackz.aisdv1.domain.entity.PerformanceSample

/**
 * Fixed oldest-to-newest resource readings for the generation progress panel.
 *
 * @param name Identifies the reading availability or graph-bound condition.
 * @param samples Captured CPU/RAM series; empty histories render no panel.
 * @author Dmitriy Moroz
 */
internal data class PerformancePreviewCase(val name: String, val samples: List<PerformanceSample>)
