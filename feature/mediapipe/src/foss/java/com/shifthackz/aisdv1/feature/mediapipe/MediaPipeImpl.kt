package com.shifthackz.aisdv1.feature.mediapipe

import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine

/**
 * FOSS adapter provides a controlled failure without linking Google runtime classes.
 *
 * @author Dmitriy Moroz
 */
internal class MediaPipeImpl : NativeInferenceEngine {
    /**
     * Fails explicitly without creating a Google runtime in the FOSS flavor.
     *
     * @param request Unsupported runtime request.
     * @param onStage Sanitized lifecycle-stage callback.
     * @param onProgress Unused progress callback.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray =
        error("Google AI MediaPipe is not supported on FOSS build.")

    /**
     * No native runtime is created.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = Unit

    /**
     * No resources are owned.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = Unit
}
