package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.shifthackz.aisdv1.storage.db.persistent.contract.RestoreSectionContract

/**
 * Committed-section ledger. Gallery/statistics write this row in their data transaction.
 *
 * @param sessionId Owning restore session.
 * @param category Stable completed section ID.
 * @param importedCount Number of destination records durably added or applied.
 * @param skippedCount Number of duplicate or unsupported entries omitted from this committed category.
 * @param retainedCredentialProviders Comma-separated stable provider IDs whose shared credentials were preserved.
 * @author Dmitriy Moroz
 */
@Entity(
    tableName = RestoreSectionContract.TABLE,
    primaryKeys = [RestoreSectionContract.SESSION_ID, RestoreSectionContract.CATEGORY]
)
data class RestoreSectionEntity(
    @ColumnInfo(name = RestoreSectionContract.SESSION_ID)
    val sessionId: String,
    @ColumnInfo(name = RestoreSectionContract.CATEGORY)
    val category: String,
    @ColumnInfo(name = RestoreSectionContract.IMPORTED_COUNT)
    val importedCount: Long = 0,
    @ColumnInfo(name = RestoreSectionContract.SKIPPED_COUNT)
    val skippedCount: Long = 0,
    @ColumnInfo(name = RestoreSectionContract.RETAINED_CREDENTIAL_PROVIDERS, defaultValue = "''")
    val retainedCredentialProviders: String = ""
)
