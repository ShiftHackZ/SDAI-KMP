package com.shifthackz.aisdv1.feature.onnx

import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.onnx.ai.tokenizer.LocalDiffusionTextTokenizer
import com.shifthackz.aisdv1.feature.onnx.ai.unet.UNet
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunController
import com.shifthackz.aisdv1.feature.onnx.environment.OrtEnvironmentProvider
import java.io.ByteArrayOutputStream

/**
 * One job's synchronous ONNX owner, executed entirely on the child inference executor.
 *
 * @param uNet Denoiser and decoder owned by this job.
 * @param tokenizer Job-scoped text session.
 * @param environment Native environment created only in the inference process.
 * @param controller Cooperative termination and run-options lifetime.
 * @author Dmitriy Moroz
 */
internal class LocalDiffusionImpl(
    private val uNet: UNet,
    private val tokenizer: LocalDiffusionTextTokenizer,
    private val environment: OrtEnvironmentProvider,
    private val controller: OnnxRunController,
) : NativeInferenceEngine {
    /**
     * Runs captured ONNX inputs on the inference owner and returns PNG bytes.
     * Native resources are released after execution returns, including failure and cancellation paths.
     *
     * @param request Immutable generation inputs.
     * @param onProgress Nonblocking step delivery.
     * @param onStage Sanitized model/tokenizer/denoise/decode stage.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray {
        val payload = request.payload
        require(payload.width % 8 == 0 && payload.height % 8 == 0)
        controller.checkActive()
        try {
            onStage(NativeGenerationStage.PREPARING)
            tokenizer.initialize()
            onStage(NativeGenerationStage.TOKENIZING)
            controller.checkActive()
            val positive = checkNotNull(tokenizer.tensor(tokenizer.encode(payload.prompt)))
            positive.use {
                val negative =
                    checkNotNull(tokenizer.tensor(tokenizer.createUnconditionalInput(payload.negativePrompt)))
                negative.use {
                    val positiveValues =
                        positive.floatBuffer.let { b -> FloatArray(b.remaining()).also(b::get) }
                    val negativeValues =
                        negative.floatBuffer.let { b -> FloatArray(b.remaining()).also(b::get) }
                    val embedding = Array(2) { Array(tokenizer.maxLength) { FloatArray(768) } }
                    for (index in positiveValues.indices) {
                        embedding[0][index / 768][index % 768] = negativeValues[index]
                        embedding[1][index / 768][index % 768] = positiveValues[index]
                    }
                    OnnxTensor.createTensor(environment.get(), embedding).use { text ->
                        tokenizer.close()
                        controller.checkActive()
                        uNet.initialize()
                        onStage(NativeGenerationStage.DENOISING)
                        val bitmap = uNet.inference(payload, text, onProgress, onStage)
                        try {
                            controller.checkActive()
                            return ByteArrayOutputStream().use { output ->
                                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                                output.toByteArray()
                            }
                        } finally {
                            bitmap.recycle()
                        }
                    }
                }
            }
        } finally {
            close()
        }
    }

    /**
     * Signals only; resource release stays in generate's owner-thread finally.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() {
        controller.cancel()
    }

    /**
     * Idempotent owner-thread cleanup after inference returns.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        try {
            tokenizer.close()
        } finally {
            try {
                uNet.close()
            } finally {
                controller.close()
            }
        }
    }
}
