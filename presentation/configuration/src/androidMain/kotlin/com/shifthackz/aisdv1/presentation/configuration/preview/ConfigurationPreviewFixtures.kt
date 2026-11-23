package com.shifthackz.aisdv1.presentation.configuration.preview

import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Nonsecret provider and catalog samples for setup previews; URLs are display-only fixtures.
 * No saved configuration, real credentials, models or download workers are accessed.
 *
 * @author Dmitriy Moroz
 */
internal object ConfigurationPreviewFixtures {
    val model = ServerSetupState.LocalModel(
        id = "preview-model", name = "Sample diffusion model", size = "1.2 GB",
        sources = listOf("https://huggingface.co/example/model/resolve/main/model.zip"),
    )
    val downloaded = model.copy(
        id = "preview-installed",
        name = "Installed sample model",
        downloaded = true,
        selected = true,
        downloadState = DownloadState.Complete("/sample/app-private/model"),
    )
    val models = listOf(
        downloaded, model,
        model.copy(
            id = "preview-downloading",
            name = "Downloading sample model",
            downloadState = DownloadState.Downloading(42)
        ),
        model.copy(
            id = "preview-failed",
            name = "Interrupted sample download",
            downloadState = DownloadState.Error(IllegalStateException("Sample transfer interruption"))
        ),
    )
    val sources = ServerSource.entries.filter {
        Platform.ANDROID in it.allowedPlatforms && BuildType.FULL in it.allowedInBuilds
    }
    val source = ServerSetupState(
        loadingConfiguration = false,
        showBackNavArrow = true,
        allowedModes = sources,
        serverUrl = "https://a1111.example",
        swarmUiUrl = "https://swarm.example",
        easyDiffusionUrl = "https://diffusion.example",
        openAiCompatibleUrl = "https://images.example/v1",
        demoModeUrl = "https://demo.example",
        huggingFaceModels = listOf(HuggingFaceModel.default.alias),
        localCatalogStatuses = LocalAiModel.Type.entries.associateWith { LocalCatalogStatus.READY },
        localMnnModels = models,
        localOnnxModels = models,
        localMediaPipeModels = models,
        localSdxlModels = models,
        localCoreMlModels = models,
        localBonsaiModels = models,
    )

    /**
     * Selects a configuration form with a platform-compatible provider catalog.
     *
     * @param provider Provider whose actual form should be rendered.
     * @author Dmitriy Moroz
     */
    fun configure(provider: ServerSource): ServerSetupState {
        val platform =
            if (provider == ServerSource.LOCAL_APPLE_CORE_ML) Platform.IOS else Platform.ANDROID
        return source.copy(
            step = ServerSetupState.Step.CONFIGURE, mode = provider, platform = platform,
            allowedModes = ServerSource.entries.filter { platform in it.allowedPlatforms && BuildType.FULL in it.allowedInBuilds },
        )
    }
}
