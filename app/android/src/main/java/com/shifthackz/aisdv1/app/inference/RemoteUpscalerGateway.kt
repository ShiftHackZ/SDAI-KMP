package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.upscalerNativeRequest
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.UpscalerGateway

/**
 * Sends local super-resolution to the isolated native process using file-backed image exchange.
 *
 * @param gateway Shared admission and cancellation owner for native inference attempts.
 * @author Dmitriy Moroz
 */
internal class RemoteUpscalerGateway(private val gateway: NativeGenerationGateway) :
    UpscalerGateway {
    override fun observeStage() = gateway.observeStage(NativeGenerationEngine.UPSCALER)

    /**
     * Captures the expected enlarged dimensions and delegates native execution to the child process.
     *
     * @param image Base64 encoded source image.
     * @param modelPath Dedicated app-owned checkpoint for the selected native upscaler.
     * @param width Source width in pixels.
     * @param height Source height in pixels.
     * @author Dmitriy Moroz
     */
    override suspend fun upscale(
        image: String,
        modelPath: String,
        width: Int,
        height: Int
    ): String =
        gateway.generate(upscalerNativeRequest(image, modelPath, width, height))
}
