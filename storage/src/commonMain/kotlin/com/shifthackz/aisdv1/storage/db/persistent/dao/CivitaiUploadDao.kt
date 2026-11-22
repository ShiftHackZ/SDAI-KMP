package com.shifthackz.aisdv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifthackz.aisdv1.storage.db.persistent.contract.CivitaiUploadContract
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import kotlinx.coroutines.flow.Flow

/**
 * Reads bounded upload-history windows and atomically replaces publication receipts.
 *
 * @author Dmitriy Moroz
 */
@Dao
interface CivitaiUploadDao {
    /**
     * Counts public receipt rows for selective backup inventory.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT COUNT(*) FROM ${CivitaiUploadContract.TABLE}")
    suspend fun count(): Long

    /**
     * Streams a stable primary-key page inside a backup snapshot, independently of viewport ordering.
     *
     * @param afterId Last exported primary key, or zero for the first page.
     * @param limit Maximum number of complete receipt records to materialize.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${CivitaiUploadContract.TABLE} WHERE ${CivitaiUploadContract.ID} > :afterId ORDER BY ${CivitaiUploadContract.ID} LIMIT :limit")
    suspend fun queryBackupPage(afterId: Long, limit: Int): List<CivitaiUploadEntity>

    /**
     * Observes ordered receipt identities without loading complete history rows.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT ${CivitaiUploadContract.ID} FROM ${CivitaiUploadContract.TABLE} ORDER BY ${CivitaiUploadContract.CREATED_AT} DESC, ${CivitaiUploadContract.ID} DESC")
    fun observeIds(): Flow<List<Long>>

    /**
     * Observes a viewport-sized page in the same order as the identity list.
     *
     * @param limit Maximum number of complete receipts to retain.
     * @param offset Global index of the first requested receipt.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${CivitaiUploadContract.TABLE} ORDER BY ${CivitaiUploadContract.CREATED_AT} DESC, ${CivitaiUploadContract.ID} DESC LIMIT :limit OFFSET :offset")
    fun observePage(limit: Int, offset: Int): Flow<List<CivitaiUploadEntity>>

    /**
     * Finds an original-image receipt, preferring confirmed publication over later unsuccessful attempts.
     *
     * @param imageId Gallery identity captured for the publication.
     * @param imageCreatedAt Original gallery timestamp guarding ID reuse.
     * @param imageRevision Original content revision guarding replacement.
     * @param accountId Restricts replacement to one account, or null to check for any existing publication.
     * @author Dmitriy Moroz
     */
    @Query(
        """SELECT * FROM ${CivitaiUploadContract.TABLE}
        WHERE ${CivitaiUploadContract.IMAGE_ID} = :imageId AND ${CivitaiUploadContract.IMAGE_CREATED_AT} = :imageCreatedAt AND ${CivitaiUploadContract.IMAGE_REVISION} = :imageRevision
        AND (:accountId IS NULL OR ${CivitaiUploadContract.ACCOUNT_ID} = :accountId)
        ORDER BY CASE WHEN ${CivitaiUploadContract.STATUS} = 'PUBLISHED' THEN 0 ELSE 1 END,
        ${CivitaiUploadContract.CREATED_AT} DESC, ${CivitaiUploadContract.ID} DESC LIMIT 1"""
    )
    suspend fun find(
        imageId: Long,
        imageCreatedAt: Long,
        imageRevision: String,
        accountId: Long?
    ): CivitaiUploadEntity?

    /**
     * Commits a complete receipt, preserving an existing row identity when replacing its phase.
     *
     * @param receipt New row with ID zero, or an existing receipt retaining its ID.
     * @return Stable persisted row identity.
     * @author Dmitriy Moroz
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(receipt: CivitaiUploadEntity): Long
}
