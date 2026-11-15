package com.shifthackz.aisdv1.app.inference

import android.content.Context
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.bonsai.di.bonsaiInferenceModule
import com.shifthackz.aisdv1.feature.mediapipe.di.mediaPipeInferenceModule
import com.shifthackz.aisdv1.feature.onnx.di.onnxInferenceModule
import com.shifthackz.aisdv1.feature.sdxl.di.sdxlInferenceModule
import org.koin.dsl.koinApplication

/**
 * The only place that resolves per-job feature dependencies; never installed in the host graph.
 *
 * @param context Child application context.
 * @author Dmitriy Moroz
 */
internal class NativeInferenceEngineFactoryImpl(private val context: Context) :
    NativeInferenceEngineFactory {
    /**
     * Creates a child-only runtime and its independent dependency graph.
     * The caller owns the returned engine and must close it after native execution returns.
     *
     * @param request Immutable job inputs.
     * @author Dmitriy Moroz
     */
    override fun create(request: NativeGenerationRequest): NativeInferenceEngine {
        val graph = koinApplication {
            modules(
                when (request.engine) {
                    NativeGenerationEngine.ONNX -> onnxInferenceModule(request)
                    NativeGenerationEngine.MEDIA_PIPE -> mediaPipeInferenceModule(context)
                    NativeGenerationEngine.SDCPP -> sdxlInferenceModule()
                    NativeGenerationEngine.BONSAI -> bonsaiInferenceModule()
                }
            )
        }
        return try {
            val engine = graph.koin.get<NativeInferenceEngine>()
            OwnedNativeInferenceEngine(engine, graph)
        } catch (failure: Throwable) {
            graph.close(); throw failure
        }
    }
}
