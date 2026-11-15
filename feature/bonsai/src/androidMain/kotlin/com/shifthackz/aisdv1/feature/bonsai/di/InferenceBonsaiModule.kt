package com.shifthackz.aisdv1.feature.bonsai.di

import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.bonsai.BonsaiInferenceEngine
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Registers the Bonsai JNI adapter only in an inference child's per-attempt graph.
 * Host composition uses [platformBonsaiModule] and never resolves this native engine.
 *
 * @author Dmitriy Moroz
 */
fun bonsaiInferenceModule(): Module = module {
    singleOf(::BonsaiInferenceEngine) bind NativeInferenceEngine::class
}
