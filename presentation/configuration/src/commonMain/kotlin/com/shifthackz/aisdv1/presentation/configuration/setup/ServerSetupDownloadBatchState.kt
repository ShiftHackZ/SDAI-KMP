package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch

/**
 * Restores worker progress without replaying old successful installs over current file state.
 * @param batches Worker snapshots, including terminal work retained for transition detection.
 * @author Dmitriy Moroz
 */
internal fun ServerSetupState.withModelDownloadBatches(batches: List<ModelDownloadBatch>): ServerSetupState {
    var result = copy(modelDownloadBatches = batches)
    modelDownloadBatches.filter { it.isRunning }.forEach { previous ->
        val model = result.catalogModels(previous.type).firstOrNull { it.id == previous.modelId }
        if (model != null && model.downloadState is DownloadState.Downloading) result =
            result.withUpdatedLocalModel(
                model.copy(downloadState = DownloadState.Unknown),
                previous.type
            )
    }
    batches.filter { it.isRunning }.forEach { batch ->
        batch.completedIds.forEach { id -> result = result.withVerifiedOwnedModel(id, batch.type) }
        val model = result.catalogModels(batch.type).firstOrNull { it.id == batch.modelId }
        if (model != null) result = result.withUpdatedLocalModel(
            model.copy(
                downloadState = batch.progress ?: DownloadState.Downloading(),
                downloaded = model.downloaded || batch.progress is DownloadState.Complete,
            ), batch.type
        )
    }
    return result
}
