package com.shifthackz.aisdv1.feature.onnx.ai.unet

import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload

/**
 * Serialized denoising and VAE contract for one child-owned ONNX attempt.
 *
 * @author Dmitriy Moroz
 */
internal interface UNet : AutoCloseable {
    /**
     * Opens the captured model using the submitted NNAPI preference.
     * Session options remain alive until the denoiser session closes.
     *
     * @author Dmitriy Moroz
     */
    fun initialize()

    /**
     * Runs the sampling loop and VAE decode with borrowed embeddings on the inference owner.
     * The caller owns the returned bitmap and disposes it after compression.
     *
     * @param payload Immutable dimensions, seed and sampling parameters.
     * @param embeddings Borrowed text tensor owned by the caller.
     * @param onProgress Nonblocking sanitized step delivery.
     * @param onStage Native stage delivery without request contents.
     * @return Owned bitmap for caller compression and disposal.
     * @author Dmitriy Moroz
     */
    fun inference(
        payload: TextToImagePayload,
        embeddings: OnnxTensor,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): Bitmap

    /**
     * Closes sessions after inference has returned.
     *
     * @author Dmitriy Moroz
     */
    override fun close()
}
