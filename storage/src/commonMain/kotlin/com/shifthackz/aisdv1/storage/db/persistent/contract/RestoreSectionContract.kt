package com.shifthackz.aisdv1.storage.db.persistent.contract

/**
 * Stable Room table and column names for the restore committed-section ledger.
 *
 * @author Dmitriy Moroz
 */
internal object RestoreSectionContract {
    /**
     * Restore ledger table whose names remain compatible with existing archives and installations.
     *
     * @author Dmitriy Moroz
     */
    const val TABLE = "restore_sections"

    /**
     * Restore operation that owns this committed section.
     *
     * @author Dmitriy Moroz
     */
    const val SESSION_ID = "sessionId"

    /**
     * Stable section key committed by the operation.
     *
     * @author Dmitriy Moroz
     */
    const val CATEGORY = "category"

    /**
     * Destination records durably added or applied.
     *
     * @author Dmitriy Moroz
     */
    const val IMPORTED_COUNT = "importedCount"

    /**
     * Duplicate or unsupported entries omitted from the committed section.
     *
     * @author Dmitriy Moroz
     */
    const val SKIPPED_COUNT = "skippedCount"

    /**
     * Comma-separated provider IDs whose existing credentials were retained; never credential values.
     *
     * @author Dmitriy Moroz
     */
    const val RETAINED_CREDENTIAL_PROVIDERS = "retainedCredentialProviders"
}
