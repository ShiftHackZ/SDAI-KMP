package com.shifthackz.aisdv1.presentation.gallery.di

import com.shifthackz.aisdv1.presentation.gallery.detail.AndroidGalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.list.AndroidGalleryPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryExportService
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryExporter
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryPlatformActions
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Supplies Android factories for streamed gallery archives, destination/ZIP actions and detail media/clipboard actions.
 *
 * @author Dmitriy Moroz
 */
val galleryUiUtilsModule = module {
    factoryOf(::GalleryExporter) bind GalleryExportService::class
    factory {
        AndroidGalleryPlatformActions(
            androidContext(),
            get()
        )
    } bind GalleryPlatformActions::class
    factory {
        AndroidGalleryDetailPlatformActions(
            context = androidContext(),
            fileProviderDescriptor = get(),
            dispatchersProvider = get(),
            originals = get(),
            imageSaver = get(),
        )
    } bind GalleryDetailPlatformActions::class
}
