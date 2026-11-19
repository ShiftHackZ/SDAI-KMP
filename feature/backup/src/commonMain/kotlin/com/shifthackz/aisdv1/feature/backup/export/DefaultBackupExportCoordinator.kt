package com.shifthackz.aisdv1.feature.backup.export

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportIntent
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportOperation
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportStatus
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import com.shifthackz.aisdv1.domain.usecase.backup.ExportBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps destination copying and its durable association alive when a screen/activity is recreated.
 * @param archives Strict app-owned archive creation, source resolution and cleanup.
 * @param records Dedicated checked nonsecret operation persistence.
 * @param export Strict complete-byte destination writer.
 * @param dispatchers Application IO dispatcher owned by this graph-scoped coordinator.
 * @author Dmitriy Moroz
 */
internal class DefaultBackupExportCoordinator(
    private val archives: BackupArchiveStore,
    private val records: BackupExportStateStore,
    private val export: ExportBackupUseCase,
    private val dispatchers: DispatchersProvider,
) : BackupExportCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io)
    private val serialization = Mutex()
    private val operation = MutableStateFlow(BackupExportOperation())
    private val queuedResults = MutableStateFlow(0)
    private val copyingJob = MutableStateFlow<Job?>(null)

    init {
        scope.launch {
            serialization.withLock {
                try {
                    records.recoverPendingWrite()
                    val intent = records.read()
                    if (intent != null) {
                        if (intent.status == BackupExportStatus.COPYING && intent.destination != null) copy(
                            intent
                        )
                        else publish(intent)
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    operation.value = BackupExportOperation(BackupExportStatus.FAILED)
                }
            }
        }
    }

    override suspend fun prepare(
        selection: BackupSelection,
        progress: (BackupProgress) -> Unit
    ): BackupExport = withContext(dispatchers.io) {
        serialization.withLock {
            records.recoverPendingWrite()
            records.read()?.let {
                return@withLock publish(it).backup ?: error("Pending backup source is unavailable")
            }
            val backup = archives.create(selection, progress)
            val intent = BackupExportIntent(backup.handle, backup.filename)
            // Never delete this source on a failed checked write: its quarantined intent can be retried.
            records.write(intent)
            publish(intent)
            backup
        }
    }

    override fun observe(): StateFlow<BackupExportOperation> = operation.asStateFlow()

    override fun submit(completion: BackupExportCompletion) {
        queuedResults.update { it + 1 }
        operation.value = operation.value.copy(status = BackupExportStatus.PROCESSING)
        scope.launch {
            serialization.withLock {
                var hasFailed = false
                try {
                    records.recoverPendingWrite()
                    val intent =
                        checkNotNull(records.read()) { "No staged export owns this document result" }
                    if (intent.handle != completion.handle) {
                        publish(intent)
                        return@withLock
                    }
                    when (completion) {
                        is BackupExportCompletion.DestinationSelected -> {
                            require(completion.document.isNotBlank()) { "Empty backup destination" }
                            // A fresh picker can truncate even a previously completed destination URI.
                            copy(
                                intent.copy(
                                    destination = completion.document
                                )
                            )
                        }

                        is BackupExportCompletion.NativeCompleted -> {
                            val complete = intent.copy(status = BackupExportStatus.COMPLETE)
                            records.write(complete)
                            publish(complete)
                        }

                        is BackupExportCompletion.Canceled -> if (intent.status != BackupExportStatus.COMPLETE) dismissLocked(
                            intent
                        )
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    hasFailed = true
                    operation.value = operation.value.copy(status = BackupExportStatus.FAILED)
                } finally {
                    queuedResults.update { it - 1 }
                    if (queuedResults.value == 0) {
                        try {
                            val current = records.read()
                            if (current == null) operation.value =
                                BackupExportOperation(if (hasFailed) BackupExportStatus.FAILED else BackupExportStatus.NONE)
                            else {
                                publish(current)
                                if (hasFailed) operation.value =
                                    operation.value.copy(status = BackupExportStatus.FAILED)
                            }
                        } catch (_: Exception) {
                            operation.value =
                                operation.value.copy(status = BackupExportStatus.FAILED)
                        }
                    }
                }
            }
        }
    }

    private suspend fun copy(intent: BackupExportIntent) {
        val backup = archives.preparedExport(intent.handle, intent.filename)
        val destination = checkNotNull(intent.destination) { "Missing backup destination" }
        val copying = intent.copy(status = BackupExportStatus.COPYING)
        withContext(NonCancellable) { records.write(copying) }
        operation.value = BackupExportOperation(BackupExportStatus.COPYING, backup)
        val owner = currentCoroutineContext().job
        copyingJob.value = owner
        try {
            export(backup.document, destination)
            withContext(NonCancellable) {
                val completed = copying.copy(status = BackupExportStatus.COMPLETE)
                records.write(completed)
                publish(completed)
            }
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) {
                val canceled = copying.copy(status = BackupExportStatus.CANCELED)
                records.write(canceled)
                publish(canceled)
            }
            throw cancelled
        } catch (failure: Exception) {
            withContext(NonCancellable) {
                val failed = copying.copy(status = BackupExportStatus.FAILED)
                records.write(failed)
                publish(failed)
            }
            throw failure
        } finally {
            if (copyingJob.value === owner) copyingJob.value = null
        }
    }

    override fun cancel() {
        copyingJob.value?.cancel()
    }

    override suspend fun dismiss() = withContext(dispatchers.io) {
        serialization.withLock {
            records.recoverPendingWrite()
            records.read()?.let { dismissLocked(it) }
            Unit
        }
    }

    private fun dismissLocked(intent: BackupExportIntent) {
        records.clear()
        operation.value =
            BackupExportOperation(if (queuedResults.value > 0) BackupExportStatus.PROCESSING else BackupExportStatus.NONE)
        archives.discard(intent.handle)
    }

    private fun publish(intent: BackupExportIntent): BackupExportOperation =
        BackupExportOperation(
            if (queuedResults.value > 0) BackupExportStatus.PROCESSING else intent.status,
            archives.preparedExport(intent.handle, intent.filename)
        ).also { operation.value = it }

    override fun close() {
        scope.cancel()
    }
}
