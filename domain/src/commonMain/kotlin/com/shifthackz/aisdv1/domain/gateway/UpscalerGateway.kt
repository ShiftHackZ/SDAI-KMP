package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import kotlinx.coroutines.flow.Flow

/**
 * Runs pixel-space super-resolution without changing the selected generation provider.
 * Unsupported platforms reject execution and do not load native libraries.
 *
 * @author Dmitriy Moroz
 */
interface UpscalerGateway {
    /**
     * Observes lifecycle stages of the active super-resolution attempt without starting inference.
     *
     * @author Dmitriy Moroz
     */
    fun observeStage(): Flow<NativeGenerationStage>

    /**
     * Enlarges one encoded image fourfold through an owned inference attempt.
     * Cancellation tears down the attempt before another image can start.
     *
     * @param image Base64 encoded source image.
     * @param modelPath Complete app-owned checkpoint path for the selected provider.
     * @param width Source width in pixels.
     * @param height Source height in pixels.
     * @return Base64 encoded PNG at four times the input dimensions.
     * @author Dmitriy Moroz
     */
    suspend fun upscale(image: String, modelPath: String, width: Int, height: Int): String
}
