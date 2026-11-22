package com.shifthackz.aisdv1.storage.gateway

import com.shifthackz.aisdv1.storage.db.persistent.dao.CivitaiUploadDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.NetworkUsageDao
import com.shifthackz.aisdv1.storage.db.persistent.dao.RestoreSessionDao

/**
 * Storage transaction boundary used by backup implementation without exposing the Room database owner.
 *
 * @author Dmitriy Moroz
 */
interface BackupStorage {
    /**
     * Reads a consistent gallery/statistics snapshot through the shared reader connection.
     *
     * @param block Bounded paged reads performed in one transaction.
     * @author Dmitriy Moroz
     */
    suspend fun <T> readSnapshot(block: suspend () -> T): T

    /**
     * Commits additive user data and its category ledger together.
     *
     * @param block Data writes and section marker belonging to one category.
     * @author Dmitriy Moroz
     */
    suspend fun <T> writeTransaction(block: suspend () -> T): T

    /**
     * Returns paged gallery persistence without generation-save export side effects.
     *
     * @author Dmitriy Moroz
     */
    fun generationResultDao(): GenerationResultDao

    /**
     * Returns public upload receipts without access to protected OAuth or API-key storage.
     *
     * @author Dmitriy Moroz
     */
    fun civitaiUploadDao(): CivitaiUploadDao

    /**
     * Returns atomic durable network byte buckets.
     *
     * @author Dmitriy Moroz
     */
    fun networkUsageDao(): NetworkUsageDao

    /**
     * Returns nonsecret restore session and committed-section persistence.
     *
     * @author Dmitriy Moroz
     */
    fun restoreSessionDao(): RestoreSessionDao
}
