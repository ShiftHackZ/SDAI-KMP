package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Committed-section ledger. Gallery/statistics write this row in their data transaction.
 * @param sessionId Owning restore session.
 * @param category Stable completed section ID.
 * @param importedCount Number of destination records durably added or applied.
 * @param skippedCount Number of duplicate or unsupported entries omitted from this committed category.
 * @param retainedCredentialProviders Comma-separated stable provider IDs whose shared credentials were preserved.
 * @author Dmitriy Moroz
 */
@Entity(tableName = "restore_sections", primaryKeys = ["sessionId", "category"])
data class RestoreSectionEntity(
    val sessionId: String,
    val category: String,
    val importedCount: Long = 0,
    val skippedCount: Long = 0,
    @ColumnInfo(defaultValue = "''") val retainedCredentialProviders: String = ""
)
