@file:Suppress("UNCHECKED_CAST")

package com.shifthackz.aisdv1.feature.onnx.ai.vae

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtSession
import ai.onnxruntime.providers.NNAPIFlags
import android.graphics.Bitmap
import android.graphics.Color
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract
import com.shifthackz.aisdv1.feature.onnx.entity.Array3D
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunController
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRuntimeConfiguration
import com.shifthackz.aisdv1.feature.onnx.environment.OrtEnvironmentProvider
import java.util.EnumSet
import kotlin.math.roundToInt

/**
 * VAE session whose options, outputs and bitmap remain owned until decode completes.
 *
 * @param environment Child native environment.
 * @param configuration Submitted model/provider snapshot.
 * @param controller Cooperative run cancellation.
 * @author Dmitriy Moroz
 */
internal class VaeDecoderImpl(
    private val environment: OrtEnvironmentProvider,
    private val configuration: OnnxRuntimeConfiguration,
    private val controller: OnnxRunController,
) : VaeDecoder {
    private var session: OrtSession? = null

    /**
     * Decodes borrowed latent tensors into a caller-owned bitmap on the session owner thread.
     * Inputs remain alive until decoding returns; the caller handles bitmap disposal.
     *
     * @param input Borrowed latent tensors.
     * @param width Validated output width.
     * @param height Validated output height.
     * @return Owned bitmap; caller compresses and recycles it.
     * @author Dmitriy Moroz
     */
    override fun decode(input: Map<String, OnnxTensor>, width: Int, height: Int): Bitmap {
        controller.checkActive()
        OrtSession.SessionOptions().use { options ->
            options.addConfigEntry(
                LocalDiffusionContract.ORT_KEY_MODEL_FORMAT,
                LocalDiffusionContract.ORT
            )
            if (configuration.isNnapiEnabled) options.addNnapi(EnumSet.of(NNAPIFlags.CPU_DISABLED))
            session = environment.get().createSession(
                "${configuration.modelDirectory}/${LocalDiffusionContract.VAE_MODEL}",
                options
            )
        }
        controller.run(checkNotNull(session), input).use { result ->
            val output = result[0].value as Array3D<FloatArray>
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            try {
                for (y in 0 until height) {
                    controller.checkActive()
                    for (x in 0 until width) {
                        val r =
                            ((output[0][0][y][x] / 2 + 0.5).coerceIn(0.0, 1.0) * 255f).roundToInt()
                        val g =
                            ((output[0][1][y][x] / 2 + 0.5).coerceIn(0.0, 1.0) * 255f).roundToInt()
                        val b =
                            ((output[0][2][y][x] / 2 + 0.5).coerceIn(0.0, 1.0) * 255f).roundToInt()
                        bitmap.setPixel(x, y, Color.rgb(r, g, b))
                    }
                }
                return bitmap
            } catch (failure: Throwable) {
                bitmap.recycle(); throw failure
            }
        }
    }

    /**
     * Called after decode/run exits, including failure paths.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        try {
            session?.close()
        } finally {
            session = null
        }
    }
}
