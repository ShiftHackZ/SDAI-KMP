package com.shifthackz.aisdv1.storage.db.persistent.contract

/**
 * Stable Room table and column names for the restore session intent.
 *
 * @author Dmitriy Moroz
 */
internal object RestoreSessionContract {
    /**
     * Restore ledger table whose names remain compatible with existing archives and installations.
     *
     * @author Dmitriy Moroz
     */
    const val TABLE = "restore_sessions"

    /**
     * Unique identity of one restore operation.
     *
     * @author Dmitriy Moroz
     */
    const val ID = "id"

    /**
     * App-owned staged archive basename without archive contents.
     *
     * @author Dmitriy Moroz
     */
    const val ARCHIVE_HANDLE = "archiveHandle"

    /**
     * Selected category and provider IDs encoded as JSON.
     *
     * @author Dmitriy Moroz
     */
    const val SELECTION = "selection"

    /**
     * Whether every selected category has a committed terminal result.
     *
     * @author Dmitriy Moroz
     */
    const val IS_COMPLETE = "isComplete"

    /**
     * Whether the durable first-launch configuration handoff is still pending.
     *
     * @author Dmitriy Moroz
     */
    const val REQUIRES_CONFIGURATION = "requiresConfiguration"
}
