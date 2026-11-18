package com.shifthackz.aisdv1.presentation.configuration.di

import com.shifthackz.aisdv1.presentation.configuration.modal.download.DownloadDialogViewModel
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.presentation.configuration.loader.ConfigurationLoaderViewModel
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupViewModel
import org.koin.dsl.module

/**
 * Creates startup loaders, model-mirror selectors and provider-setup ViewModels.
 * Setup receives domain connection/catalog operations and the shared storage-invalidation observer;
 * launch source, model identity and routers are supplied by the owning route.
 *
 * @author Dmitriy Moroz
 */
val configurationViewModelModule = module {
    factory { (router: ConfigurationLoaderRouter) ->
        ConfigurationLoaderViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            dataPreLoaderUseCase = get(),
            getConfigurationUseCase = get(),
            router = router,
        )
    }
    factory { (modelId: String) ->
        DownloadDialogViewModel(
            diagnostics = get(),
            modelId = modelId,
            getLocalModelUseCase = get(),
            dispatchersProvider = get(),
        )
    }
    factory { (launchSource: LaunchSource, router: ServerSetupRouter) ->
        ServerSetupViewModel(
            diagnostics = get(),
            launchSource = launchSource,
            dispatchersProvider = get(),
            buildInfoProvider = get(),
            getConfigurationUseCase = get(),
            getCachedLocalModelCatalogUseCase = get(),
            refreshLocalModelCatalogUseCase = get(),
            retryAuthorizationStorageUseCase = get(),
            resetAuthorizationCredentialsUseCase = get(),
            fetchHuggingFaceModelsUseCase = get(),
            urlValidator = get(),
            stringValidator = get(),
            filePathValidator = get(),
            connectToA1111UseCase = get(),
            connectToSwarmUiUseCase = get(),
            connectToHordeUseCase = get(),
            connectToHuggingFaceUseCase = get(),
            connectToLocalDiffusionUseCase = get(),
            connectToMediaPipeUseCase = get(),
            connectToSdxlUseCase = get(),
            connectToCoreMlUseCase = get(),
            connectToBonsaiUseCase = get(),
            connectToMnnUseCase = get(),
            connectToOpenAiUseCase = get(),
            connectToOpenAiCompatibleUseCase = get(),
            connectToEasyDiffusionUseCase = get(),
            openAiOAuthGateway = get(),
            setServerConfigurationUseCase = get(),
            connectToStabilityAiUseCase = get(),
            connectToFalAiUseCase = get(),
            connectToArliAiUseCase = get(),
            downloadModelUseCase = get(),
            deleteModelUseCase = get(),
            downloadGuard = get(),
            storageUsageObserver = get(),
            linksProvider = get(),
            preferenceManager = get(),
            router = router,
        )
    }
}
