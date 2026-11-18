package com.shifthackz.aisdv1.presentation.gallery.di

import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.detail.createDefaultGalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryExportService
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.list.NoOpGalleryExportService
import com.shifthackz.aisdv1.presentation.gallery.list.NoOpGalleryPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailContentCache
import com.shifthackz.aisdv1.presentation.gallery.detail.BoundedGalleryDetailContentCache
import org.koin.dsl.module


/**
 * Creates a bounded detail-metadata cache per consumer and supplies singleton platform defaults
 * for archive export, destination actions and detail image/text operations.
 *
 * @author Dmitriy Moroz
 */
val galleryCoreModule = module {
    factory<GalleryDetailContentCache> { BoundedGalleryDetailContentCache(get()) }
    single<GalleryExportService> { NoOpGalleryExportService }
    single<GalleryPlatformActions> { NoOpGalleryPlatformActions }
    single<GalleryDetailPlatformActions> { createDefaultGalleryDetailPlatformActions() }
}
