package com.shifthackz.aisdv1.domain.entity.benchmark

import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Conservative outcome for a concrete local generation provider.
 *
 * @param provider Provider.
 * @param recommended Whether this provider meets the conservative device requirements.
 * @param width Recommended output width in pixels.
 * @param height Recommended output height in pixels.
 * @param samplingSteps Recommended maximum inference step count.
 * @param cfgScale Recommended guidance scale.
 * @param batchCount Recommended maximum image batch size.
 * @param estimatedTimeSeconds Estimated warm generation duration in seconds.
 * @param backgroundGeneration Whether background work is recommended for this hardware.
 * @param sdxlBackend Recommended SDCPP execution backend.
 * @param issues Machine-readable reasons limiting this provider.
 * @author Dmitriy Moroz
 */
data class BenchmarkProviderRecommendation(
    val provider: ServerSource,
    val recommended: Boolean,
    val width: Int,
    val height: Int,
    val samplingSteps: Int,
    val cfgScale: Float,
    val batchCount: Int,
    val estimatedTimeSeconds: Int,
    val backgroundGeneration: Boolean,
    val sdxlBackend: SdxlBackend,
    val issues: List<BenchmarkProviderIssue> = emptyList(),
)
