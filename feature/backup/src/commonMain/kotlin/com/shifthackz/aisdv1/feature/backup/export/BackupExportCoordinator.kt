package com.shifthackz.aisdv1.feature.backup.export

import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportOperation
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns one process-scoped export; native callbacks are serialized independently from screen lifetimes.
 *
 * @author Dmitriy Moroz
 */
internal interface BackupExportCoordinator {
    /**
     * Creates or resumes a staged source and persists association before native picker presentation.
     *
     * @param selection Explicit data selection when creating a new source.
     * @param progress Bounded source-creation progress.
     * @author Dmitriy Moroz
     */
    suspend fun prepare(
        selection: BackupSelection,
        progress: (BackupProgress) -> Unit
    ): BackupExport

    /**
     * Observes nonsecret pending/completed operation state across composition recreation.
     *
     * @author Dmitriy Moroz
     */
    fun observe(): StateFlow<BackupExportOperation>

    /**
     * Queues a native result even while screen initialization is running.
     *
     * @param completion Destination or native completion/cancellation outcome.
     * @author Dmitriy Moroz
     */
    fun submit(completion: BackupExportCompletion)

    /**
     * Cancels only the currently active copy, preserving its source and explicit retry state.
     *
     * @author Dmitriy Moroz
     */
    fun cancel()

    /**
     * Acknowledges the current export before releasing its app-owned staged source.
     *
     * @author Dmitriy Moroz
     */
    suspend fun dismiss()

    /**
     * Closes the process owner when its Koin graph is stopped; durable intent remains recoverable.
     *
     * @author Dmitriy Moroz
     */
    fun close()
}
