package com.shifthackz.aisdv1.feature.sdxl

import android.util.Base64
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.sdxl.runtime.StableDiffusionCppBackend
import com.shifthackz.aisdv1.feature.sdxl.runtime.StableDiffusionCppRequest
import com.shifthackz.aisdv1.feature.sdxl.runtime.StableDiffusionCppRuntime
import com.shifthackz.aisdv1.feature.sdxl.runtime.StableDiffusionCppSampler

/**
 * Child adapter retaining the existing backend, sampler and image conversion implementation.
 * The runtime shares one callback across model loading, sampling and VAE tile work, so its
 * counters do not identify a generation phase or a requested sampling-step total.
 *
 * @param runtime Exclusively owned native backend.
 * @author Dmitriy Moroz
 */
internal class StableDiffusionCppInferenceEngine(private val runtime: StableDiffusionCppRuntime) :
    NativeInferenceEngine {
    @Volatile
    private var hasStarted = false

    /**
     * Runs the opaque native lifecycle without inferring phases from its mixed progress counters.
     *
     * @param request Captured model and sampler inputs.
     * @param onStage PREPARING before bridge loading, then RUNNING through the opaque runtime call.
     * @param onProgress Nonblocking opaque work counts, never labeled as denoising steps.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray {
        val p = request.payload
        onStage(NativeGenerationStage.PREPARING)
        StableDiffusionCppNativeBridge.ensureLoaded()
        hasStarted = true
        onStage(NativeGenerationStage.RUNNING)
        val result = runtime.generate(
            StableDiffusionCppRequest(
                modelPath = request.modelPath,
                prompt = p.prompt,
                negativePrompt = p.negativePrompt,
                width = p.width,
                height = p.height,
                samplingSteps = p.samplingSteps,
                cfgScale = p.cfgScale,
                seed = p.seed.toLongOrNull() ?: -1,
                batchCount = 1,
                backend = StableDiffusionCppBackend.valueOf(p.sdxlBackend.name),
                sampler = StableDiffusionCppSampler.parse(p.sampler),
            ),
        ) { onProgress(LocalDiffusionStatus(it.current, it.total)) }
        return Base64.decode(result.base64Image, Base64.NO_WRAP)
    }

    /**
     * JNI interrupt only sets its cancellation flag and never frees an active native context.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() {
        if (hasStarted) StableDiffusionCppNativeBridge.interrupt()
    }

    /**
     * Process teardown releases cached native contexts; no unsafe concurrent close is performed.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = Unit
}
