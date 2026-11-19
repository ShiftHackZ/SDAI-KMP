package com.shifthackz.aisdv1.domain.entity.backup

/**
 * Explicit consent request or durable completion report; no secret values are exposed to UI state.
 * @author Dmitriy Moroz
 */
sealed interface BackupRestoreResult {
    /**
     * Provider replacement requires confirmation of this exact source/destination revision.
     * @param providers All configured provider names affected by the replacement.
     * @param fingerprint Opaque consent revision invalidated by destination changes.
     * @author Dmitriy Moroz
     */

    data class Conflict(val providers: Set<String>, val fingerprint: String) : BackupRestoreResult

    /**
     * Reports actual committed work after success or a recoverable category failure.
     * @param completed Categories with durable section markers in the journal.
     * @param skippedCount Unsupported or deliberately omitted entries.
     * @param hasFailed Whether selected data remains unapplied after a category failure.
     * @param importedCounts Committed entry counts retained across session retries.
     * @param notices Typed explanations identifying deliberately preserved destination data.
     * @author Dmitriy Moroz
     */

    data class Finished(
        val completed: Set<BackupCategory>,
        val skippedCount: Int,
        val hasFailed: Boolean,
        val importedCounts: Map<BackupCategory, Long> = emptyMap(),
        val notices: List<BackupRestoreNotice> = emptyList()
    ) : BackupRestoreResult
}
