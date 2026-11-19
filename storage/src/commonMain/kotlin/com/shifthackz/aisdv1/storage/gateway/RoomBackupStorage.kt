package com.shifthackz.aisdv1.storage.gateway

import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import androidx.room.deferredTransaction
import androidx.room.immediateTransaction
import com.shifthackz.aisdv1.storage.db.persistent.PersistentDatabase

/**
 * Keeps Room connection ownership inside storage while backup uses the common transaction contract.
 * @param database Shared persistent Room owner; never exposed to consuming modules.
 * @author Dmitriy Moroz
 */
internal class RoomBackupStorage(private val database: PersistentDatabase) : BackupStorage {
    override suspend fun <T> readSnapshot(block: suspend () -> T): T =
        database.useReaderConnection { it.deferredTransaction { block() } }

    override suspend fun <T> writeTransaction(block: suspend () -> T): T =
        database.useWriterConnection { it.immediateTransaction { block() } }

    override fun generationResultDao() = database.generationResultDao()
    override fun networkUsageDao() = database.networkUsageDao()
    override fun restoreSessionDao() = database.restoreSessionDao()
}
