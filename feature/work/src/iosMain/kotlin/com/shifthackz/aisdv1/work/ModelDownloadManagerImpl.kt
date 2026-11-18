package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelsUseCase
import kotlinx.coroutines.CancellationException
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
 * Each job receives finite iOS execution time, without durable background transfers.
 * @param downloadModels Sequential policy within each batch or single model.
 * @param scope Application-owned scope that outlives setup view models.
 * @author Dmitriy Moroz
 */
internal class ModelDownloadManagerImpl(
    private val downloadModels: DownloadModelsUseCase,
    private val scope: CoroutineScope,
) : ModelDownloadManager {
    private val state = MutableStateFlow<List<ModelDownloadBatch>>(emptyList())
    override val batches = state.asStateFlow()
    private val jobs = mutableMapOf<Pair<LocalAiModel.Type, String?>, Job>()
    private val lifecycleMutex = Mutex()

    /**
     * Starts independent model work or joins cancelled jobs before a confirmed provider replacement.
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
            state.update { snapshots ->
                snapshots.filterNot { it.type == request.type && (if (request.isBatch) it.isBatch else !it.isBatch && it.modelId == request.modelId) } +
                        ModelDownloadBatch(
                            request.type,
                            modelId = request.modelId,
                            isBatch = request.isBatch,
                            workId = workId
                        )
            }
            val job = scope.launch(start = CoroutineStart.LAZY) {
                val activeJob = currentCoroutineContext()[Job]
                val background = IosBackgroundExecution("Model downloads") { activeJob }
                try {
                    background.begin()
                    downloadModels(request).collect { snapshot ->
                        state.update { snapshots ->
                            snapshots.map {
                                if (it.workId == workId) snapshot.copy(
                                    workId = workId
                                ) else it
                            }
                        }
                    }
                } catch (cancelled: CancellationException) {
                    state.update { snapshots ->
                        snapshots.map {
                            if (it.workId == workId) it.copy(
                                progress = DownloadState.Unknown,
                                isRunning = false
                            ) else it
                        }
                    }
                    throw cancelled
                } catch (failure: Exception) {
                    state.update { snapshots ->
                        snapshots.map {
                            if (it.workId == workId) it.copy(
                                progress = DownloadState.Error(failure),
                                isRunning = false
                            ) else it
                        }
                    }
                } finally {
                    background.end()
                }
            }
            jobs[key] = job
            job.start()
            Unit
        }

    /**
     * Cancels matching jobs without affecting other models' transfers or installed files.
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
