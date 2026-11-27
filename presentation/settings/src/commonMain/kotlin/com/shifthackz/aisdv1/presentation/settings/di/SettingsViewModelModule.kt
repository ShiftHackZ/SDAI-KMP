package com.shifthackz.aisdv1.presentation.settings.di

import com.shifthackz.aisdv1.core.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.presentation.settings.SettingsViewModel
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions
import org.koin.dsl.module

/**
 * Creates settings state holders with domain operations, the shared storage observer/developer accessor,
 * and the router/platform actions supplied by the current settings screen.
 *
 * @author Dmitriy Moroz
 */
val settingsViewModelModule = module {
    factory { (router: SettingsRouter, platformActions: SettingsPlatformActions) ->
        SettingsViewModel(
            galleryReads = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            getStableDiffusionModelsUseCase = get(),
            observeStabilityAiCreditsUseCase = get(),
            selectStableDiffusionModelUseCase = get(),
            clearAppCacheUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalSdxlModelsUseCase = get(),
            getLocalCoreMlModelsUseCase = get(),
            getLocalBonsaiModelsUseCase = get(),
            getLocalMnnModelsUseCase = get(),
            getCachedLocalModelCatalogUseCase = get(),
            observeNetworkUsageUseCase = get(),
            storageUsageObserver = get(),
            preferenceManager = get(),
            debugMenuAccessor = get(),
            buildInfoProvider = get(),
            linksProvider = get(),
            router = router,
            platformActions = platformActions,
        )
    }
}
