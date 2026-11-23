package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Supplies local-runtime forms, catalog lifecycle, custom paths and single/batch download controls.
 * Sample paths are never opened and download callbacks remain inert.
 *
 * @author Dmitriy Moroz
 */
internal class LocalSetupPreviewParameterProvider :
    PreviewParameterProvider<ServerSetupPreviewCase> {
    private val mnn = ConfigurationPreviewFixtures.configure(ServerSource.LOCAL_MNN)
    private val cases = ServerSource.entries.filter { it.type == ServerSourceType.LOCAL }.map {
        ServerSetupPreviewCase("Catalog · ${it.name}", ConfigurationPreviewFixtures.configure(it))
    } + listOf(
        ServerSetupPreviewCase(
            "Initial catalog loading", mnn.copy(
                localMnnModels = emptyList(),
                localCatalogStatuses = mapOf(LocalAiModel.Type.Mnn to LocalCatalogStatus.LOADING),
            )
        ),
        ServerSetupPreviewCase("Empty catalog", mnn.copy(localMnnModels = emptyList())),
        ServerSetupPreviewCase(
            "Catalog unavailable", mnn.copy(
                localMnnModels = emptyList(),
                localCatalogStatuses = mapOf(LocalAiModel.Type.Mnn to LocalCatalogStatus.ERROR),
            )
        ),
        ServerSetupPreviewCase(
            "Failed refresh with cached rows", mnn.copy(
                localCatalogStatuses = mapOf(LocalAiModel.Type.Mnn to LocalCatalogStatus.ERROR),
            )
        ),
        ServerSetupPreviewCase(
            "Refreshing cached catalog",
            mnn.copy(refreshingLocalCatalogs = setOf(LocalAiModel.Type.Mnn))
        ),
        ServerSetupPreviewCase(
            "Installed model selected",
            mnn.copy(localMnnModels = listOf(ConfigurationPreviewFixtures.downloaded))
        ),
        ServerSetupPreviewCase(
            "No model selected",
            mnn.copy(localMnnModels = ConfigurationPreviewFixtures.models.map { it.copy(selected = false) })
        ),
        ServerSetupPreviewCase("Lower download rows", mnn, formOffset = 550),
        ServerSetupPreviewCase("Custom paths disabled", mnn.copy(allowLocalCustomModels = false)),
        ServerSetupPreviewCase(
            "Batch downloading", mnn.copy(
                modelDownloadBatches = listOf(
                    ModelDownloadBatch(
                        type = LocalAiModel.Type.Mnn,
                        modelId = ConfigurationPreviewFixtures.model.id,
                        modelName = ConfigurationPreviewFixtures.model.name,
                        totalModelCount = 3,
                        completedIds = listOf(ConfigurationPreviewFixtures.downloaded.id),
                        progress = DownloadState.Downloading(42),
                    ),
                )
            )
        ),
        ServerSetupPreviewCase(
            "Single downloading", mnn.copy(
                modelDownloadBatches = listOf(
                    ModelDownloadBatch(
                        type = LocalAiModel.Type.Mnn,
                        modelId = ConfigurationPreviewFixtures.model.id,
                        modelName = ConfigurationPreviewFixtures.model.name,
                        totalModelCount = 1,
                        isBatch = false,
                        progress = DownloadState.Downloading(68)
                    ),
                )
            )
        ),
        ServerSetupPreviewCase("iOS MNN catalog", mnn.copy(platform = Platform.IOS)),
    ) + listOf(
        ServerSource.LOCAL_MNN, ServerSource.LOCAL_MICROSOFT_ONNX,
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE, ServerSource.LOCAL_STABLE_DIFFUSION_CPP
    ).flatMap { provider ->
        val custom = ConfigurationPreviewFixtures.configure(provider).copy(
            isLocalMnnCustomModel = true, localOnnxCustomModel = true,
            localMediaPipeCustomModel = true, localSdxlCustomModel = true,
            localMnnCustomModelPath = "/storage/emulated/0/Download/sample-model",
            localOnnxCustomModelPath = "/storage/emulated/0/Download/sample-model",
            localMediaPipeCustomModelPath = "/storage/emulated/0/Download/sample-model",
            localSdxlCustomModelPath = "/storage/emulated/0/Download/sample-model",
        )
        listOf(
            ServerSetupPreviewCase("Custom path · ${provider.name}", custom),
            ServerSetupPreviewCase(
                "Custom validation · ${provider.name}", custom.copy(
                    localCustomMnnPathValidationError = ServerSetupState.ValidationError.EmptyField,
                    localCustomOnnxPathValidationError = ServerSetupState.ValidationError.EmptyField,
                    localCustomMediaPipePathValidationError = ServerSetupState.ValidationError.EmptyField,
                    localCustomSdxlPathValidationError = ServerSetupState.ValidationError.EmptyField,
                ), formOffset = 200
            ),
        )
    }

    /**
     * Named local form and catalog states rendered in both palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ServerSetupPreviewCase> = cases.asSequence()

    /**
     * Identifies the runtime, catalog state or custom-model presentation.
     *
     * @param index Position in the ordered local-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
