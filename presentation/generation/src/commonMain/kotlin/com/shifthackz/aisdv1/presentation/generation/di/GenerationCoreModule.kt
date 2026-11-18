package com.shifthackz.aisdv1.presentation.generation.di

import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.generation.img2img.NoOpImageToImagePlatformActions
import org.koin.dsl.module


/**
 * Provides an unsupported picker singleton for hosts without a composition-bound native image picker.
 *
 * @author Dmitriy Moroz
 */
val generationCoreModule = module {
    single<ImageToImagePlatformActions> { NoOpImageToImagePlatformActions }
}
