package com.shifthackz.aisdv1.feature.onnx.ai.vae

import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap

/**
 * Owner-thread VAE contract with borrowed inputs and an explicitly transferred bitmap.
 *
 * @author Dmitriy Moroz
 */
internal interface VaeDecoder : AutoCloseable {
    /**
     * Decodes borrowed latent tensors into a caller-owned bitmap on the session owner thread.
     * Inputs remain alive until decoding returns; the caller handles bitmap disposal.
     *
     * @param input Latent tensors kept alive by the caller.
     * @param width Validated output width.
     * @param height Validated output height.
     * @return Bitmap owned by the caller until compression/disposal.
     * @author Dmitriy Moroz
     */
    fun decode(input: Map<String, OnnxTensor>, width: Int, height: Int): Bitmap

    /**
     * Releases the session only after decode returns.
     *
     * @author Dmitriy Moroz
     */
    override fun close()
}
