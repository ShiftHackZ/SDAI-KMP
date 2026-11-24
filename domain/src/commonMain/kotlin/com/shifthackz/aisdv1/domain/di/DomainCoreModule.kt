package com.shifthackz.aisdv1.domain.di

import com.shifthackz.aisdv1.domain.usecase.appupdate.CheckAppUpdateUseCase
import com.shifthackz.aisdv1.domain.usecase.appupdate.CheckAppUpdateUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.TrackRemoteGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TrackRemoteGenerationUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationPerformanceUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationPerformanceUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveMnnProcessStatusUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.generation.ObserveMnnProcessStatusUseCase

import com.shifthackz.aisdv1.domain.gateway.MnnBackendPolicyImpl

import com.shifthackz.aisdv1.domain.gateway.MnnBackendPolicy

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.SelectEasyDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.SelectEasyDiffusionModelUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.connectivity.TestEasyDiffusionConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestEasyDiffusionConnectivityUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToEasyDiffusionUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GenerateEasyDiffusionUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.InterruptEasyDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.InterruptEasyDiffusionUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GetEasyDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GetEasyDiffusionModelsUseCaseImpl

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.FetchEasyDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.FetchEasyDiffusionModelsUseCaseImpl

import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.NoOpNativeGenerationGateway
import com.shifthackz.aisdv1.domain.usecase.arliai.FetchAndGetArliAiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.arliai.FetchAndGetArliAiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.feature.work.NoOpBackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.NoOpBackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.NoOpServerConnectivityGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.ServerConnectivityGateway
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActorImpl
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import com.shifthackz.aisdv1.domain.repository.UnsupportedMnnGenerationRepository
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMnnModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.RefreshLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.RefreshLocalModelCatalogUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMnnModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalMnnModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalMnnModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMnnUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.SelectMnnModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.SelectMnnModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMnnUseCaseImpl
import com.shifthackz.aisdv1.domain.repository.BonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.CoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.MediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpBonsaiGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpCoreMlGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpLocalDiffusionGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpMediaPipeGenerationRepository
import com.shifthackz.aisdv1.domain.repository.NoOpNetworkUsageRepository
import com.shifthackz.aisdv1.domain.repository.NoOpStableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.usecase.caching.AppCacheCleaner
import com.shifthackz.aisdv1.domain.usecase.caching.ClearAppCacheUseCase
import com.shifthackz.aisdv1.domain.repository.NoOpWakeLockRepository
import com.shifthackz.aisdv1.domain.repository.NetworkUsageRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionCppGenerationRepository
import com.shifthackz.aisdv1.domain.repository.WakeLockRepository
import com.shifthackz.aisdv1.domain.usecase.caching.ClearAppCacheUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.DataPreLoaderUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.caching.NoOpAppCacheCleaner
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestArliAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestFalAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestHordeApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestHuggingFaceApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestOpenAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestStabilityAiApiKeyUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.DefaultTestSwarmUiConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.GetMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.GetMonitorConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveMonitorConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveMonitorConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.ObserveSeverConnectivityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.PingStableDiffusionServiceUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.PingStableDiffusionServiceUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestArliAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestFalAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestHordeApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestHuggingFaceApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestOpenAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestStabilityAiApiKeyUseCase
import com.shifthackz.aisdv1.domain.usecase.connectivity.TestSwarmUiConnectivityUseCase
import com.shifthackz.aisdv1.domain.usecase.debug.DebugInsertBadBase64UseCase
import com.shifthackz.aisdv1.domain.usecase.debug.DebugInsertBadBase64UseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.donate.FetchSupportersUseCase
import com.shifthackz.aisdv1.domain.usecase.donate.FetchSupportersUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import com.shifthackz.aisdv1.domain.usecase.forgemodule.GetForgeModulesUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteGalleryItemsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryIdsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryIdsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetGalleryItemsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetGalleryItemsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.GetMediaStoreInfoUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetMediaStoreInfoUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsLikedUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsLikedUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsVisibilityUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.SetGalleryItemsVisibilityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageLikeUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageLikeUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageVisibilityUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.ToggleImageVisibilityUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultPagedUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultPagedUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetGenerationResultUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptWorkerGenerationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationResultCountUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationResultCountUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationResultPageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationResultPageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationResultPreviewPageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveGenerationResultPreviewPageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveStableDiffusionCppProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveStableDiffusionCppProcessStatusUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.report.SendReportUseCase
import com.shifthackz.aisdv1.domain.usecase.report.SendReportUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdhypernet.FetchAndGetHyperNetworksUseCase
import com.shifthackz.aisdv1.domain.usecase.sdhypernet.FetchAndGetHyperNetworksUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdlora.FetchAndGetLorasUseCase
import com.shifthackz.aisdv1.domain.usecase.sdlora.FetchAndGetLorasUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCase
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToA1111UseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToArliAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToBonsaiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToBonsaiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToCoreMlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToCoreMlUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToFalAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHordeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHuggingFaceUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToLocalDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToLocalDiffusionUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMediaPipeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMediaPipeUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiCompatibleUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSdxlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSdxlUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToStabilityAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSwarmUiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToA1111UseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToArliAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToFalAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToHordeUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToHuggingFaceUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToOpenAiCompatibleUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToOpenAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToStabilityAiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultConnectToSwarmUiUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultGetConfigurationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.DefaultSetServerConfigurationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ObserveNetworkUsageUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ObserveNetworkUsageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ResetAuthorizationCredentialsUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ResetAuthorizationCredentialsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.ResetNetworkUsageUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ResetNetworkUsageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.RetryAuthorizationStorageUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.RetryAuthorizationStorageUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.settings.SetServerConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCase
import com.shifthackz.aisdv1.domain.usecase.splash.SplashNavigationUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchStabilityAiEnginesUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchSwarmUiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchSwarmUiModelsUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.wakelock.AcquireWakelockUseCase
import com.shifthackz.aisdv1.domain.usecase.wakelock.AcquireWakelockUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.wakelock.ReleaseWakeLockUseCase
import com.shifthackz.aisdv1.domain.usecase.wakelock.ReleaseWakeLockUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.faq.FetchOrGetFaqUseCase
import com.shifthackz.aisdv1.domain.usecase.faq.FetchOrGetFaqUseCaseImpl
import org.koin.dsl.module

/**
 * Domain-layer Koin module that provides default repositories and use case implementations.
 * Network usage receives a no-op repository here so common presentation can resolve its contracts
 * before platform data modules override the binding with a persistent implementation.
 *
 * @author Dmitriy Moroz
 */
val coreDomainModule = module {
    factory<FetchOrGetFaqUseCase> { FetchOrGetFaqUseCaseImpl(repository = get()) }
    factory<CheckAppUpdateUseCase> { CheckAppUpdateUseCaseImpl(get()) }
    factory<TrackRemoteGenerationUseCase> { TrackRemoteGenerationUseCaseImpl(get(), get()) }
    factory<ObserveGenerationPerformanceUseCase> {
        ObserveGenerationPerformanceUseCaseImpl(get(), get(), get())
    }
    single<MnnBackendPolicy> { MnnBackendPolicyImpl(get()) }
    factory<ObserveMnnProcessStatusUseCase> { ObserveMnnProcessStatusUseCaseImpl(get()) }
    single<NativeGenerationGateway> { NoOpNativeGenerationGateway }
    single { ProviderSelectionGuard() }
    single<ServerConnectivityGateway> { NoOpServerConnectivityGateway }
    single<BackgroundWorkObserver> { NoOpBackgroundWorkObserver }
    single<BackgroundTaskManager> { NoOpBackgroundTaskManager }
    single<LocalDiffusionGenerationRepository> { NoOpLocalDiffusionGenerationRepository }
    single<MediaPipeGenerationRepository> { NoOpMediaPipeGenerationRepository }
    single<StableDiffusionCppGenerationRepository> { NoOpStableDiffusionCppGenerationRepository }
    single<CoreMlGenerationRepository> { NoOpCoreMlGenerationRepository }
    single<MnnGenerationRepository> { UnsupportedMnnGenerationRepository }
    factory<GetCachedLocalModelCatalogUseCase> {
        GetCachedLocalModelCatalogUseCaseImpl(
            get(),
            get()
        )
    }
    factory<RefreshLocalModelCatalogUseCase> { RefreshLocalModelCatalogUseCaseImpl(get(), get()) }
    factory<GetLocalMnnModelsUseCase> { GetLocalMnnModelsUseCaseImpl(get()) }
    factory<ObserveLocalMnnModelsUseCase> { ObserveLocalMnnModelsUseCaseImpl(get()) }
    factory<SelectMnnModelUseCase> { SelectMnnModelUseCaseImpl(get(), get(), get()) }
    factory<ConnectToMnnUseCase> { ConnectToMnnUseCaseImpl(get(), get(), get()) }
    single<BonsaiGenerationRepository> { NoOpBonsaiGenerationRepository }
    single<WakeLockRepository> { NoOpWakeLockRepository }
    single<NetworkUsageRepository> { NoOpNetworkUsageRepository }
    single<AppCacheCleaner> { NoOpAppCacheCleaner }
    factory<AcquireWakelockUseCase> {
        AcquireWakelockUseCaseImpl(wakeLockRepository = get())
    }
    factory<ReleaseWakeLockUseCase> {
        ReleaseWakeLockUseCaseImpl(wakeLockRepository = get())
    }
    factory<WakeLockInterActor> {
        WakeLockInterActorImpl(
            acquireWakelockUseCase = get(),
            releaseWakeLockUseCase = get(),
        )
    }
    factory<GetMonitorConnectivityUseCase> {
        GetMonitorConnectivityUseCaseImpl(preferenceManager = get())
    }
    factory<ObserveMonitorConnectivityUseCase> {
        ObserveMonitorConnectivityUseCaseImpl(preferenceManager = get())
    }
    factory<PingStableDiffusionServiceUseCase> {
        PingStableDiffusionServiceUseCaseImpl(repository = get())
    }
    factory<ObserveSeverConnectivityUseCase> {
        ObserveSeverConnectivityUseCaseImpl(serverConnectivityGateway = get())
    }
    factory<TestConnectivityUseCase> {
        DefaultTestConnectivityUseCaseImpl(
            remoteDataSource = get(),
            connections = get(),
        )
    }
    factory<TestSwarmUiConnectivityUseCase> {
        DefaultTestSwarmUiConnectivityUseCaseImpl(
            remoteDataSource = get(),
            connections = get(),
        )
    }
    factory<TestHordeApiKeyUseCase> {
        DefaultTestHordeApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestHuggingFaceApiKeyUseCase> {
        DefaultTestHuggingFaceApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestOpenAiApiKeyUseCase> {
        DefaultTestOpenAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestStabilityAiApiKeyUseCase> {
        DefaultTestStabilityAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestFalAiApiKeyUseCase> {
        DefaultTestFalAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<TestArliAiApiKeyUseCase> {
        DefaultTestArliAiApiKeyUseCaseImpl(
            configurationStore = get(),
            remoteDataSource = get(),
        )
    }
    factory<DataPreLoaderUseCase> {
        DataPreLoaderUseCaseImpl(
            serverConfigurationRepository = get(),
            sdModelsRepository = get(),
            sdSamplersRepository = get(),
            sdHyperNetworksRepository = get(),
            sdEmbeddingsRepository = get(),
        )
    }
    factory<ClearAppCacheUseCase> {
        ClearAppCacheUseCaseImpl(
            appCacheCleaner = get(),
            repository = get(),
        )
    }
    factory<GetLastResultFromCacheUseCase> {
        GetLastResultFromCacheUseCaseImpl(temporaryGenerationResultRepository = get())
    }
    factory<SaveLastResultToCacheUseCase> {
        SaveLastResultToCacheUseCaseImpl(
            temporaryGenerationResultRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<DebugInsertBadBase64UseCase> {
        DebugInsertBadBase64UseCaseImpl(
            repository = get(),
            timeProvider = get(),
        )
    }
    factory<SplashNavigationUseCase> {
        SplashNavigationUseCaseImpl(
            preferenceManager = get(),
            getBackupStartupStage = get(),
            loadPendingBackup = get(),
            getPendingExport = get()
        )
    }
    factory<ObserveLocalOnnxModelsUseCase> {
        ObserveLocalOnnxModelsUseCaseImpl(repository = get())
    }
    factory<ObserveLocalCoreMlModelsUseCase> {
        ObserveLocalCoreMlModelsUseCaseImpl(repository = get())
    }
    factory<ObserveLocalBonsaiModelsUseCase> {
        ObserveLocalBonsaiModelsUseCaseImpl(repository = get())
    }
    factory<ObserveLocalSdxlModelsUseCase> {
        ObserveLocalSdxlModelsUseCaseImpl(repository = get())
    }
    factory<GetLocalOnnxModelsUseCase> {
        GetLocalOnnxModelsUseCaseImpl(refreshCatalog = get())
    }
    factory<GetLocalMediaPipeModelsUseCase> {
        GetLocalMediaPipeModelsUseCaseImpl(refreshCatalog = get())
    }
    factory<GetLocalSdxlModelsUseCase> {
        GetLocalSdxlModelsUseCaseImpl(refreshCatalog = get())
    }
    factory<GetLocalCoreMlModelsUseCase> {
        GetLocalCoreMlModelsUseCaseImpl(refreshCatalog = get())
    }
    factory<GetLocalBonsaiModelsUseCase> {
        GetLocalBonsaiModelsUseCaseImpl(refreshCatalog = get())
    }
    factory<GetLocalModelUseCase> {
        GetLocalModelUseCaseImpl(localDataSource = get())
    }
    factory<DownloadModelsUseCase> { DownloadModelsUseCaseImpl(get(), get()) }
    single<DownloadModelUseCase> {
        DownloadModelUseCaseImpl(downloadableModelRepository = get())
    }
    factory<DeleteModelUseCase> {
        DeleteModelUseCaseImpl(downloadableModelRepository = get(), preferenceManager = get())
    }
    factory<TextToImageUseCase> {
        TextToImageUseCaseImpl(
            deviceProbe = get(),
            trackRemoteGenerationUseCase = get(),
            generationRestoreGate = get(),
            generateEasyDiffusionUseCase = get(),
            captureEasyDiffusionConnectionUseCase = get(),
            stableDiffusionGenerationRepository = get(),
            hordeGenerationRepository = get(),
            huggingFaceGenerationRepository = get(),
            openAiGenerationRepository = get(),
            openAiCompatibleGenerationRepository = get(),
            openAiOAuthGenerationRepository = get(),
            stabilityAiGenerationRepository = get(),
            falAiGenerationRepository = get(),
            arliAiGenerationRepository = get(),
            swarmUiGenerationRepository = get(),
            localDiffusionGenerationRepository = get(),
            mediaPipeGenerationRepository = get(),
            stableDiffusionCppGenerationRepository = get(),
            coreMlGenerationRepository = get(),
            bonsaiGenerationRepository = get(),
            mnnGenerationRepository = get(),
            preferenceManager = get(),
            autoUploadToCivitaiUseCase = get(),
        )
    }
    factory<ImageToImageUseCase> {
        ImageToImageUseCaseImpl(
            deviceProbe = get(),
            trackRemoteGenerationUseCase = get(),
            generationRestoreGate = get(),
            generateEasyDiffusionUseCase = get(),
            captureEasyDiffusionConnectionUseCase = get(),
            stableDiffusionGenerationRepository = get(),
            swarmUiGenerationRepository = get(),
            hordeGenerationRepository = get(),
            huggingFaceGenerationRepository = get(),
            stabilityAiGenerationRepository = get(),
            coreMlGenerationRepository = get(),
            falAiGenerationRepository = get(),
            arliAiGenerationRepository = get(),
            preferenceManager = get(),
            autoUploadToCivitaiUseCase = get(),
        )
    }
    factory<GetRandomImageUseCase> {
        GetRandomImageUseCaseImpl(randomImageRepository = get())
    }
    factory<ObserveHordeProcessStatusUseCase> {
        ObserveHordeProcessStatusUseCaseImpl(hordeGenerationRepository = get())
    }
    factory<ObserveLocalDiffusionProcessStatusUseCase> {
        ObserveLocalDiffusionProcessStatusUseCaseImpl(localDiffusionGenerationRepository = get())
    }
    factory<ObserveCoreMlProcessStatusUseCase> {
        ObserveCoreMlProcessStatusUseCaseImpl(coreMlGenerationRepository = get())
    }
    factory<ObserveBonsaiProcessStatusUseCase> {
        ObserveBonsaiProcessStatusUseCaseImpl(bonsaiGenerationRepository = get())
    }
    factory<ObserveStableDiffusionCppProcessStatusUseCase> {
        ObserveStableDiffusionCppProcessStatusUseCaseImpl(
            stableDiffusionCppGenerationRepository = get(),
        )
    }
    factory<InterruptWorkerGenerationUseCase> {
        InterruptWorkerGenerationUseCaseImpl(
            mnnRepository = get(),
            interruptEasyDiffusionUseCase = get(),
            nativeGenerationGateway = get(),
            interruptSourceGenerationUseCase = get(),
            buildInfoProvider = get(),
        )
    }
    factory<InterruptSourceGenerationUseCase> {
        InterruptSourceGenerationUseCaseImpl(
            stableDiffusionGenerationRepository = get(),
            hordeGenerationRepository = get(),
            localDiffusionGenerationRepository = get(),
            stableDiffusionCppGenerationRepository = get(),
            coreMlGenerationRepository = get(),
            bonsaiGenerationRepository = get(),
            mnnGenerationRepository = get(),
        )
    }
    factory<SaveGenerationResultUseCase> {
        SaveGenerationResultUseCaseImpl(repository = get())
    }
    factory<GetGenerationResultUseCase> {
        GetGenerationResultUseCaseImpl(repository = get())
    }
    factory<GetGenerationResultPagedUseCase> {
        GetGenerationResultPagedUseCaseImpl(repository = get())
    }
    factory<ObserveGenerationResultPageUseCase> {
        ObserveGenerationResultPageUseCaseImpl(repository = get())
    }
    factory<ObserveGenerationResultPreviewPageUseCase> {
        ObserveGenerationResultPreviewPageUseCaseImpl(repository = get())
    }
    factory<ObserveGenerationResultCountUseCase> {
        ObserveGenerationResultCountUseCaseImpl(repository = get())
    }
    factory<GetAllGalleryIdsUseCase> {
        GetAllGalleryIdsUseCaseImpl(repository = get())
    }
    factory<GetAllGalleryUseCase> {
        GetAllGalleryUseCaseImpl(repository = get())
    }
    factory<GetGalleryItemsUseCase> {
        GetGalleryItemsUseCaseImpl(generationResultRepository = get())
    }
    factory<DeleteGalleryItemUseCase> {
        DeleteGalleryItemUseCaseImpl(repository = get())
    }
    factory<DeleteGalleryItemsUseCase> {
        DeleteGalleryItemsUseCaseImpl(generationResultRepository = get())
    }
    factory<DeleteAllGalleryUseCase> {
        DeleteAllGalleryUseCaseImpl(generationResultRepository = get())
    }
    factory<SetGalleryItemsVisibilityUseCase> {
        SetGalleryItemsVisibilityUseCaseImpl(generationResultRepository = get())
    }
    factory<SetGalleryItemsLikedUseCase> {
        SetGalleryItemsLikedUseCaseImpl(generationResultRepository = get())
    }
    factory<GetMediaStoreInfoUseCase> {
        GetMediaStoreInfoUseCaseImpl(mediaStoreGateway = get())
    }
    factory<ToggleImageVisibilityUseCase> {
        ToggleImageVisibilityUseCaseImpl(repository = get())
    }
    factory<ToggleImageLikeUseCase> {
        ToggleImageLikeUseCaseImpl(repository = get())
    }
    factory<ResetAuthorizationCredentialsUseCase> {
        ResetAuthorizationCredentialsUseCaseImpl(
            get(),
            get(),
            get(),
            get()
        )
    }
    factory<RetryAuthorizationStorageUseCase> {
        RetryAuthorizationStorageUseCaseImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory<GetConfigurationUseCase> {
        DefaultGetConfigurationUseCaseImpl(
            configurationStore = get(),
            authorizationStore = get(),
            mnnBackendPolicy = get(),
        )
    }
    factory<SetServerConfigurationUseCase> {
        DefaultSetServerConfigurationUseCaseImpl(
            backupPreferences = get(),
            dispatchersProvider = get(),
            providerSelectionGuard = get(),
            configurationStore = get(),
            authorizationStore = get(),
            preferenceManager = get(),
        )
    }
    factory<ObserveNetworkUsageUseCase> {
        ObserveNetworkUsageUseCaseImpl(repository = get())
    }
    factory<ResetNetworkUsageUseCase> {
        ResetNetworkUsageUseCaseImpl(repository = get())
    }
    factory<ConnectToA1111UseCase> {
        DefaultConnectToA1111UseCaseImpl(
            preferenceManager = get(),
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testConnectivityUseCase = get(),
            dataPreLoaderUseCase = get(),
        )
    }
    factory<ConnectToSwarmUiUseCase> {
        DefaultConnectToSwarmUiUseCaseImpl(
            preferenceManager = get(),
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testSwarmUiConnectivityUseCase = get(),
        )
    }
    factory<ConnectToHordeUseCase> {
        DefaultConnectToHordeUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testHordeApiKeyUseCase = get(),
        )
    }
    factory<ConnectToHuggingFaceUseCase> {
        DefaultConnectToHuggingFaceUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testHuggingFaceApiKeyUseCase = get(),
        )
    }
    factory<ConnectToLocalDiffusionUseCase> {
        ConnectToLocalDiffusionUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToMediaPipeUseCase> {
        ConnectToMediaPipeUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToSdxlUseCase> {
        ConnectToSdxlUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToCoreMlUseCase> {
        ConnectToCoreMlUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToBonsaiUseCase> {
        ConnectToBonsaiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<TestEasyDiffusionConnectivityUseCase> { TestEasyDiffusionConnectivityUseCaseImpl(get()) }
    factory<ConnectToEasyDiffusionUseCase> {
        ConnectToEasyDiffusionUseCaseImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    factory<CaptureEasyDiffusionConnectionUseCase> {
        CaptureEasyDiffusionConnectionUseCaseImpl(
            get(),
            get()
        )
    }
    factory<SelectEasyDiffusionModelUseCase> {
        SelectEasyDiffusionModelUseCaseImpl(
            get(),
            get(),
            get()
        )
    }
    factory<FetchEasyDiffusionModelsUseCase> { FetchEasyDiffusionModelsUseCaseImpl(get()) }
    factory<GetEasyDiffusionModelsUseCase> { GetEasyDiffusionModelsUseCaseImpl(get()) }
    factory<ObserveEasyDiffusionProgressUseCase> { ObserveEasyDiffusionProgressUseCaseImpl(get()) }
    factory<InterruptEasyDiffusionUseCase> { InterruptEasyDiffusionUseCaseImpl(get()) }
    factory<GenerateEasyDiffusionUseCase> { GenerateEasyDiffusionUseCaseImpl(get(), get(), get()) }
    factory<ConnectToOpenAiCompatibleUseCase> {
        DefaultConnectToOpenAiCompatibleUseCaseImpl(
            repository = get(),
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
        )
    }
    factory<ConnectToOpenAiUseCase> {
        DefaultConnectToOpenAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testOpenAiApiKeyUseCase = get(),
        )
    }
    factory<ConnectToStabilityAiUseCase> {
        DefaultConnectToStabilityAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testStabilityAiApiKeyUseCase = get(),
        )
    }
    factory<ConnectToFalAiUseCase> {
        DefaultConnectToFalAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testFalAiApiKeyUseCase = get(),
        )
    }
    factory<ConnectToArliAiUseCase> {
        DefaultConnectToArliAiUseCaseImpl(
            getConfigurationUseCase = get(),
            setServerConfigurationUseCase = get(),
            testArliAiApiKeyUseCase = get(),
        )
    }
    factory<FetchSupportersUseCase> {
        FetchSupportersUseCaseImpl(repository = get())
    }
    factory<FetchHuggingFaceModelsUseCase> {
        FetchHuggingFaceModelsUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<SendReportUseCase> {
        SendReportUseCaseImpl(repository = get())
    }
    factory<FetchStabilityAiEnginesUseCase> {
        FetchStabilityAiEnginesUseCaseImpl(repository = get())
    }
    factory<FetchAndGetStabilityAiEnginesUseCase> {
        FetchAndGetStabilityAiEnginesUseCaseImpl(
            fetchStabilityAiEnginesUseCase = get(),
            preferenceManager = get(),
        )
    }
    factory<ObserveStabilityAiCreditsUseCase> {
        ObserveStabilityAiCreditsUseCaseImpl(repository = get())
    }
    factory<FetchSwarmUiModelsUseCase> {
        FetchSwarmUiModelsUseCaseImpl(remoteDataSource = get())
    }
    factory<FetchAndGetSwarmUiModelsUseCase> {
        FetchAndGetSwarmUiModelsUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<FetchAndGetArliAiModelsUseCase> {
        FetchAndGetArliAiModelsUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<FetchAndGetLorasUseCase> {
        FetchAndGetLorasUseCaseImpl(lorasRepository = get())
    }
    factory<FetchAndGetEmbeddingsUseCase> {
        FetchAndGetEmbeddingsUseCaseImpl(repository = get())
    }
    factory<FetchAndGetHyperNetworksUseCase> {
        FetchAndGetHyperNetworksUseCaseImpl(stableDiffusionHyperNetworksRepository = get())
    }
    factory<GetStableDiffusionModelsUseCase> {
        GetStableDiffusionModelsUseCaseImpl(
            preferenceManager = get(),
            serverConfigurationRepository = get(),
            sdModelsRepository = get(),
        )
    }
    factory<SelectStableDiffusionModelUseCase> {
        SelectStableDiffusionModelUseCaseImpl(
            serverConfigurationRepository = get(),
            preferenceManager = get(),
        )
    }
    factory<GetStableDiffusionSamplersUseCase> {
        GetStableDiffusionSamplersUseCaseImpl(repository = get())
    }
    factory<GetForgeModulesUseCase> {
        GetForgeModulesUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
    factory<IsADetailerAvailableUseCase> {
        IsADetailerAvailableUseCaseImpl(
            preferenceManager = get(),
            repository = get(),
        )
    }
}
