package com.shifthackz.aisdv1.core.imageprocessing.di

import com.shifthackz.aisdv1.core.imageprocessing.ImageDownscaler
import com.shifthackz.aisdv1.core.imageprocessing.PlatformImageDownscaler
import org.koin.dsl.module

/**
 * Binds encoded-image downscaling for Android and iOS without exposing native image types.
 *
 * @author Dmitriy Moroz
 */
val imageDownscalingModule = module {
    factory<ImageDownscaler> { PlatformImageDownscaler() }
}
