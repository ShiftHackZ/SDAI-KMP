package com.shifthackz.aisdv1.feature.sdxl.di

import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.sdxl.EsrganInferenceEngine
import com.shifthackz.aisdv1.feature.sdxl.AndroidStableDiffusionCppRuntime
import com.shifthackz.aisdv1.feature.sdxl.StableDiffusionCppInferenceEngine
import com.shifthackz.aisdv1.feature.sdxl.runtime.StableDiffusionCppRuntime
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Creates exactly one child-owned generation or super-resolution adapter.
 *
 * @param isUpscaler Whether this attempt requires the independent ESRGAN context.
 *
 * @author Dmitriy Moroz
 */
fun sdxlInferenceModule(isUpscaler: Boolean = false): Module = module {
    if (isUpscaler) singleOf(::EsrganInferenceEngine) bind NativeInferenceEngine::class
    else {
        singleOf(::AndroidStableDiffusionCppRuntime) bind StableDiffusionCppRuntime::class
        singleOf(::StableDiffusionCppInferenceEngine) bind NativeInferenceEngine::class
    }
}
