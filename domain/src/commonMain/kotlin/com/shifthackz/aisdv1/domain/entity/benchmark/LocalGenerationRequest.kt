package com.shifthackz.aisdv1.domain.entity.benchmark

import com.shifthackz.aisdv1.domain.entity.SdxlBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Captured generation settings compared against a previously saved benchmark recommendation.
 *
 * @param source Provider selected for this generation request.
 * @param width Requested output width in pixels.
 * @param height Requested output height in pixels.
 * @param samplingSteps Requested inference step count.
 * @param batchCount Requested number of output images.
 * @param hiresFix Whether the request enables an additional high-resolution pass.
 * @param sdxlBackend Requested SDCPP backend; AUTO accepts the saved recommendation.
 *
 * @author Dmitriy Moroz
 */
data class LocalGenerationRequest(
    val source: ServerSource,
    val width: Int,
    val height: Int,
    val samplingSteps: Int,
    val batchCount: Int,
    val hiresFix: Boolean = false,
    val sdxlBackend: SdxlBackend = SdxlBackend.AUTO,
)
