package com.shifthackz.aisdv1.domain.feature.diffusion

import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * Child-only runtime adapter. Creation, inference and close belong to one serial owner thread.
 *
 * @author Dmitriy Moroz
 */
interface NativeInferenceEngine : AutoCloseable {
    /**
     * Produces a complete encoded image before the IPC service publishes its output.
     *
     * @param request Immutable inputs for this single runtime instance.
     * @param onProgress Nonblocking native work counters, containing no image or prompt data.
     * Counters represent sampling steps only while onStage reports DENOISING; RUNNING counters are opaque.
     * @param onStage Sanitized native lifecycle stage.
     * @author Dmitriy Moroz
     */
    suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray

    /**
     * Signals cooperative termination; must never close a session concurrently with inference.
     *
     * @author Dmitriy Moroz
     */
    fun interrupt()

    /**
     * Releases owned native resources after generate has returned.
     *
     * @author Dmitriy Moroz
     */
    override fun close()
}
