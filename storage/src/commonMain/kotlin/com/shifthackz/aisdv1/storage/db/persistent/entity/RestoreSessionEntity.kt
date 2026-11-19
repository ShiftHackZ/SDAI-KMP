package com.shifthackz.aisdv1.storage.db.persistent.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Durable restore identity containing only private archive and selection references, never secret values.
 * @param id Unique restore session, separate from archive identity.
 * @param archiveHandle App-owned staged archive basename.
 * @param selection Selected category/provider IDs encoded as JSON.
 * @param isComplete Whether every selected category has a terminal committed result.
 * @param requiresConfiguration Whether the checked configuration handoff still needs acknowledgement.
 * @author Dmitriy Moroz
 */
@Entity(tableName = "restore_sessions")
data class RestoreSessionEntity(
    @PrimaryKey val id: String,
    val archiveHandle: String,
    val selection: String,
    val isComplete: Boolean = false,
    @ColumnInfo(defaultValue = "0") val requiresConfiguration: Boolean = false,
)
