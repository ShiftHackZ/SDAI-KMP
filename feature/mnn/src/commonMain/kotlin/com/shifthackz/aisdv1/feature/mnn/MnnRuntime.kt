package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest

/**
 * Platform-neutral execution boundary for one MNN inference attempt; unsupported platforms reject generation.
 *
 * @author Dmitriy Moroz
 */
interface MnnRuntime {
    /**
     * Loads and runs a fresh runtime, returning only complete PNG bytes.
     *
     * @param request Validated immutable SD 1.5 inputs and model directory.
     * @param onProgress Overall completion percentage, never a denoising step counter.
     *
     * @author Dmitriy Moroz
     */
    suspend fun generate(request: NativeGenerationRequest, onProgress: (Int) -> Unit): ByteArray

    /**
     * Requests cancellation without concurrently destroying the executing runtime.
     *
     * @param ownerId Optional exact Apple operation identity; Android uses its host gateway for named cancellation.
     *
     * @author Dmitriy Moroz
     */
    fun interrupt(ownerId: String? = null)
}
