package com.shifthackz.aisdv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSessionEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import com.shifthackz.aisdv1.storage.db.persistent.contract.RestoreSessionContract
import com.shifthackz.aisdv1.storage.db.persistent.contract.RestoreSectionContract

/**
 * Persists restore intent and committed categories without serializing application secrets.
 * @author Dmitriy Moroz
 */
@Dao
interface RestoreSessionDao {
    /**
     * Returns unfinished restore intents before normal application startup publication.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${RestoreSessionContract.TABLE} WHERE ${RestoreSessionContract.IS_COMPLETE} = 0 OR ${RestoreSessionContract.REQUIRES_CONFIGURATION} = 1")
    suspend fun pending(): List<RestoreSessionEntity>

    /**
     * Loads exact operation intent, refusing to silently reuse IDs for different imports.
     * @param id Restore session ID.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${RestoreSessionContract.TABLE} WHERE ${RestoreSessionContract.ID} = :id")
    suspend fun get(id: String): RestoreSessionEntity?

    /**
     * Records one new restore intent. Reused session IDs are rejected.
     * @param session Nonsecret archive and selection binding.
     * @author Dmitriy Moroz
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: RestoreSessionEntity)

    /**
     * Reads categories already applied by this operation.
     * @param sessionId Restore session ID.
     * @author Dmitriy Moroz
     */
    @Query("SELECT ${RestoreSectionContract.CATEGORY} FROM ${RestoreSectionContract.TABLE} WHERE ${RestoreSectionContract.SESSION_ID} = :sessionId")
    suspend fun completed(sessionId: String): List<String>

    /**
     * Loads committed counts for accurate reports after process recovery.
     * @param sessionId Restore session ID.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${RestoreSectionContract.TABLE} WHERE ${RestoreSectionContract.SESSION_ID} = :sessionId")
    suspend fun results(sessionId: String): List<RestoreSectionEntity>

    /**
     * Marks a category applied in the same transaction as its additive data.
     * @param section Committed category and owning operation.
     * @author Dmitriy Moroz
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun mark(section: RestoreSectionEntity)

    /**
     * Completes an operation after all selected sections have committed.
     * @param id Restore session ID.
     * @author Dmitriy Moroz
     */
    @Query("UPDATE ${RestoreSessionContract.TABLE} SET ${RestoreSessionContract.IS_COMPLETE} = 1 WHERE ${RestoreSessionContract.ID} = :id")
    suspend fun finish(id: String)

    /**
     * Acknowledges configuration only after its startup stage was checked durably.
     * @param id Restore session whose first-launch handoff has been persisted.
     * @author Dmitriy Moroz
     */
    @Query("UPDATE ${RestoreSessionContract.TABLE} SET ${RestoreSessionContract.REQUIRES_CONFIGURATION} = 0 WHERE ${RestoreSessionContract.ID} = :id")
    suspend fun acknowledgeConfiguration(id: String)
}
