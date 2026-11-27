package com.shifthackz.aisdv1.feature.sdxl

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
 * Child-only bounded RGB adapter for the bundled CPU ESRGAN runtime.
 * Native model and activation buffers are released before returning the encoded PNG.
 *
 * @author Dmitriy Moroz
 */
internal class EsrganInferenceEngine : NativeInferenceEngine {
    @Volatile
    private var hasStarted = false

    /**
     * Validates source dimensions before decoding, then enlarges through the dedicated ESRGAN context.
     *
     * @param request Captured checkpoint, source bytes and expected enlarged output dimensions.
     * @param onProgress Reports image-stage completion; native tile durations are indeterminate.
     * @param onStage Publishes model preparation, execution and image encoding stages.
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
        require(bounds.outWidth * 4 == request.payload.width && bounds.outHeight * 4 == request.payload.height)
        val bitmap = requireNotNull(BitmapFactory.decodeByteArray(encoded, 0, encoded.size))
        val rgb = try {
            val colors = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(colors, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            ByteArray(colors.size * 3).also { pixels ->
                colors.forEachIndexed { index, color ->
                    pixels[index * 3] = (color shr 16).toByte()
                    pixels[index * 3 + 1] = (color shr 8).toByte()
                    pixels[index * 3 + 2] = color.toByte()
                }
            }
        } finally {
            bitmap.recycle()
        }
        currentCoroutineContext().ensureActive()
        StableDiffusionCppNativeBridge.ensureLoaded()
        hasStarted = true
        onStage(NativeGenerationStage.RUNNING)
        val shape = IntArray(3)
        val enlarged = StableDiffusionCppNativeBridge.upscale(
            request.modelPath, rgb,
            bounds.outWidth, bounds.outHeight, shape
        )
        currentCoroutineContext().ensureActive()
        require(shape.contentEquals(intArrayOf(request.payload.width, request.payload.height, 3)))
        onStage(NativeGenerationStage.WRITING_RESULT)
        val colors = IntArray(shape[0] * shape[1]) { index ->
            (0xff shl 24) or ((enlarged[index * 3].toInt() and 255) shl 16) or
                    ((enlarged[index * 3 + 1].toInt() and 255) shl 8) or (enlarged[index * 3 + 2].toInt() and 255)
        }
        val output = Bitmap.createBitmap(colors, shape[0], shape[1], Bitmap.Config.ARGB_8888)
        return try {
            ByteArrayOutputStream().use { stream ->
                check(output.compress(Bitmap.CompressFormat.PNG, 100, stream))
                onProgress(LocalDiffusionStatus(1, 1))
                stream.toByteArray()
            }
        } finally {
            output.recycle()
        }
    }

    /**
     * Signals cooperative interruption; the IPC owner enforces child teardown when necessary.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() {
        if (hasStarted) StableDiffusionCppNativeBridge.interrupt()
    }

    /**
     * Native resources are scoped to the call and process; no concurrent context destruction occurs.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = Unit
}
