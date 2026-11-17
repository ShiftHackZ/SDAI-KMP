package com.shifthackz.aisdv1.presentation.screen.setup.model

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Resolves a local runtime catalog without changing the selected provider.
 *
 * @author Dmitriy Moroz
 */
internal val ServerSource.localCatalogType: LocalAiModel.Type?
    get() = when (this) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> LocalAiModel.Type.ONNX
        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> LocalAiModel.Type.MediaPipe
        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> LocalAiModel.Type.Sdxl
        ServerSource.LOCAL_APPLE_CORE_ML -> LocalAiModel.Type.CoreMl
        ServerSource.LOCAL_APPLE_BONSAI -> LocalAiModel.Type.Bonsai
        ServerSource.LOCAL_MNN -> LocalAiModel.Type.Mnn
        else -> null
    }

/**
 * Reads one source-owned list, including pending setup selection and download state.
 *
 * @param type Runtime whose list is requested independently of the visible mode.
 *
 * @author Dmitriy Moroz
 */
internal fun ServerSetupState.catalogModels(type: LocalAiModel.Type): List<ServerSetupState.LocalModel> =
    when (type) {
        LocalAiModel.Type.ONNX -> localOnnxModels
        LocalAiModel.Type.MediaPipe -> localMediaPipeModels
        LocalAiModel.Type.Sdxl -> localSdxlModels
        LocalAiModel.Type.CoreMl -> localCoreMlModels
        LocalAiModel.Type.Bonsai -> localBonsaiModels
        LocalAiModel.Type.Mnn -> localMnnModels
    }

/**
 * Merges refreshed metadata without overwriting pending selection or in-flight download state.
 * Previously visible entries remain available when a refresh fails; a custom entry is never evidence of a remote catalog.
 *
 * @param type Runtime owning this completion, regardless of the currently visible provider.
 * @param models Fresh or cached domain choices, or null when cache reading also failed.
 * @param status Completed refresh state exposed next to the affected catalog.
 *
 * @author Dmitriy Moroz
 */
internal fun ServerSetupState.withCatalogResult(
    type: LocalAiModel.Type,
    models: List<LocalAiModel>?,
    status: LocalCatalogStatus
): ServerSetupState {
    val previous = catalogModels(type)
    val hasPendingSelection = localCatalogSelectionOverrides.containsKey(type)
    val selectedId =
        if (hasPendingSelection) localCatalogSelectionOverrides[type] else previous.firstOrNull { it.selected }?.id
    val incoming = models?.mapToCommonSetupModels() ?: previous
    val activeDownloads = localCatalogActiveDownloads[type].orEmpty()
    val retained = previous.filter { old ->
        incoming.none { it.id == old.id } &&
                (status == LocalCatalogStatus.ERROR || old.id in activeDownloads)
    }
    val merged = (incoming + retained).map { model ->
        val old = previous.firstOrNull { it.id == model.id }
        val wasDeleted = model.id in localCatalogDeletedModelIds[type].orEmpty()
        val isDownloadActive = model.id in activeDownloads
        model.copy(
            selected = if (hasPendingSelection) model.id == selectedId else selectedId?.let { it == model.id }
                ?: model.selected,
            downloadState = when {
                isDownloadActive -> old?.downloadState as? DownloadState.Downloading
                    ?: DownloadState.Downloading()

                wasDeleted -> DownloadState.Unknown
                model.downloaded && old?.downloadState is DownloadState.Downloading -> DownloadState.Unknown
                else -> old?.downloadState?.takeUnless { it == DownloadState.Unknown }
                    ?: model.downloadState
            },
            downloaded = when {
                isDownloadActive || wasDeleted -> false
                old?.downloadState is DownloadState.Complete -> old.downloaded
                else -> model.downloaded
            },
        )
    }
    val result = when (type) {
        LocalAiModel.Type.ONNX -> copy(localOnnxModels = merged)
        LocalAiModel.Type.MediaPipe -> copy(localMediaPipeModels = merged)
        LocalAiModel.Type.Sdxl -> copy(localSdxlModels = merged)
        LocalAiModel.Type.CoreMl -> copy(localCoreMlModels = merged)
        LocalAiModel.Type.Bonsai -> copy(localBonsaiModels = merged)
        LocalAiModel.Type.Mnn -> copy(localMnnModels = merged)
    }
    return result.copy(localCatalogStatuses = result.localCatalogStatuses + (type to status))
}

/**
 * Recovers a completed owned install after a transfer has joined but its terminal emission was lost.
 * The owning file-store read must confirm completion before this transition; partial directory observations during active work are insufficient.
 *
 * @param id Model identity whose owned payload was verified after joining the transfer.
 * @param type Provider owning the completed payload and its deletion/transfer markers.
 *
 * @author Dmitriy Moroz
 */
internal fun ServerSetupState.withVerifiedOwnedModel(
    id: String,
    type: LocalAiModel.Type
): ServerSetupState {
    val current = catalogModels(type).firstOrNull { it.id == id } ?: return this
    val result = withUpdatedLocalModel(
        current.copy(
            downloaded = true,
            downloadState = DownloadState.Unknown
        ), type
    )
    return result.copy(
        localCatalogDeletedModelIds = result.localCatalogDeletedModelIds +
                (type to (result.localCatalogDeletedModelIds[type].orEmpty() - id))
    )
}
