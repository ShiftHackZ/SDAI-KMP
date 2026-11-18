package com.shifthackz.aisdv1.domain.entity.benchmark

import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Recommended local generation parameters derived from benchmark results.
 *
 * @param width Recommended output width in pixels.
 * @param height Recommended output height in pixels.
 * @param samplingSteps Recommended maximum inference step count.
 * @param cfgScale Recommended guidance scale.
 * @param batchCount Recommended maximum image batch size.
 * @param providers Local providers considered usable by this recommendation.
 * @param backgroundGeneration Whether background work is recommended for this hardware.
 * @param sdxlBackend Recommended SDCPP execution backend.
 * @author Dmitriy Moroz
 */
data class BenchmarkRecommendation(
    val width: Int,
    val height: Int,
    val samplingSteps: Int,
    val cfgScale: Float,
    val batchCount: Int,
    val providers: List<ServerSource>,
    val backgroundGeneration: Boolean,
    val sdxlBackend: SdxlBackend,
)
