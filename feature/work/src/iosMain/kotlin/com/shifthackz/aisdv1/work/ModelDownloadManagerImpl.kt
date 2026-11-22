package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.Foundation.NSUUID

/**
 * Owns concurrent single downloads or an exclusive batch across setup navigation.
 * Each job waits for native execution admission and owns its system progress and cancellation.
 * Transfers retain the normal catalog activation path rather than a separate background URL session.
 *
 * @param downloadModels Sequential policy within each batch or single model.
 * @param execution Shared native work admission, progress presentation and expiration handling.
 * @param scope Application-owned scope that outlives setup view models.
 * @author Dmitriy Moroz
 */
internal class ModelDownloadManagerImpl(
    private val downloadModels: DownloadModelsUseCase,
    private val execution: IosWorkExecution,
    private val scope: CoroutineScope,
) : ModelDownloadManager {
    private val state = MutableStateFlow<List<ModelDownloadBatch>>(emptyList())
    override val batches = state.asStateFlow()
    private val jobs = mutableMapOf<Pair<LocalAiModel.Type, String?>, Job>()
    private val lifecycleMutex = Mutex()

    /**
     * Starts independent model work or joins cancelled jobs before a confirmed provider replacement.
     *
     * @param request Captured provider and optional single-model mirror.
     * @param shouldReplace Whether the user confirmed stopping existing transfers.
     * @author Dmitriy Moroz
     */
    override suspend fun start(request: ModelDownloadRequest, shouldReplace: Boolean) =
        lifecycleMutex.withLock {
            val key = request.type to request.modelId
            val active = state.value.filter { it.isRunning }
            if (shouldReplace) {
                jobs.values.forEach { it.cancelAndJoin() }
            } else if (active.any { it.isBatch } || (request.isBatch && active.isNotEmpty()) || jobs[key]?.isCompleted == false) {
                return@withLock
            }
            jobs.entries.removeAll { it.value.isCompleted }
            val workId = NSUUID().UUIDString
            val initial = ModelDownloadBatch(
                request.type,
                modelId = request.modelId,
                isBatch = request.isBatch,
                workId = workId
            )
            state.update { snapshots ->
                snapshots.filterNot { it.type == request.type && (if (request.isBatch) it.isBatch else !it.isBatch && it.modelId == request.modelId) } +
                        initial
            }
            val job = scope.launch(start = CoroutineStart.LAZY) {
                val activeJob = currentCoroutineContext()[Job]
                val title = Localization.string(if (request.isBatch) "download_all" else "download")
                var latest = initial
                var isSuccess = false
                var isCancelled = false
                try {
                    val admission = CompletableDeferred<Unit>()
                    execution.begin(
                        ownerId = workId,
                        title = title,
                        subtitle = Localization.string("loading"),
                        requiresGpu = false,
                        onExpiration = { activeJob?.cancel() },
                        onReady = { admission.complete(Unit) },
                        onUnavailable = {
                            admission.completeExceptionally(
                                IllegalStateException(Localization.string("error_download_fail"))
                            )
                        },
                    )
                    admission.await()
                    downloadModels(request).collect { snapshot ->
                        latest = snapshot.copy(workId = workId)
                        publish(latest)
                        execution.update(
                            workId,
                            title,
                            latest.modelName ?: Localization.string("loading"),
                            latest.totalProgress.takeIf { latest.totalModelCount > 0 },
                        )
                    }
                    check(!latest.isRunning && latest.progress !is DownloadState.Error) {
                        Localization.string("error_download_fail")
                    }
                    isSuccess = true
                } catch (cancelled: CancellationException) {
                    isCancelled = true
                    latest = latest.copy(progress = DownloadState.Unknown, isRunning = false)
                    publish(latest)
                    throw cancelled
                } catch (failure: Exception) {
                    latest = latest.copy(progress = DownloadState.Error(failure), isRunning = false)
                    publish(latest)
                } finally {
                    execution.end(
                        workId,
                        isSuccess,
                        Localization.string(
                            when {
                                isSuccess -> "notification_download_finish_title"
                                isCancelled -> "notification_download_cancel_title"
                                else -> "notification_download_fail_title"
                            }
                        ),
                        latest.modelName ?: Localization.string("notification_finish_sub_title"),
                    )
                }
            }
            jobs[key] = job
            job.start()
            Unit
        }

    /**
     * Publishes only into the captured work entry so late cleanup cannot overwrite a replacement.
     *
     * @param snapshot Latest state retaining this operation's immutable platform work ID.
     * @author Dmitriy Moroz
     */
    private fun publish(snapshot: ModelDownloadBatch) {
        state.update { snapshots ->
            snapshots.map { if (it.workId == snapshot.workId) snapshot else it }
        }
    }

    /**
     * Cancels matching jobs without affecting other models' transfers or installed files.
     *
     * @param type Provider to cancel, or null for every model request.
     * @param modelId Model within that provider, or null for all its requests.
     * @author Dmitriy Moroz
     */
    override suspend fun cancel(type: LocalAiModel.Type?, modelId: String?) =
        lifecycleMutex.withLock {
            require(modelId == null || type != null)
            jobs.filterKeys { (provider, id) -> (type == null || provider == type) && (modelId == null || id == modelId) }
                .values.forEach { it.cancel() }
        }
}
