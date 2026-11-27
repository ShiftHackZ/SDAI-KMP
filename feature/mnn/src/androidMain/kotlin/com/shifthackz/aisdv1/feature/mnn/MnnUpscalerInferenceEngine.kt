package com.shifthackz.aisdv1.feature.mnn

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Adapts bounded encoded images to the shared MNN RGB tile runner in the inference child.
 *
 * @param bridge Native attempt whose handle is released only after execution drains.
 * @author Dmitriy Moroz
 */
internal class MnnUpscalerInferenceEngine(private val bridge: MnnUpscalerNativeBridge) :
    NativeInferenceEngine {
    /**
     * Validates prepared dimensions, runs tiled inference and encodes a complete PNG.
     *
     * @param request Prepared source, owned MNN model and expected output dimensions.
     * @param onProgress Completed tile percentage.
     * @param onStage Current native lifecycle stage.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest, onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray {
        onStage(NativeGenerationStage.PREPARING)
        val encoded = Base64.decode(requireNotNull(request.upscaleImage), Base64.DEFAULT)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(encoded, 0, encoded.size, bounds)
        require(bounds.outWidth in 1..1024 && bounds.outHeight in 1..1024)
        require(request.payload.width == bounds.outWidth * 4 && request.payload.height == bounds.outHeight * 4)
        val bitmap = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
        val rgb = try {
            val colors = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(colors, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            ByteArray(colors.size * 3).also { pixels ->
                colors.forEachIndexed { i, color ->
                    pixels[i * 3] = (color shr 16).toByte()
                    pixels[i * 3 + 1] = (color shr 8).toByte()
                    pixels[i * 3 + 2] = color.toByte()
                }
            }
        } finally {
            bitmap.recycle()
        }
        currentCoroutineContext().ensureActive()
        onStage(NativeGenerationStage.RUNNING)
        val output = bridge.run(request.modelPath, rgb, bounds.outWidth, bounds.outHeight) {
            onProgress(LocalDiffusionStatus(it, 100))
        }
        currentCoroutineContext().ensureActive()
        onStage(NativeGenerationStage.WRITING_RESULT)
        val width = request.payload.width
        val height = request.payload.height
        require(output.size == width * height * 3)
        val colors = IntArray(width * height) { i ->
            (0xff shl 24) or ((output[i * 3].toInt() and 255) shl 16) or
                    ((output[i * 3 + 1].toInt() and 255) shl 8) or (output[i * 3 + 2].toInt() and 255)
        }
        val result = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888)
        return try {
            ByteArrayOutputStream().use {
                check(result.compress(Bitmap.CompressFormat.PNG, 100, it))
                it.toByteArray()
            }
        } finally {
            result.recycle()
        }
    }

    /**
     * Signals cancellation without concurrently destroying native memory.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = bridge.interrupt()

    /**
     * Releases the native handle after its call has returned.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = bridge.close()
}
