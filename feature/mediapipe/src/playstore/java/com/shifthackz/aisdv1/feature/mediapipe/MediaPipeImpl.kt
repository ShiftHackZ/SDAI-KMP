package com.shifthackz.aisdv1.feature.mediapipe

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapExtractor
import com.google.mediapipe.tasks.vision.imagegenerator.ImageGenerator
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import java.io.ByteArrayOutputStream

/**
 * Child-only owner of the GPU generator; create, generate and close run on the same executor.
 *
 * @param context Application context needed by the MediaPipe runtime.
 * @author Dmitriy Moroz
 */
internal class MediaPipeImpl(private val context: Context) : NativeInferenceEngine {
    private var generator: ImageGenerator? = null

    /**
     * Generates one image with the GPU runtime on its creating thread and returns PNG bytes.
     * The generator and output image are closed before returning or propagating failure.
     *
     * @param request Captured model directory and generation parameters.
     * @param onStage Sanitized lifecycle-stage callback.
     * @param onProgress Initial and completed step counts; intermediate progress is unavailable.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray {
        try {
            onStage(NativeGenerationStage.PREPARING)
            onProgress(LocalDiffusionStatus(0, request.payload.samplingSteps))
            val options = ImageGenerator.ImageGeneratorOptions.builder()
                .setImageGeneratorModelDirectory(request.modelPath).build()
            val active = ImageGenerator.createFromOptions(context, options)
            generator = active
            onStage(NativeGenerationStage.DENOISING)
            val result = active.generate(
                request.payload.prompt,
                request.payload.samplingSteps,
                request.payload.seed.toIntOrNull() ?: 0
            )
            onStage(NativeGenerationStage.DECODING)
            val image = result.generatedImage()
            try {
                val bitmap = BitmapExtractor.extract(image)
                val bytes = ByteArrayOutputStream().use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
                    output.toByteArray()
                }
                onProgress(
                    LocalDiffusionStatus(
                        request.payload.samplingSteps,
                        request.payload.samplingSteps
                    )
                )
                return bytes
            } finally {
                image.close()
            }
        } finally {
            close()
        }
    }

    /**
     * MediaPipe has no safe cooperative cancellation here; the service owns bounded process exit.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = Unit

    /**
     * Invoked on the creating thread only, after native generation returns.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        val active = generator; generator = null; active?.close()
    }
}
