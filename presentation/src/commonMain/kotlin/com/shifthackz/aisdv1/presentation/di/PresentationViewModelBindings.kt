package com.shifthackz.aisdv1.presentation.di

import com.shifthackz.aisdv1.core.validation.di.easyDiffusionSeedValidatorQualifier

import com.shifthackz.aisdv1.core.validation.di.nativeSeedValidatorQualifier
import com.shifthackz.aisdv1.presentation.modal.download.DownloadDialogViewModel
import com.shifthackz.aisdv1.presentation.modal.embedding.EmbeddingViewModel
import com.shifthackz.aisdv1.presentation.modal.extras.ExtrasViewModel
import com.shifthackz.aisdv1.presentation.modal.history.InputHistoryViewModel
import com.shifthackz.aisdv1.presentation.modal.tag.EditTagViewModel
import com.shifthackz.aisdv1.presentation.model.ExtraType
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.ConfigurationLoaderRouter
import com.shifthackz.aisdv1.core.navigation.router.BenchmarkRouter
import com.shifthackz.aisdv1.core.navigation.router.DebugMenuRouter
import com.shifthackz.aisdv1.core.navigation.router.DonateRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryDetailRouter
import com.shifthackz.aisdv1.core.navigation.router.GalleryRouter
import com.shifthackz.aisdv1.core.navigation.router.HistoryRouter
import com.shifthackz.aisdv1.core.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.LoggerRouter
import com.shifthackz.aisdv1.core.navigation.router.NetworkUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.OnBoardingRouter
import com.shifthackz.aisdv1.core.navigation.router.ReportRouter
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.core.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.core.navigation.router.SplashRouter
import com.shifthackz.aisdv1.core.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.core.navigation.router.TextToImageRouter
import com.shifthackz.aisdv1.core.navigation.router.WebUiRouter
import com.shifthackz.aisdv1.presentation.screen.benchmark.BenchmarkViewModel
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuPlatformActions
import com.shifthackz.aisdv1.presentation.screen.debug.DebugMenuViewModel
import com.shifthackz.aisdv1.presentation.screen.donate.DonateViewModel
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailPlatformActions
import com.shifthackz.aisdv1.presentation.screen.gallery.detail.GalleryDetailViewModel
import com.shifthackz.aisdv1.presentation.screen.gallery.list.GalleryViewModel
import com.shifthackz.aisdv1.presentation.screen.history.HistoryViewModel
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.img2img.ImageToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.loader.ConfigurationLoaderViewModel
import com.shifthackz.aisdv1.presentation.screen.logger.LoggerViewModel
import com.shifthackz.aisdv1.presentation.screen.networkusage.NetworkUsageViewModel
import com.shifthackz.aisdv1.presentation.screen.onboarding.OnBoardingViewModel
import com.shifthackz.aisdv1.presentation.screen.report.ReportViewModel
import com.shifthackz.aisdv1.presentation.screen.settings.SettingsViewModel
import com.shifthackz.aisdv1.presentation.screen.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.screen.setup.ServerSetupViewModel
import com.shifthackz.aisdv1.presentation.screen.splash.SplashViewModel
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageObserver
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageViewModel
import com.shifthackz.aisdv1.presentation.screen.storageusage.platform.StorageUsagePlatformActions
import com.shifthackz.aisdv1.presentation.screen.txt2img.TextToImageViewModel
import com.shifthackz.aisdv1.presentation.screen.web.webui.WebUiViewModel
import com.shifthackz.aisdv1.presentation.theme.global.AiSdAppThemeViewModel
import com.shifthackz.aisdv1.presentation.widget.connectivity.ConnectivityViewModel
import com.shifthackz.aisdv1.presentation.widget.engine.EngineSelectionViewModel
import com.shifthackz.aisdv1.presentation.widget.work.BackgroundWorkViewModel
import org.koin.core.module.Module

/**
 * Registers ViewModel factories and shared presentation observers.
 *
 * The usage screens intentionally have separate ViewModels, while Settings receives only summary
 * observers so the app settings list stays lightweight and never owns destructive usage actions.
 *
 * @receiver Koin module receiving presentation ViewModel factories.
 *
 * @author Dmitriy Moroz
 */
internal fun Module.registerPresentationViewModelBindings() {
    single {
        StorageUsageObserver()
    }
    factory {
        AiSdAppThemeViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
        )
    }
    factory { (splashRouter: SplashRouter) ->
        SplashViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            splashNavigationUseCase = get(),
            splashRouter = splashRouter,
        )
    }
    factory { (launchSource: LaunchSource, router: OnBoardingRouter) ->
        OnBoardingViewModel(
            diagnostics = get(),
            launchSource = launchSource,
            dispatchersProvider = get(),
            router = router,
            splashNavigationUseCase = get(),
            preferenceManager = get(),
            buildInfoProvider = get(),
        )
    }
    factory { (router: ConfigurationLoaderRouter) ->
        ConfigurationLoaderViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            dataPreLoaderUseCase = get(),
            getConfigurationUseCase = get(),
            router = router,
        )
    }
    factory { (router: BenchmarkRouter) ->
        BenchmarkViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            benchmarkManager = get(),
            router = router,
            platformActions = get(),
        )
    }
    factory { (router: DonateRouter) ->
        DonateViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchSupportersUseCase = get(),
            linksProvider = get(),
            router = router,
        )
    }
    factory { (router: DebugMenuRouter, platformActions: DebugMenuPlatformActions) ->
        DebugMenuViewModel(
            diagnosticReports = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
            debugInsertBadBase64UseCase = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: LoggerRouter) ->
        LoggerViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            logReader = get(),
            platformActions = get(),
            router = router,
        )
    }
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
    factory { (itemId: Long, router: ReportRouter) ->
        ReportViewModel(
            diagnostics = get(),
            itemId = itemId,
            dispatchersProvider = get(),
            sendReportUseCase = get(),
            getGenerationResultUseCase = get(),
            getLastResultFromCacheUseCase = get(),
            router = router,
            buildInfoProvider = get(),
        )
    }
    factory { (prompt: String, negativePrompt: String, type: ExtraType) ->
        ExtrasViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchAndGetLorasUseCase = get(),
            fetchAndGetHyperNetworksUseCase = get(),
            preferenceManager = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
            type = type,
        )
    }
    factory { (prompt: String, negativePrompt: String) ->
        EmbeddingViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            fetchAndGetEmbeddingsUseCase = get(),
            preferenceManager = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
        )
    }
    factory { (prompt: String, negativePrompt: String, tag: String, isNegative: Boolean) ->
        EditTagViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            prompt = prompt,
            negativePrompt = negativePrompt,
            tag = tag,
            isNegative = isNegative,
        )
    }
    factory {
        InputHistoryViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            galleryReads = get(),
            getGenerationResultUseCase = get(),
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
    factory { (router: TextToImageRouter) ->
        TextToImageViewModel(
            observeEasyDiffusionProgressUseCase = get(),
            nativeGenerationGateway = get(),
            diagnostics = get(),
            openAiOAuthGateway = get(),
            providerSelectionGuard = get(),
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            fetchAndGetArliAiModelsUseCase = get(),
            isADetailerAvailableUseCase = get(),
            textToImageUseCase = get(),
            saveGenerationResultUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            interruptSourceGenerationUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            observeStableDiffusionCppProcessStatusUseCase = get(),
            observeCoreMlProcessStatusUseCase = get(),
            observeBonsaiProcessStatusUseCase = get(),
            preferenceManager = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            wakeLockInterActor = get(),
            platformServices = get(),
            buildInfoProvider = get(),
            generationFormUpdateEvent = get(),
            dimensionValidator = get(),
            seedValidator = get(),
            nativeSeedValidator = get(nativeSeedValidatorQualifier),
            easyDiffusionSeedValidator = get(easyDiffusionSeedValidatorQualifier),
            localGenerationBenchmarkGateProvider = { get() },
            imageSaver = get(),
            imageSharer = get(),
            router = router,
        )
    }
    factory { (router: ImageToImageRouter, platformActions: ImageToImagePlatformActions) ->
        ImageToImageViewModel(
            observeEasyDiffusionProgressUseCase = get(),
            originalImageBudget = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            getConfigurationUseCase = get(),
            getStableDiffusionSamplersUseCase = get(),
            getForgeModulesUseCase = get(),
            fetchAndGetArliAiModelsUseCase = get(),
            isADetailerAvailableUseCase = get(),
            getRandomImageUseCase = get(),
            imageToImageUseCase = get(),
            saveGenerationResultUseCase = get(),
            saveLastResultToCacheUseCase = get(),
            interruptSourceGenerationUseCase = get(),
            observeHordeProcessStatusUseCase = get(),
            observeLocalDiffusionProcessStatusUseCase = get(),
            preferenceManager = get(),
            backgroundTaskManager = get(),
            backgroundWorkObserver = get(),
            wakeLockInterActor = get(),
            platformServices = get(),
            buildInfoProvider = get(),
            generationFormUpdateEvent = get(),
            dimensionValidator = get(),
            seedValidator = get(),
            nativeSeedValidator = get(nativeSeedValidatorQualifier),
            easyDiffusionSeedValidator = get(easyDiffusionSeedValidatorQualifier),
            localGenerationBenchmarkGateProvider = { get() },
            imageSaver = get(),
            imageSharer = get(),
            router = router,
            platformActions = platformActions,
        )
    }
    factory { (router: HistoryRouter) ->
        HistoryViewModel(
            galleryReads = get(),
            diagnostics = get(),
            dispatchersProvider = get(),
            router = router,
        )
    }
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
    factory { (launchSource: LaunchSource, router: ServerSetupRouter) ->
        ServerSetupViewModel(
            diagnostics = get(),
            launchSource = launchSource,
            dispatchersProvider = get(),
            buildInfoProvider = get(),
            getConfigurationUseCase = get(),
            retryAuthorizationStorageUseCase = get(),
            resetAuthorizationCredentialsUseCase = get(),
            getLocalOnnxModelsUseCase = get(),
            getLocalMediaPipeModelsUseCase = get(),
            getLocalSdxlModelsUseCase = get(),
            getLocalCoreMlModelsUseCase = get(),
            getLocalBonsaiModelsUseCase = get(),
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
            observeLocalSdxlModelsUseCase = get(),
            fetchAndGetStabilityAiEnginesUseCase = get(),
            getHuggingFaceModelsUseCase = get(),
            preferenceManager = get(),
            getConfigurationUseCase = get(),
            selectStableDiffusionModelUseCase = get(),
            getStableDiffusionModelsUseCase = get(),
        )
    }
    factory { (router: WebUiRouter) ->
        WebUiViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            preferenceManager = get(),
            router = router,
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
            imageLoader = get(),
        )
    }
}
