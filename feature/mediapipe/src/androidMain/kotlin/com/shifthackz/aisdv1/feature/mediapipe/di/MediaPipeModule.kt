package com.shifthackz.aisdv1.feature.mediapipe.di

import android.content.Context
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.mediapipe.MediaPipeImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Registers only the flavor's child runtime; constructor injection stays inside the feature.
 *
 * @param context Child application context.
 * @author Dmitriy Moroz
 */
fun mediaPipeInferenceModule(context: Context): Module = module {
    single<Context> { context }
    singleOf(::MediaPipeImpl) bind NativeInferenceEngine::class
}
