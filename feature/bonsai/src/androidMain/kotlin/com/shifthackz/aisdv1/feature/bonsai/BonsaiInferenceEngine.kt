package com.shifthackz.aisdv1.feature.bonsai

import android.os.Process
import android.util.Base64
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine

/**
 * Child-only Bonsai JNI adapter, executed on the service's serial inference executor.
 * Native contexts remain owned by the synchronous call and are released when it returns.
 *
 * @author Dmitriy Moroz
 */
internal class BonsaiInferenceEngine : NativeInferenceEngine {
    @Volatile
    private var hasLoadedBridge = false

    /**
     * Runs a single image with captured backend and model inputs, returning the native PNG bytes.
     * Loading and text conditioning remain PREPARING because JNI has no phase callbacks for them.
     * Its progress callback starts at denoising and counts completed sampling steps only.
     *
     * @param request Immutable inputs for this child attempt.
     * @param onProgress Completed sampling steps supplied by the native denoising loop.
     * @param onStage PREPARING, then DENOISING on the first native callback and DECODING after its final step.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit,
    ): ByteArray {
        require(request.engine == NativeGenerationEngine.BONSAI)
        val payload = request.payload
        require(payload.batchCount == 1)
        onStage(NativeGenerationStage.PREPARING)
        AndroidBonsaiRequestValidator.validate(payload, request.modelPath)
        BonsaiNativeBridge.ensureLoaded()
        hasLoadedBridge = true
        val layout = AndroidBonsaiModelLayout.resolve(request.modelPath)
        val image = withBackgroundThreadPriority {
            BonsaiNativeBridge.generate(
                layout = layout,
                prompt = payload.prompt,
                negativePrompt = payload.negativePrompt,
                samplingSteps = payload.samplingSteps,
                cfgScale = payload.cfgScale,
                width = payload.width,
                height = payload.height,
                seed = payload.seed,
                batchCount = 1,
                allowNsfw = payload.nsfw,
                backend = payload.bonsaiBackend.key,
                callback = object : BonsaiNativeBridge.ProgressCallback {
                    /**
                     * Relays the denoising loop's counters and advances stages at native boundaries.
                     *
                     * @param current Completed sampling steps; zero marks the denoising entry.
                     * @param total Requested sampling-step count.
                     * @author Dmitriy Moroz
                     */
                    override fun onProgress(current: Int, total: Int) {
                        if (current == 0) onStage(NativeGenerationStage.DENOISING)
                        onProgress(LocalDiffusionStatus(current, total))
                        if (total > 0 && current == total) onStage(NativeGenerationStage.DECODING)
                    }
                },
            )
        }
        return Base64.decode(image, Base64.NO_WRAP)
    }

    /**
     * Signals the JNI cancellation flag without releasing resources concurrently with generation.
     * The service owns bounded termination when native loading or execution does not cooperate.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() {
        if (hasLoadedBridge) BonsaiNativeBridge.interrupt()
    }

    /**
     * Native scoped contexts have already been released; the gateway then tears down this child.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = Unit

    /**
     * Lowers this inference thread's priority and restores it after native execution or failure.
     *
     * @param block Synchronous native operation owned by this inference thread.
     * @return Result returned by the native operation.
     * @author Dmitriy Moroz
     */
    private inline fun <T> withBackgroundThreadPriority(block: () -> T): T {
        val threadId = Process.myTid()
        val previousPriority = runCatching { Process.getThreadPriority(threadId) }.getOrNull()
        runCatching { Process.setThreadPriority(threadId, Process.THREAD_PRIORITY_BACKGROUND) }
        return try {
            block()
        } finally {
            if (previousPriority != null) {
                runCatching { Process.setThreadPriority(threadId, previousPriority) }
            }
        }
    }
}
