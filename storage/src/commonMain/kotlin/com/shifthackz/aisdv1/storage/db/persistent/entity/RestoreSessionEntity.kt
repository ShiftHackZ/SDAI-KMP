package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.shifthackz.aisdv1.storage.db.persistent.contract.RestoreSessionContract

/**
 * Durable restore identity containing only private archive and selection references, never secret values.
 * @param id Unique restore session, separate from archive identity.
 * @param archiveHandle App-owned staged archive basename.
 * @param selection Selected category/provider IDs encoded as JSON.
 * @param isComplete Whether every selected category has a terminal committed result.
 * @param requiresConfiguration Whether the checked configuration handoff still needs acknowledgement.
 * @author Dmitriy Moroz
 */
@Entity(tableName = RestoreSessionContract.TABLE)
data class RestoreSessionEntity(
    @PrimaryKey @ColumnInfo(name = RestoreSessionContract.ID)
    val id: String,
    @ColumnInfo(name = RestoreSessionContract.ARCHIVE_HANDLE)
    val archiveHandle: String,
    @ColumnInfo(name = RestoreSessionContract.SELECTION)
    val selection: String,
    @ColumnInfo(name = RestoreSessionContract.IS_COMPLETE)
    val isComplete: Boolean = false,
    @ColumnInfo(name = RestoreSessionContract.REQUIRES_CONFIGURATION, defaultValue = "0")
    val requiresConfiguration: Boolean = false,
)
