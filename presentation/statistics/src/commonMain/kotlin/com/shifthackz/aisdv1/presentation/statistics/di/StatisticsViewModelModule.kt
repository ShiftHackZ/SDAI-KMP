package com.shifthackz.aisdv1.presentation.statistics.di

import com.shifthackz.aisdv1.core.navigation.router.NetworkUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.presentation.statistics.networkusage.NetworkUsageViewModel
import com.shifthackz.aisdv1.presentation.statistics.storageusage.StorageUsageViewModel
import com.shifthackz.aisdv1.presentation.statistics.storageusage.platform.StorageUsagePlatformActions
import org.koin.dsl.module

/**
 * Creates separate storage and network usage state holders with caller-owned routers.
 * Storage shares model/gallery accounting and invalidation services while receiving platform cleanup actions.
 *
 * @author Dmitriy Moroz
 */
val statisticsViewModelModule = module {
    factory { (router: StorageUsageRouter, platformActions: StorageUsagePlatformActions) ->
        StorageUsageViewModel(
            galleryReads = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            deleteAllGalleryUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalSdxlModelsUseCase = get(),
            getLocalCoreMlModelsUseCase = get(),
            getLocalMnnModelsUseCase = get(),
            getLocalBonsaiModelsUseCase = get(),
            deleteModelUseCase = get(),
            storageUsageObserver = get(),
            buildInfoProvider = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: NetworkUsageRouter) ->
        NetworkUsageViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            observeNetworkUsageUseCase = get(),
            resetNetworkUsageUseCase = get(),
            router = router,
        )
    }
}
