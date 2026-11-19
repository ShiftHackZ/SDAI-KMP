package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress

/**
 * Strict additive Room writes; each category and its ledger marker commit together.
 * @author Dmitriy Moroz
 */
internal interface BackupRestoreWriter {
    /**
     * Allocates fresh IDs and imports gallery rows with their section marker in one Room transaction.
     * @param sessionId Stable restore retry identity.
     * @param archive Validated immutable staged archive.
     * @param progress Processed-row progress; completion is acknowledged only after commit.
     * @author Dmitriy Moroz
     */
    suspend fun gallery(
        sessionId: String,
        archive: StagedBackup,
        progress: (BackupProgress) -> Unit
    )

    /**
     * Adds checked known byte buckets and their section marker atomically, never resetting existing counters.
     * @param sessionId Stable restore retry identity.
     * @param archive Validated immutable staged archive.
     * @author Dmitriy Moroz
     */
    suspend fun network(sessionId: String, archive: StagedBackup)
}
