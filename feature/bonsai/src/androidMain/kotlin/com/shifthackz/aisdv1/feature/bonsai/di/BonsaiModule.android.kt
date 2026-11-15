package com.shifthackz.aisdv1.feature.bonsai.di

import com.shifthackz.aisdv1.domain.feature.bonsai.BonsaiDiffusion
import com.shifthackz.aisdv1.feature.bonsai.BonsaiDiffusionImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Registers the Android host gateway facade without constructing or loading Bonsai JNI.
 *
 * @return Host bindings; the inference service composes its native adapter separately.
 * @author Dmitriy Moroz
 */
internal actual fun platformBonsaiModule() = module {
    singleOf(::BonsaiDiffusionImpl) bind BonsaiDiffusion::class
}
