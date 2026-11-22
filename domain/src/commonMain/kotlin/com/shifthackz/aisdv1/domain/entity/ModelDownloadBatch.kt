package com.shifthackz.aisdv1.domain.entity

/**
 * Snapshot of a sequential provider download, retained while setup is closed.
 *
 * @param type Provider owning the batch.
 * @param modelId Model currently transferring or most recently completed.
 * @param progress Current transfer result; null before the first transfer.
 * @param completedIds Models installed successfully in this batch.
 * @param modelName Catalog name shown in background progress notifications.
 * @param totalModelCount Number of missing models included at batch start; each contributes equally to total progress.
 * @param isBatch Whether this work downloads a whole provider catalog or one selected model.
 * @param isRunning Whether the worker still owns the batch.
 * @param workId Platform work identity distinguishing retries of the same model.
 * @author Dmitriy Moroz
 */
data class ModelDownloadBatch(
    val type: LocalAiModel.Type,
    val modelId: String? = null,
    val progress: DownloadState? = null,
    val completedIds: List<String> = emptyList(),
    val isRunning: Boolean = true,
    val modelName: String? = null,
    val totalModelCount: Int = 0,
    val isBatch: Boolean = true,
    val workId: String = "",
) {
    /**
     * Whole-batch percent; completed installs plus the active model's fraction of an equal share.
     *
     * @author Dmitriy Moroz
     */
    val totalProgress: Int
        get() {
            if (totalModelCount == 0) return if (isRunning) 0 else 100
            val activePercent = (progress as? DownloadState.Downloading)?.percent ?: 0
            return ((completedIds.size * 100 + activePercent) / totalModelCount).coerceIn(0, 100)
        }
}
