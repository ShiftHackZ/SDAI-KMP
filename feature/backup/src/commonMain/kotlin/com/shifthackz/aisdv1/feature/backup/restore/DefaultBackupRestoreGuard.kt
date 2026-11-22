package com.shifthackz.aisdv1.feature.backup.restore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Owns one process-wide restore lock; no user-data work escapes its operation scope.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultBackupRestoreGuard : BackupRestoreGuard {
    private val mutex = Mutex()

    override suspend fun <T> exclusive(block: suspend () -> T): T = mutex.withLock { block() }
}
