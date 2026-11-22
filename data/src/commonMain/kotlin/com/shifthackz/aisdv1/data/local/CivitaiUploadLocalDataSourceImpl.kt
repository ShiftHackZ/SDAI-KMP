package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.data.mappers.toDomain
import com.shifthackz.aisdv1.data.mappers.toEntity
import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.datasource.CivitaiUploadLocalDataSource
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.storage.db.persistent.dao.CivitaiUploadDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * Converts Room receipt rows into domain metadata and migrates the legacy preference list once.
 * Interrupted conversion is idempotent; preferences are cleared only after every receipt is committed.
 *
 * @param dao Indexed persistent receipt storage and bounded page queries.
 * @param preferences Previous receipt format and durable conversion marker.
 * @author Dmitriy Moroz
 */
internal class CivitaiUploadLocalDataSourceImpl(
    private val dao: CivitaiUploadDao,
    private val preferences: KeyValueStore,
) : CivitaiUploadLocalDataSource {
    private val mutex = Mutex()

    /**
     * Observes stable identities after completing legacy receipt conversion.
     *
     * @author Dmitriy Moroz
     */
    override fun observeIds(): Flow<List<Long>> = flow {
        mutex.withLock { migrateIfNeeded() }
        emitAll(dao.observeIds())
    }

    /**
     * Observes a bounded metadata page without reading unrelated receipt rows or credentials.
     *
     * @param limit Requested receipt window, between one and 120 rows, matching the gallery budget.
     * @param offset First global receipt position.
     * @author Dmitriy Moroz
     */
    override fun observePage(limit: Int, offset: Int): Flow<List<CivitaiUpload>> = flow {
        require(limit in 1..120 && offset >= 0)
        mutex.withLock { migrateIfNeeded() }
        emitAll(dao.observePage(limit, offset).map { rows -> rows.map { it.toDomain() } })
    }

    /**
     * Reads one original-image receipt, optionally restricted to its owning account.
     *
     * @param imageId Gallery identity captured for publication.
     * @param imageCreatedAt Original gallery timestamp guarding ID reuse.
     * @param imageRevision Original image-content identity.
     * @param accountId Optional owner restriction used when replacing a phase.
     * @author Dmitriy Moroz
     */
    override suspend fun find(
        imageId: Long,
        imageCreatedAt: Long,
        imageRevision: String,
        accountId: Long?
    ): CivitaiUpload? = mutex.withLock {
        migrateIfNeeded()
        dao.find(imageId, imageCreatedAt, imageRevision, accountId)?.toDomain()
    }

    /**
     * Commits the receipt and returns the database identity, preserving failures for caller policy.
     *
     * @param receipt Complete publication phase for one account and original image.
     * @author Dmitriy Moroz
     */
    override suspend fun save(receipt: CivitaiUpload): CivitaiUpload = mutex.withLock {
        migrateIfNeeded()
        saveReceipt(receipt)
    }

    /**
     * Replaces an existing attempt without changing its lazy-list identity.
     *
     * @param receipt Phase to persist for the original image and account.
     * @author Dmitriy Moroz
     */
    private suspend fun saveReceipt(receipt: CivitaiUpload): CivitaiUpload {
        val previous = dao.find(
            receipt.imageId,
            receipt.imageCreatedAt,
            receipt.imageRevision,
            receipt.accountId
        )
        val id = dao.insert(receipt.toEntity(previous?.id ?: 0L))
        return receipt.copy(id = id)
    }

    /**
     * Converts the earlier preference format without losing ambiguous-publication guards on retry.
     * The durable marker and preference cleanup share one commit after the database writes succeed.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun migrateIfNeeded() {
        if (preferences.getBoolean("civitai_history_migrated")) return
        val legacy =
            Json.parseToJsonElement(preferences.getString("civitai_uploads", "[]")).jsonArray
        for (value in legacy) {
            val row = value.jsonObject
            saveReceipt(
                CivitaiUpload(
                    imageId = row.getValue("imageId").jsonPrimitive.long,
                    accountId = row.getValue("accountId").jsonPrimitive.long,
                    url = row["url"]?.jsonPrimitive?.contentOrNull,
                    createdAt = row.getValue("createdAt").jsonPrimitive.long,
                    imageCreatedAt = row.getValue("imageCreatedAt").jsonPrimitive.long,
                    imageRevision = row.getValue("imageRevision").jsonPrimitive.content,
                    status = CivitaiUploadStatus.valueOf(row.getValue("status").jsonPrimitive.content),
                )
            )
        }
        preferences.putSettingsDurably(
            mapOf("civitai_uploads" to "[]"),
            mapOf("civitai_history_migrated" to true),
            emptyMap(),
        )
    }
}
