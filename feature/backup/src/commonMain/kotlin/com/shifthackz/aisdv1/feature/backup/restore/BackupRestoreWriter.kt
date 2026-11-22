package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress

/**
 * Strict additive Room writes; exact gallery duplicates are skipped and each category commits with its ledger marker.
 *
 * @author Dmitriy Moroz
 */
internal interface BackupRestoreWriter {
    /**
     * Allocates fresh IDs only for output images absent from gallery/history and earlier archive rows.
     * Existing metadata, favorites and visibility remain unchanged; imports and skipped counts commit atomically.
     *
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
     *
     * @param sessionId Stable restore retry identity.
     * @param archive Validated immutable staged archive.
     * @author Dmitriy Moroz
     */
    suspend fun network(sessionId: String, archive: StagedBackup)

    /**
     * Adds missing public receipts and a category ledger marker atomically, preserving existing outcomes.
     * No account is authorized and no automatic-sharing setting is enabled by restoring history.
     *
     * @param sessionId Stable restore retry identity.
     * @param archive Validated immutable archive containing public receipts.
     * @param progress Committed receipt count, emitted only after the transaction succeeds.
     * @author Dmitriy Moroz
     */
    suspend fun civitai(
        sessionId: String,
        archive: StagedBackup,
        progress: (BackupProgress) -> Unit
    )
}
