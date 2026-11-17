package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.core.common.log.infoLog
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine

/**
 * Executes one validated SD 1.5 request on the inference service's serial native owner.
 *
 * @param bridge Injected JNI boundary; never constructed or loaded in the application host.
 *
 * @author Dmitriy Moroz
 */
internal class MnnInferenceEngine(private val bridge: MnnRuntime) : NativeInferenceEngine {
    /**
     * Rejects unsupported inputs before the first native call and reports opaque percentage work.
     *
     * @param request Captured model/backend/profile and resolved seed.
     * @param onProgress Overall percentage counters while RUNNING, never sampling steps.
     * @param onStage Lifecycle independent of preferences changing in the host.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit, onStage: (NativeGenerationStage) -> Unit
    ): ByteArray {
        val payload = request.payload
        require(payload.width == 512 && payload.height == 512 && payload.cfgScale == 7.5f)
        require(payload.samplingSteps in 1..50 && payload.seed.toLongOrNull() in 0..Int.MAX_VALUE.toLong())
        require(payload.batchCount == 1 && payload.negativePrompt.isEmpty() && payload.sampler == "PNDM")
        require(payload.mnnProfile == "sd15-512-general-int8-v1")
        onStage(NativeGenerationStage.PREPARING)
        onStage(NativeGenerationStage.RUNNING)
        val started = System.nanoTime()
        infoLog(
            "MNN",
            "requestedBackend=${payload.mnnBackend.name}; profile=${payload.mnnProfile}; freshRuntime=true; executionPolicy=sequential-cfg-batch1; stageLoading=true"
        )
        return bridge.generate(request) { onProgress(LocalDiffusionStatus(it, 100)) }.also {
            infoLog(
                "MNN",
                "runtimeBackend=${payload.mnnBackend.name}; elapsedMs=${(System.nanoTime() - started) / 1_000_000}; operatorPlacement=unmeasured; cpuFallback=possible"
            )
        }
    }

    /**
     * Requests cooperative interruption; native destruction remains on the generation thread.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = bridge.interrupt()

    /**
     * The synchronous bridge already releases the request runtime before returning.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = Unit
}
