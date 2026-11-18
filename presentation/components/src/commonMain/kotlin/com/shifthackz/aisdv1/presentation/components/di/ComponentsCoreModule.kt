package com.shifthackz.aisdv1.presentation.components.di

import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.BoundedOriginalImageLoader
import com.shifthackz.aisdv1.presentation.components.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.core.NoOpGenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.image.ImageSaver
import com.shifthackz.aisdv1.presentation.components.image.ImageSharer
import com.shifthackz.aisdv1.presentation.components.image.createPlatformImageSaver
import com.shifthackz.aisdv1.presentation.components.image.createPlatformImageSharer
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryMemoryBudget
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageLoader
import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryImageDecoder
import com.shifthackz.aisdv1.presentation.components.widget.image.PlatformGalleryImageDecoder
import com.shifthackz.aisdv1.presentation.components.widget.image.BoundedGalleryImageLoader
import org.koin.dsl.module

/**
 * Shares bounded sampled/original image budgets, decoders and loaders across presentation modules.
 * Also owns the form-restoration channel, with platform defaults for
 * image save/share and generation outcome feedback that native bindings may replace.
 *
 * @author Dmitriy Moroz
 */
val componentsCoreModule = module {
    single {
        GalleryMemoryBudget(
            cacheEntries = 32, cacheBytes = 24L * 1024 * 1024, concurrentDecodes = 2,
            encodedChars = 32 * 1024 * 1024, maxDisplayEdge = 2048, detailEntries = 3,
            detailMetadataBytes = 2L * 1024 * 1024, metadataWindow = 120, metadataPrefetch = 30
        )
    }
    single<GalleryImageDecoder> { PlatformGalleryImageDecoder() }
    single { OriginalImageBudget(encodedChars = 32 * 1024 * 1024, maxPixels = 16_777_216L) }
    single<OriginalImageLoader> { BoundedOriginalImageLoader(get(), get()) }
    single<GalleryImageLoader> { BoundedGalleryImageLoader(get(), get(), get(), get()) }
    single<GenerationPlatformServices> { NoOpGenerationPlatformServices }
    single<ImageSaver> { createPlatformImageSaver() }
    single<ImageSharer> { createPlatformImageSharer() }
    single { GenerationFormUpdateEvent() }
}
