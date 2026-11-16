@file:Suppress("UNCHECKED_CAST")

package com.shifthackz.aisdv1.feature.onnx.ai.vae

import ai.onnxruntime.OnnxTensor
import android.graphics.Bitmap
import android.graphics.Color
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionContract
import com.shifthackz.aisdv1.feature.onnx.entity.Array3D
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunController
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSession
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSessionFactory
import kotlin.math.roundToInt

/**
 * VAE session whose options, outputs and bitmap remain owned until decode completes.
 *
 * @param sessionFactory Compatible decoder provider settings and ownership of session options.
 * @param controller Cooperative run cancellation.
 * @author Dmitriy Moroz
 */
internal class VaeDecoderImpl(
    private val sessionFactory: OnnxSessionFactory,
    private val controller: OnnxRunController,
) : VaeDecoder {
    private var session: OnnxSession? = null

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
        session = sessionFactory.create(
            LocalDiffusionContract.VAE_MODEL,
            isAccelerationAllowed = true,
        )
        controller.run(checkNotNull(session).value, input).use { result ->
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
