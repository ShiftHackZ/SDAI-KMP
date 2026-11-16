package com.shifthackz.aisdv1.feature.onnx.di

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.onnx.LocalDiffusionImpl
import com.shifthackz.aisdv1.feature.onnx.ai.scheduler.EulerAncestralDiscreteLocalDiffusionScheduler
import com.shifthackz.aisdv1.feature.onnx.ai.scheduler.LocalDiffusionScheduler
import com.shifthackz.aisdv1.feature.onnx.ai.tokenizer.EnglishTextTokenizer
import com.shifthackz.aisdv1.feature.onnx.ai.tokenizer.LocalDiffusionTextTokenizer
import com.shifthackz.aisdv1.feature.onnx.ai.unet.UNet
import com.shifthackz.aisdv1.feature.onnx.ai.unet.UNetImpl
import com.shifthackz.aisdv1.feature.onnx.ai.vae.VaeDecoder
import com.shifthackz.aisdv1.feature.onnx.ai.vae.VaeDecoderImpl
import com.shifthackz.aisdv1.feature.onnx.entity.LocalDiffusionConfig
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunController
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRunControllerImpl
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxRuntimeConfiguration
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSessionFactory
import com.shifthackz.aisdv1.feature.onnx.environment.OnnxSessionFactoryImpl
import com.shifthackz.aisdv1.feature.onnx.environment.OrtEnvironmentProvider
import com.shifthackz.aisdv1.feature.onnx.environment.OrtEnvironmentProviderImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Creates an independent child-only graph for one immutable ONNX job.
 *
 * @param request Captured model and execution settings.
 * @param isDiagnosticsEnabled Enables native provider assignment logs in development builds.
 * @author Dmitriy Moroz
 */
fun onnxInferenceModule(request: NativeGenerationRequest, isDiagnosticsEnabled: Boolean): Module =
    module {
        single {
            OnnxRuntimeConfiguration(
                request.modelPath,
                request.isNnapiEnabled,
                isDiagnosticsEnabled
            )
        }
        singleOf(::OrtEnvironmentProviderImpl) bind OrtEnvironmentProvider::class
        singleOf(::OnnxRunControllerImpl) bind OnnxRunController::class
        singleOf(::OnnxSessionFactoryImpl) bind OnnxSessionFactory::class
        singleOf(::EnglishTextTokenizer) bind LocalDiffusionTextTokenizer::class
        singleOf(::VaeDecoderImpl) bind VaeDecoder::class
        single<LocalDiffusionScheduler> {
            EulerAncestralDiscreteLocalDiffusionScheduler(
                get(),
                LocalDiffusionConfig()
            )
        }
        singleOf(::UNetImpl) bind UNet::class
        singleOf(::LocalDiffusionImpl) bind NativeInferenceEngine::class
    }
