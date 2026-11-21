package com.shifthackz.aisdv1.presentation.components.di

import com.shifthackz.aisdv1.presentation.components.modal.history.InputHistoryViewModel
import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver
import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserverImpl
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeViewModel
import com.shifthackz.aisdv1.presentation.components.widget.connectivity.ConnectivityViewModel
import com.shifthackz.aisdv1.presentation.components.widget.engine.EngineSelectionViewModel
import com.shifthackz.aisdv1.presentation.components.widget.work.BackgroundWorkViewModel
import org.koin.dsl.module

/**
 * Owns the single storage-invalidation observer shared by configuration, settings and statistics.
 * Creates separate history-selector, theme, engine-selection, connectivity and background-work ViewModels on demand.
 *
 * @author Dmitriy Moroz
 */
val componentsViewModelModule = module {
    factory {
        InputHistoryViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            galleryReads = get(),
            getGenerationResultUseCase = get(),
        )
    }

    single<StorageUsageObserver> {
        StorageUsageObserverImpl()
    }
    factory {
        AiSdAppThemeViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
        )
    }
    factory {
        EngineSelectionViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchAndGetSwarmUiModelsUseCase = get(),
            fetchEasyDiffusionModelsUseCase = get(),
            getEasyDiffusionModelsUseCase = get(),
            captureEasyDiffusionConnectionUseCase = get(),
            selectEasyDiffusionModelUseCase = get(),
            providerSelectionGuard = get(),
            observeLocalOnnxModelsUseCase = get(),
            observeLocalCoreMlModelsUseCase = get(),
            observeLocalBonsaiModelsUseCase = get(),
            observeLocalMnnModelsUseCase = get(),
            selectMnnModelUseCase = get(),
            observeLocalSdxlModelsUseCase = get(),
            fetchAndGetStabilityAiEnginesUseCase = get(),
            getHuggingFaceModelsUseCase = get(),
            preferenceManager = get(),
            getConfigurationUseCase = get(),
            selectStableDiffusionModelUseCase = get(),
            getStableDiffusionModelsUseCase = get(),
        )
    }
    factory {
        ConnectivityViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            observeServerConnectivityUseCase = get(),
            getMonitorConnectivityUseCase = get(),
            observeMonitorConnectivityUseCase = get(),
        )
    }
    factory {
        BackgroundWorkViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            backgroundWorkObserver = get(),
        )
    }
}
