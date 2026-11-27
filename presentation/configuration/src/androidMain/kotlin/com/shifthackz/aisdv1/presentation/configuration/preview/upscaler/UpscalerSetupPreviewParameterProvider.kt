package com.shifthackz.aisdv1.presentation.configuration.preview.upscaler

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.configuration.preview.upscaler.UpscalerSetupPreviewCase.Kind
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerDiscoveryState
import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerSetupState

/**
 * Fixed provider discovery, catalog, download and confirmation states for upscaler configuration.
 *
 * @author Dmitriy Moroz
 */
internal class UpscalerSetupPreviewParameterProvider :
    PreviewParameterProvider<UpscalerSetupPreviewCase> {
    private val model = LocalAiModel(
        id = "upscaler_esrgan_anime", type = LocalAiModel.Type.Upscaler,
        name = "Real-ESRGAN Anime 6B", size = "17 MB", sources = emptyList(), downloaded = true,
    )
    private val mnn = model.copy(id = "upscaler_mnn_anime", name = "Real-ESRGAN Anime 6B (MNN)")
    private val source = UpscalerSetupState(
        platform = Platform.ANDROID, isLoading = false, catalogStatus = LocalCatalogStatus.READY,
        models = listOf(
            model,
            model.copy(
                id = "upscaler_esrgan_x4plus",
                name = "Real-ESRGAN x4plus",
                size = "64 MB",
                downloaded = false
            ),
            mnn
        ),
        selectedId = model.id,
    )
    private val setup = source.copy(step = ServerSetupState.Step.CONFIGURE)
    private val filtered = source.copy(
        discovery = UpscalerDiscoveryState(
            type = ServerSourceType.LOCAL, readiness = setOf(ServerSourceReadiness.STABLE),
            tags = setOf(UpscalerFeatureTag.MultipleModels),
        )
    )
    private val cases = listOf(
        UpscalerSetupPreviewCase("Discovery · ESRGAN selected", source),
        UpscalerSetupPreviewCase(
            "Discovery · MNN selected",
            source.copy(source = UpscalerSource.LOCAL_MNN)
        ),
        UpscalerSetupPreviewCase(
            "Discovery · iOS metadata",
            source.copy(platform = Platform.IOS, source = UpscalerSource.LOCAL_MNN)
        ),
        UpscalerSetupPreviewCase(
            "Search · MNN",
            source.copy(discovery = UpscalerDiscoveryState(query = "MNN"))
        ),
        UpscalerSetupPreviewCase(
            "Search · No matches",
            source.copy(discovery = UpscalerDiscoveryState(query = "no matching provider"))
        ),
        UpscalerSetupPreviewCase("Discovery · Filtered", filtered),
        UpscalerSetupPreviewCase("Setup · ESRGAN", setup),
        UpscalerSetupPreviewCase(
            "Setup · MNN",
            setup.copy(source = UpscalerSource.LOCAL_MNN, selectedId = mnn.id)
        ),
        UpscalerSetupPreviewCase(
            "Setup · Catalog loading",
            setup.copy(
                models = emptyList(),
                isLoading = true,
                catalogStatus = LocalCatalogStatus.LOADING
            )
        ),
        UpscalerSetupPreviewCase("Setup · Empty catalog", setup.copy(models = emptyList())),
        UpscalerSetupPreviewCase(
            "Setup · Catalog error",
            setup.copy(models = emptyList(), catalogStatus = LocalCatalogStatus.ERROR)
        ),
        UpscalerSetupPreviewCase(
            "Setup · Cached model and refresh error",
            setup.copy(catalogStatus = LocalCatalogStatus.ERROR)
        ),
        UpscalerSetupPreviewCase(
            "Setup · Download required",
            setup.copy(models = listOf(model.copy(downloaded = false)), selectedId = "")
        ),
        UpscalerSetupPreviewCase(
            "Setup · Downloading 45 percent",
            setup.copy(
                models = listOf(model.copy(downloaded = false)),
                downloads = listOf(
                    ModelDownloadBatch(
                        LocalAiModel.Type.Upscaler,
                        model.id,
                        DownloadState.Downloading(45)
                    )
                )
            )
        ),
        UpscalerSetupPreviewCase(
            "Setup · Download failed",
            setup.copy(
                models = listOf(model.copy(downloaded = false)),
                downloads = listOf(
                    ModelDownloadBatch(
                        LocalAiModel.Type.Upscaler,
                        model.id,
                        DownloadState.Error(IllegalStateException("Download interrupted")),
                        isRunning = false
                    )
                )
            )
        ),
        UpscalerSetupPreviewCase(
            "Setup · Long model name",
            setup.copy(models = listOf(model.copy(name = "Real-ESRGAN detailed illustration restoration and super-resolution model")))
        ),
        UpscalerSetupPreviewCase(
            "Setup · Operation error dialog",
            setup.copy(error = "The model could not be installed. Check available storage and try again.")
        ),
        UpscalerSetupPreviewCase(
            "Setup · Delete confirmation",
            setup.copy(pendingDeleteId = model.id)
        ),
        UpscalerSetupPreviewCase("Filters · Default", source, Kind.FILTERS),
        UpscalerSetupPreviewCase("Filters · Selected intersections", filtered, Kind.FILTERS),
        UpscalerSetupPreviewCase(
            "Filters · iOS availability",
            source.copy(platform = Platform.IOS, source = UpscalerSource.LOCAL_MNN),
            Kind.FILTERS
        ),
        *ServerSetupState.SourceSortOrder.entries.map {
            UpscalerSetupPreviewCase(
                "Sort · ${it.name}",
                source.copy(discovery = UpscalerDiscoveryState(sortOrder = it)),
                Kind.SORT
            )
        }.toTypedArray(),
        UpscalerSetupPreviewCase("Provider row · ESRGAN selected", source, Kind.PROVIDER),
        UpscalerSetupPreviewCase(
            "Provider row · ESRGAN unselected",
            source,
            Kind.PROVIDER,
            isSelected = false
        ),
        UpscalerSetupPreviewCase(
            "Provider row · MNN Android",
            source.copy(source = UpscalerSource.LOCAL_MNN),
            Kind.PROVIDER
        ),
        UpscalerSetupPreviewCase(
            "Provider row · MNN iOS metadata",
            source.copy(platform = Platform.IOS, source = UpscalerSource.LOCAL_MNN),
            Kind.PROVIDER
        ),
    )

    /**
     * Snapshots consumed by the real wizard, row and sheet renderers with inert intents.
     *
     * @author Dmitriy Moroz
     */
    override val values = cases.asSequence()

    /**
     * Names the configuration surface and its visible operation state.
     *
     * @param index Position in the ordered preview list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
