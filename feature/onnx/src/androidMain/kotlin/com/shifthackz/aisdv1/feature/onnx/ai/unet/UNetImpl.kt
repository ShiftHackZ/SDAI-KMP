@file:Suppress("UNCHECKED_CAST")

package com.shifthackz.aisdv1.feature.onnx.ai.unet

import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract
import com.shifthackz.aisdv1.feature.onnx.ai.extensions.duplicate
import com.shifthackz.aisdv1.feature.onnx.ai.extensions.getSizes
import com.shifthackz.aisdv1.feature.onnx.ai.extensions.multipleTensorsByFloat
import com.shifthackz.aisdv1.feature.onnx.ai.extensions.splitTensor
import com.shifthackz.aisdv1.feature.onnx.ai.scheduler.LocalDiffusionScheduler
import com.shifthackz.aisdv1.feature.onnx.ai.vae.VaeDecoder
import com.shifthackz.aisdv1.feature.onnx.entity.Array3D
import com.shifthackz.aisdv1.feature.onnx.entity.LocalDiffusionTensor
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunController
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSession
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSessionFactory
import com.shifthackz.aisdv1.feature.onnx.environment.OrtEnvironmentProvider
import java.nio.IntBuffer
import java.util.Random
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Denoises one image with explicit tensor ownership; callers close only after this owner returns.
 *
 * @param environment Child native environment.
 * @param sessionFactory Compatible provider settings and native resource ownership.
 * @param controller Run cancellation without concurrent destruction.
 * @param decoder Job-scoped VAE collaborator.
 * @param scheduler Job-scoped Euler scheduler.
 * @author Dmitriy Moroz
 */
internal class UNetImpl(
    private val environment: OrtEnvironmentProvider,
    private val sessionFactory: OnnxSessionFactory,
    private val controller: OnnxRunController,
    private val decoder: VaeDecoder,
    private val scheduler: LocalDiffusionScheduler,
) : UNet {

    private var session: OnnxSession? = null

    /**
     * Opens the denoiser with captured provider settings and retains its native session options.
     *
     * @author Dmitriy Moroz
     */
    override fun initialize() {
        controller.checkActive()
        if (session != null) return
        session = sessionFactory.create(
            LocalDiffusionContract.UNET_MODEL,
            isAccelerationAllowed = true,
        )
    }

    /**
     * Preserves the existing scheduler and guidance math with bounded per-step resource lifetime.
     *
     * @param payload Immutable image parameters.
     * @param embeddings Borrowed text tensor, kept alive by the caller.
     * @param onProgress Step callback.
     * @param onStage Sanitized denoising/decode stage.
     * @author Dmitriy Moroz
     */
    override fun inference(
        payload: TextToImagePayload,
        embeddings: OnnxTensor,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): Bitmap {
        val width = payload.width
        val height = payload.height
        val seed = payload.seed.toLongOrNull()?.takeIf { it > 0 } ?: Random().nextLong()
        val random = Random(seed)
        val shape = longArrayOf(1, 4, (height / 8).toLong(), (width / 8).toLong())
        val steps = scheduler.setTimeSteps(payload.samplingSteps)
        val initialSigma = scheduler.initNoiseSigma.toFloat()
        val values = Array(1) { Array(4) { Array(height / 8) { FloatArray(width / 8) } } }
        for (channel in 0..3) for (y in 0 until height / 8) for (x in 0 until width / 8) {
            val radius = sqrt(-2.0 * ln(random.nextDouble()))
            values[0][channel][y][x] =
                (radius * cos(2.0 * Math.PI * random.nextDouble()) * initialSigma).toFloat()
        }
        var latents =
            LocalDiffusionTensor(OnnxTensor.createTensor(environment.get(), values), values, shape)
        try {
            for (index in steps.indices) {
                controller.checkActive()
                onProgress(LocalDiffusionStatus(index, steps.size))
                val doubledShape = longArrayOf(2, 4, (height / 8).toLong(), (width / 8).toLong())
                duplicate(
                    latents.tensor.floatBuffer.let { b -> FloatArray(b.remaining()).also(b::get) },
                    doubledShape,
                    environment
                ).use { duplicate ->
                    scheduler.scaleModelInput(duplicate, index).use { input ->
                        OnnxTensor.createTensor(
                            environment.get(),
                            IntBuffer.wrap(intArrayOf(steps[index])),
                            longArrayOf(1)
                        ).use { timestep ->
                            val map = mapOf(
                                LocalDiffusionContract.KEY_ENCODER_HIDDEN_STATES to embeddings,
                                LocalDiffusionContract.KEY_SAMPLE to input.tensor,
                                LocalDiffusionContract.KEY_TIME_STEP to timestep,
                            )
                            controller.run(checkNotNull(session).value, map).use { output ->
                                val dataset = output[0].value as Array3D<FloatArray>
                                val split = splitTensor(dataset, shape)
                                val noise = split.first
                                val textNoise = split.second
                                for (c in 0..3) for (y in 0 until height / 8) for (x in 0 until width / 8) {
                                    noise[0][c][y][x] += payload.cfgScale * (textNoise[0][c][y][x] - noise[0][c][y][x])
                                }
                                LocalDiffusionTensor(
                                    OnnxTensor.createTensor(
                                        environment.get(),
                                        noise
                                    ), noise, getSizes(noise)
                                ).use { prediction ->
                                    val next = scheduler.step(prediction, index, latents)
                                    latents.close()
                                    latents = next as LocalDiffusionTensor<Array3D<FloatArray>>
                                }
                            }
                        }
                    }
                }
            }
            controller.checkActive()
            onProgress(LocalDiffusionStatus(steps.size, steps.size))
            closeDenoiser()
            onStage(NativeGenerationStage.DECODING)
            val array =
                latents.tensor.floatBuffer.let { b -> FloatArray(b.remaining()).also(b::get) }
            multipleTensorsByFloat(array, 1f / 0.18215f, shape, environment).use { scaled ->
                return decoder.decode(
                    mapOf(LocalDiffusionContract.KEY_LATENT_SAMPLE to scaled.tensor),
                    width,
                    height
                )
            }
        } finally {
            latents.close()
        }
    }

    /**
     * Owner-thread disposal only; cancellation never invokes this while run is active.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        try {
            closeDenoiser()
        } finally {
            decoder.close()
        }
    }

    /**
     * Releases denoiser weights before the decoder loads, retaining only the final latent tensor.
     *
     * @author Dmitriy Moroz
     */
    private fun closeDenoiser() {
        val ownedSession = session
        session = null
        ownedSession?.close()
    }
}
