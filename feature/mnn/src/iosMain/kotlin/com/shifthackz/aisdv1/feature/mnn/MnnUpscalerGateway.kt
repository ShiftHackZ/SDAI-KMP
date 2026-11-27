package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.upscalerNativeRequest
import com.shifthackz.aisdv1.domain.gateway.UpscalerGateway
import kotlin.io.encoding.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Runs Apple super-resolution through the same serial, cancellation-drained MNN owner as diffusion.
 *
 * @param runtime Injected Apple runtime retaining native buffers until its attempt has drained.
 * @author Dmitriy Moroz
 */
internal class MnnUpscalerGateway(private val runtime: MnnRuntime) : UpscalerGateway {
    private val stage = MutableStateFlow(NativeGenerationStage.PREPARING)

    /**
     * Observes the active super-resolution lifecycle without admitting a new attempt.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStage() = stage.asStateFlow()

    /**
     * Encodes only complete native output; cancellation and native errors remain failures.
     *
     * @param image Prepared bounded source in Base64.
     * @param modelPath App-private MNN checkpoint.
     * @param width Source width in pixels.
     * @param height Source height in pixels.
     * @author Dmitriy Moroz
     */
    override suspend fun upscale(
        image: String,
        modelPath: String,
        width: Int,
        height: Int
    ): String {
        stage.value = NativeGenerationStage.PREPARING
        val result = runtime.generate(upscalerNativeRequest(image, modelPath, width, height)) {
            stage.value = NativeGenerationStage.RUNNING
        }
        stage.value = NativeGenerationStage.WRITING_RESULT
        return Base64.encode(result)
    }
}
