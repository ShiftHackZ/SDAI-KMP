package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.sdxl.StableDiffusionCpp
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway

/**
 * Android host proxy preserving the common SDCPP repository contract without loading JNI.
 *
 * @param gateway Sole host inference owner.
 * @author Dmitriy Moroz
 */
internal class RemoteStableDiffusionCpp(private val gateway: NativeGenerationGateway) :
    StableDiffusionCpp {
    /**
     * Submits captured SDCPP inputs to the host IPC gateway without loading JNI in the host process.
     *
     * @param payload Captured generation parameters.
     * @param modelPath Resolved app-readable model path.
     * @author Dmitriy Moroz
     */
    override suspend fun process(payload: TextToImagePayload, modelPath: String): String =
        gateway.generate(NativeGenerationRequest(NativeGenerationEngine.SDCPP, payload, modelPath))

    /**
     * Cancels the owned local attempt even if provider selection has changed.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interrupt() {
        gateway.cancelActive()
    }

    /**
     * Host-side progress without native runtime resolution.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus() = gateway.observeStatus(NativeGenerationEngine.SDCPP)
}
