package com.shifthackz.aisdv1.feature.sdxl.di

import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.sdxl.AndroidStableDiffusionCppRuntime
import com.shifthackz.aisdv1.feature.sdxl.StableDiffusionCppInferenceEngine
import com.shifthackz.aisdv1.feature.sdxl.runtime.StableDiffusionCppRuntime
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Child-only SDCPP composition, separate from the host gateway binding.
 *
 * @author Dmitriy Moroz
 */
fun sdxlInferenceModule(): Module = module {
    singleOf(::AndroidStableDiffusionCppRuntime) bind StableDiffusionCppRuntime::class
    singleOf(::StableDiffusionCppInferenceEngine) bind NativeInferenceEngine::class
}
