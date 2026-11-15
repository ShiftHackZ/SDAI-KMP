package com.shifthackz.aisdv1.feature.bonsai

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.bonsai.BonsaiDiffusion
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway

/**
 * Android host facade that submits Bonsai work without loading its native library.
 * Native execution, progress and cancellation belong to the shared inference gateway.
 *
 * @param gateway Owns serialized attempts and their child-process lifetime.
 * @author Dmitriy Moroz
 */
internal class BonsaiDiffusionImpl(private val gateway: NativeGenerationGateway) : BonsaiDiffusion {
    /**
     * Captures one image request; the shared use case owns sequential batching.
     *
     * @param payload Generation parameters, including the selected Bonsai backend and NSFW setting.
     * @param modelPath Selected downloaded or custom model directory.
     * @return Complete validated PNG encoded as Base64 for repository persistence.
     * @author Dmitriy Moroz
     */
    override suspend fun process(payload: TextToImagePayload, modelPath: String): String =
        gateway.generate(NativeGenerationRequest(NativeGenerationEngine.BONSAI, payload, modelPath))

    /**
     * Cancels the current owned native attempt without initializing Bonsai JNI in the host.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interrupt() {
        gateway.cancelActive()
    }

    /**
     * Observes child progress without constructing a native runtime.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus() = gateway.observeStatus(NativeGenerationEngine.BONSAI)
}
