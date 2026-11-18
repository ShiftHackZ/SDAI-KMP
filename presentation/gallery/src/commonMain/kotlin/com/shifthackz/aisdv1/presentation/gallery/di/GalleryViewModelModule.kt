package com.shifthackz.aisdv1.presentation.gallery.di

import com.shifthackz.aisdv1.core.navigation.router.GalleryDetailRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryRouter
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.gallery.detail.GalleryDetailViewModel
import com.shifthackz.aisdv1.presentation.gallery.list.GalleryViewModel
import org.koin.dsl.module


/**
 * Creates independent gallery-list and detail ViewModels with caller-owned routers.
 * Detail instances receive their own metadata cache and the platform actions supplied by the screen.
 *
 * @author Dmitriy Moroz
 */
val galleryViewModelModule = module {
    factory { (router: GalleryRouter) ->
        GalleryViewModel(
            memoryBudget = get(),
            galleryReads = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            getMediaStoreInfoUseCase = get(),
            backgroundWorkObserver = get(),
            preferenceManager = get(),
            deleteAllGalleryUseCase = get(),
            deleteGalleryItemsUseCase = get(),
            setGalleryItemsVisibilityUseCase = get(),
            setGalleryItemsLikedUseCase = get(),
            galleryExportService = get(),
            galleryRouter = router,
        )
    }
    factory { (itemId: Long, router: GalleryDetailRouter, platformActions: GalleryDetailPlatformActions) ->
        GalleryDetailViewModel(
            galleryReads = get(),
            contentCache = get(),
            diagnostics = get(),
            itemId = itemId,
            dispatchersProvider = get(),
            buildInfoProvider = get(),
            getGenerationResultUseCase = get(),
            getAllGalleryIdsUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            deleteGalleryItemUseCase = get(),
            toggleImageVisibilityUseCase = get(),
            toggleImageLikeUseCase = get(),
            generationFormUpdateEvent = get(),
            router = router,
            platformActions = platformActions,
        )
    }
}
