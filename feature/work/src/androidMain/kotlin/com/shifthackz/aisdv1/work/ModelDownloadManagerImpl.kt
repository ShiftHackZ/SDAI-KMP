package com.shifthackz.aisdv1.work

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.work.di.WorkManagerProvider
import com.shifthackz.aisdv1.work.task.DownloadModelsTask
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Persists independent single downloads or one exclusive sequential provider batch.
 * Each model has its own unique work and notification; duplicates retain the running transfer.
 * @param workManager Application WorkManager bridge.
 * @param dispatchersProvider Keeps durable scheduling and cancellation waits off the main thread.
 * @author Dmitriy Moroz
 */
internal class ModelDownloadManagerImpl(
    private val workManager: WorkManagerProvider,
    private val dispatchersProvider: DispatchersProvider,
) :
    ModelDownloadManager {
    private val scheduling = Mutex()

    override val batches =
        workManager().getWorkInfosByTagFlow(DownloadModelsTask.WORK_NAME).map { infos ->
            infos.groupBy { info ->
                info.tags.firstOrNull { it.startsWith("download:") } ?: info.id.toString()
            }
                .values.mapNotNull { group ->
                    val info =
                        group.firstOrNull { !it.state.isFinished } ?: group.maxBy { candidate ->
                            candidate.tags.firstOrNull { it.startsWith("enqueued:") }
                                ?.removePrefix("enqueued:")?.toLongOrNull() ?: 0L
                        }
                    val data = if (info.state.isFinished) info.outputData else info.progress
                    val type = data.getString("type")
                        ?: info.tags.firstOrNull { it.startsWith("provider:") }
                            ?.removePrefix("provider:") ?: return@mapNotNull null
                    val modelId =
                        data.getString("model") ?: info.tags.firstOrNull { it.startsWith("model:") }
                            ?.removePrefix("model:")
                    val progress = when {
                        info.state == WorkInfo.State.CANCELLED -> DownloadState.Unknown
                        data.getString("error") != null -> DownloadState.Error(
                            IllegalStateException(
                                data.getString("error")
                            )
                        )

                        info.state == WorkInfo.State.FAILED -> DownloadState.Error(
                            IllegalStateException("Model download failed")
                        )

                        data.getString("path") != null -> DownloadState.Complete(
                            data.getString("path").orEmpty()
                        )

                        !info.state.isFinished && modelId != null -> DownloadState.Downloading(
                            data.getInt(
                                "percent",
                                0
                            )
                        )

                        else -> null
                    }
                    ModelDownloadBatch(
                        type = LocalAiModel.Type.parse(type),
                        modelId = modelId,
                        progress = progress,
                        completedIds = data.getStringArray("completed")?.toList().orEmpty(),
                        isRunning = !info.state.isFinished,
                        modelName = data.getString("name"),
                        totalModelCount = data.getInt("total_count", 0),
                        isBatch = "mode:single" !in info.tags,
                        workId = info.id.toString(),
                    )
                }
        }

    /**
     * Admits distinct single models concurrently or replaces all transfers with a confirmed batch.
     * @param request Provider and optional model mirror captured by setup.
     * @param shouldReplace Whether to cancel previous work before admitting this request.
     * @author Dmitriy Moroz
     */
    override suspend fun start(request: ModelDownloadRequest, shouldReplace: Boolean) =
        withContext(dispatchersProvider.io) {
            scheduling.withLock {
                val active =
                    workManager().getWorkInfosByTagFlow(DownloadModelsTask.WORK_NAME).first()
                        .filterNot { it.state.isFinished }
                if (shouldReplace) {
                    workManager().cancelAllWorkByTag(DownloadModelsTask.WORK_NAME).result.get()
                } else if (active.any { "mode:single" !in it.tags } || (request.isBatch && active.isNotEmpty())) {
                    return@withLock
                }
                val name =
                    "${DownloadModelsTask.WORK_NAME}:${request.type.key}:${request.modelId ?: "all"}"
                workManager().enqueueUniqueWork(
                    name,
                    if (shouldReplace) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
                    OneTimeWorkRequestBuilder<DownloadModelsTask>()
                        .addTag(DownloadModelsTask.WORK_NAME).addTag("download:$name")
                        .addTag("enqueued:${System.currentTimeMillis()}")
                        .addTag("provider:${request.type.key}")
                        .addTag(if (request.isBatch) "mode:all" else "mode:single")
                        .apply { request.modelId?.let { addTag("model:$it") } }
                        .setInputData(
                            workDataOf(
                                "type" to request.type.key,
                                "model" to request.modelId,
                                "url" to request.sourceUrl
                            )
                        )
                        .build()
                ).result.get()
                Unit
            }
        }

    /**
     * Cancels only matching requests; other concurrent model workers keep their progress.
     * @param type Provider to cancel, or null to stop every model request.
     * @param modelId Single model within that provider, or null for all its requests.
     * @author Dmitriy Moroz
     */
    override suspend fun cancel(type: LocalAiModel.Type?, modelId: String?) =
        withContext(dispatchersProvider.io) {
            scheduling.withLock {
                require(modelId == null || type != null)
                if (modelId != null) {
                    workManager().cancelUniqueWork("${DownloadModelsTask.WORK_NAME}:${type?.key}:$modelId").result.get()
                } else {
                    workManager().cancelAllWorkByTag(type?.let { "provider:${it.key}" }
                        ?: DownloadModelsTask.WORK_NAME).result.get()
                }
                Unit
            }
        }
}
