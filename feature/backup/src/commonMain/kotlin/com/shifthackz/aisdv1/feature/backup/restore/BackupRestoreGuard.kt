package com.shifthackz.aisdv1.feature.backup.restore

/**
 * Serializes restore operations across screens and recovery entry points.
 * @author Dmitriy Moroz
 */
internal interface BackupRestoreGuard {
    /**
     * Runs one restore or staging-discard operation at a time; cancellation while waiting propagates.
     * @param block Complete operation that owns private staging and journal transitions.
     * @author Dmitriy Moroz
     */
    suspend fun <T> exclusive(block: suspend () -> T): T
}
