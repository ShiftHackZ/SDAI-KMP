package com.shifthackz.aisdv1.presentation.screen.setup.model

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks shared catalog completion against pending selection and downloads for every runtime family.
 *
 * @author Dmitriy Moroz
 */
class ServerSetupCatalogStateTest {
    @Test
    fun `all catalog completions retain pending selection and download progress without changing mode`() {
        LocalAiModel.Type.entries.forEach { type ->
            val domain = LocalAiModel(
                "catalog",
                type,
                "Model",
                "1 GB",
                listOf("https://example.com/model.zip"),
                selected = true
            )
            val state = ServerSetupState(mode = ServerSource.AUTOMATIC1111)
                .withCatalogResult(type, listOf(domain), LocalCatalogStatus.READY)
            val current = state.catalogModels(type).single()
                .copy(downloadState = DownloadState.Downloading(35))
            val downloading = when (type) {
                LocalAiModel.Type.ONNX -> state.copy(localOnnxModels = listOf(current))
                LocalAiModel.Type.MediaPipe -> state.copy(localMediaPipeModels = listOf(current))
                LocalAiModel.Type.Sdxl -> state.copy(localSdxlModels = listOf(current))
                LocalAiModel.Type.CoreMl -> state.copy(localCoreMlModels = listOf(current))
                LocalAiModel.Type.Bonsai -> state.copy(localBonsaiModels = listOf(current))
                LocalAiModel.Type.Mnn -> state.copy(localMnnModels = listOf(current))
            }
            val refreshed = downloading.withCatalogResult(
                type,
                listOf(domain.copy(selected = false)),
                LocalCatalogStatus.READY
            )
            assertEquals(true, refreshed.catalogModels(type).single().selected)
            assertEquals(
                DownloadState.Downloading(35),
                refreshed.catalogModels(type).single().downloadState
            )
            assertEquals(ServerSource.AUTOMATIC1111, refreshed.mode)
            val failed = refreshed.withCatalogResult(type, emptyList(), LocalCatalogStatus.ERROR)
            assertEquals(refreshed.catalogModels(type), failed.catalogModels(type))
            assertEquals(LocalCatalogStatus.ERROR, failed.localCatalogStatuses[type])
            val deleted =
                refreshed.withDeletedLocalModel(refreshed.catalogModels(type).single(), type)
                    .withCatalogResult(
                        type,
                        listOf(domain.copy(downloaded = true)),
                        LocalCatalogStatus.READY
                    )
            assertEquals(false, deleted.catalogModels(type).single().selected)
            assertEquals(false, deleted.catalogModels(type).single().downloaded)
        }
    }

    @Test
    fun `explicit custom toggle in both directions wins over saved catalog selection`() {
        listOf(
            ServerSource.LOCAL_MNN to LocalAiModel.CustomMnn,
            ServerSource.LOCAL_MICROSOFT_ONNX to LocalAiModel.CustomOnnx,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE to LocalAiModel.CustomMediaPipe,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP to LocalAiModel.CustomSdxl,
        ).forEach { (source, custom) ->
            val saved = custom.copy(selected = true)
            val state = ServerSetupState(mode = source).withCatalogResult(
                custom.type,
                listOf(saved),
                LocalCatalogStatus.READY
            )
            val disabled = state.withAllowCustomModel(false)
                .withCatalogResult(custom.type, listOf(saved), LocalCatalogStatus.READY)
            assertEquals(false, disabled.catalogModels(custom.type).single().selected)
            val enabled = disabled.withAllowCustomModel(true)
                .withCatalogResult(custom.type, listOf(custom), LocalCatalogStatus.READY)
            assertEquals(true, enabled.catalogModels(custom.type).single().selected)
        }
    }

    @Test
    fun `active downloads cannot become available from partial directory cache observations`() {
        LocalAiModel.Type.entries.forEach { type ->
            val model = LocalAiModel("active-${type.key}", type, "Model", "1 GB", emptyList())
            val state =
                ServerSetupState().withCatalogResult(type, listOf(model), LocalCatalogStatus.READY)
            val active = state.withUpdatedLocalModel(
                state.catalogModels(type).single()
                    .copy(downloadState = DownloadState.Downloading(45)), type
            )
            val refreshed = active.withCatalogResult(
                type,
                listOf(model.copy(downloaded = true)),
                LocalCatalogStatus.READY
            )
            assertEquals(false, refreshed.catalogModels(type).single().downloaded)
            assertEquals(
                DownloadState.Downloading(45),
                refreshed.catalogModels(type).single().downloadState
            )
            val removed =
                refreshed.withDeletedLocalModel(refreshed.catalogModels(type).single(), type)
                    .withCatalogResult(
                        type,
                        listOf(model.copy(downloaded = true)),
                        LocalCatalogStatus.READY
                    )
            assertEquals(false, removed.catalogModels(type).single().downloaded)
            assertEquals(DownloadState.Unknown, removed.catalogModels(type).single().downloadState)
        }
    }

    @Test
    fun `verified post join install clears earlier deletion and active transfer markers`() {
        val type = LocalAiModel.Type.Mnn
        val model = LocalAiModel.CustomMnn.copy(id = "mnn-reinstalled", downloaded = true)
        val state =
            ServerSetupState().withCatalogResult(type, listOf(model), LocalCatalogStatus.READY)
        val deleted = state.withDeletedLocalModel(state.catalogModels(type).single(), type)
        val active = deleted.withUpdatedLocalModel(
            deleted.catalogModels(type).single()
                .copy(downloadState = DownloadState.Downloading(99)), type
        )
        val recovered = active.withVerifiedOwnedModel(model.id, type)
            .withCatalogResult(type, listOf(model), LocalCatalogStatus.READY)
        assertEquals(true, recovered.catalogModels(type).single().downloaded)
        assertEquals(DownloadState.Unknown, recovered.catalogModels(type).single().downloadState)
        assertEquals(emptySet<String>(), recovered.localCatalogDeletedModelIds[type])
        assertEquals(emptySet<String>(), recovered.localCatalogActiveDownloads[type])
    }

}
