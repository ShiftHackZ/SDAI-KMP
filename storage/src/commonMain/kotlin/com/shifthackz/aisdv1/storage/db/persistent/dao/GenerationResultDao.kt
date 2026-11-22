package com.shifthackz.aisdv1.storage.db.persistent.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifthackz.aisdv1.storage.db.persistent.contract.GenerationResultContract
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultMetadataEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultPreviewEntity
import kotlinx.coroutines.flow.Flow

/**
 * Reads paged gallery metadata and persists complete image rows; backup imports must use fresh IDs.
 *
 * @author Dmitriy Moroz
 */
@Dao
interface GenerationResultDao {
    /**
     * Records a successful publication without rewriting image bytes or gallery flags.
     *
     * @param id Saved gallery identity.
     * @param url Public Civitai post URL.
     * @author Dmitriy Moroz
     */
    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.CIVITAI_URL} = :url WHERE ${GenerationResultContract.ID} = :id")
    suspend fun setCivitaiUrl(id: Long, url: String)

    /**
     * Advances through a stable backup snapshot by primary key without repeated OFFSET scans.
     *
     * @param afterId Exclusive ID cursor from the previous page, or zero for the first page.
     * @param limit Maximum number of full image rows held at once.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM generation_results WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun queryBackupPage(afterId: Long, limit: Int): List<GenerationResultEntity>


    /**
     * Pages every saved image identity, including hidden history, without materializing image payloads.
     *
     * @param afterId Exclusive primary-key cursor, or zero for the first page.
     * @param limit Maximum IDs read for duplicate checking at once.
     * @author Dmitriy Moroz
     */
    @Query("SELECT id FROM generation_results WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun queryImageIdsAfter(afterId: Long, limit: Int): List<Long>

    /**
     * Admits one output-only image string before materialization for a single-pass duplicate check.
     *
     * @param id Saved gallery or hidden-history identity.
     * @param maxBytes Maximum stored UTF8 bytes allowed in the admitted text.
     * @return Base64 text, or null for an oversized representation or missing row.
     * @author Dmitriy Moroz
     */
    @Query("SELECT image_base_64 FROM generation_results WHERE id = :id AND length(CAST(image_base_64 AS BLOB)) <= :maxBytes")
    suspend fun queryImageTextForFingerprint(id: Long, maxBytes: Long): String?

    /**
     * Reads a bounded Kotlin chunk from an oversized legacy representation.
     * Used only when the single-read admission rejects the text; SQLite may materialize its value natively.
     *
     * @param id Saved gallery or hidden-history identity.
     * @param offset One-based byte position in the stored UTF8 Base64 representation.
     * @param maxBytes Maximum UTF8 bytes materialized in the result; BLOB slicing avoids repeated text-prefix scans.
     * @return Image text bytes, an empty array at EOF, or null if the row no longer exists.
     * @author Dmitriy Moroz
     */
    @Query("SELECT substr(CAST(image_base_64 AS BLOB), :offset, :maxBytes) FROM generation_results WHERE id = :id")
    suspend fun queryImageChunk(id: Long, offset: Long, maxBytes: Int): ByteArray?

    /**
     * Reads all gallery rows, including hidden generation history.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC, ${GenerationResultContract.ID} DESC")
    suspend fun query(): List<GenerationResultEntity>

    /**
     * Reads one full-image page ordered by creation time and ID.
     *
     * @param limit Maximum rows held in one page.
     * @param offset Number of prior ordered rows to skip.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC, ${GenerationResultContract.ID} DESC LIMIT :limit OFFSET :offset ")
    suspend fun queryPage(limit: Int, offset: Int): List<GenerationResultEntity>

    /**
     * Reads one preview-only page without full image payloads.
     *
     * @param limit Maximum rows held in one page.
     * @param offset Number of prior ordered rows to skip.
     * @author Dmitriy Moroz
     */
    @Query(
        """
        SELECT
            ${GenerationResultContract.ID},
            ${GenerationResultContract.IMAGE_REVISION},
            ${GenerationResultContract.HIDDEN},
            ${GenerationResultContract.LIKED}
        FROM ${GenerationResultContract.TABLE}
        ORDER BY ${GenerationResultContract.CREATED_AT} DESC, ${GenerationResultContract.ID} DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun queryPagePreview(limit: Int, offset: Int): List<GenerationResultPreviewEntity>

    /**
     * Observes a full-image gallery page after Room invalidations.
     *
     * @param limit Maximum rows held in one page.
     * @param offset Number of prior ordered rows to skip.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC, ${GenerationResultContract.ID} DESC LIMIT :limit OFFSET :offset ")
    fun observePage(limit: Int, offset: Int): Flow<List<GenerationResultEntity>>

    /**
     * Observes a preview-only page after Room invalidations.
     *
     * @param limit Maximum rows held in one page.
     * @param offset Number of prior ordered rows to skip.
     * @author Dmitriy Moroz
     */
    @Query(
        """
        SELECT
            ${GenerationResultContract.ID},
            ${GenerationResultContract.IMAGE_REVISION},
            ${GenerationResultContract.HIDDEN},
            ${GenerationResultContract.LIKED}
        FROM ${GenerationResultContract.TABLE}
        ORDER BY ${GenerationResultContract.CREATED_AT} DESC, ${GenerationResultContract.ID} DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    fun observePagePreview(limit: Int, offset: Int): Flow<List<GenerationResultPreviewEntity>>

    /**
     * Observes the current total number of persisted generation rows.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT COUNT(*) FROM ${GenerationResultContract.TABLE}")
    fun observeCount(): Flow<Int>

    /**
     * Reads IDs of all saved generation rows.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT ${GenerationResultContract.ID} FROM ${GenerationResultContract.TABLE} ORDER BY ${GenerationResultContract.CREATED_AT} DESC, ${GenerationResultContract.ID} DESC")
    suspend fun queryIds(): List<Long>

    /**
     * Reads one complete saved image row by local identity.
     *
     * @param id Local persisted row identity.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} = :id LIMIT 1")
    suspend fun queryById(id: Long): GenerationResultEntity

    /**
     * Reads complete saved rows for explicit local identities.
     *
     * @param idList Explicit local row identities to read or update.
     * @author Dmitriy Moroz
     */
    @Query("SELECT * FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} IN (:idList)")
    suspend fun queryByIdList(idList: List<Long>): List<GenerationResultEntity>

    /**
     * Changes history visibility without replacing image content.
     *
     * @param idList Explicit local row identities to read or update.
     * @param hidden Whether selected rows appear only in generation history.
     * @author Dmitriy Moroz
     */
    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.HIDDEN} = :hidden WHERE ${GenerationResultContract.ID} IN (:idList)")
    suspend fun updateHiddenByIdList(idList: List<Long>, hidden: Boolean)

    /**
     * Changes favorite flags without replacing image content.
     *
     * @param idList Explicit local row identities to read or update.
     * @param liked Whether selected rows are marked as favorites.
     * @author Dmitriy Moroz
     */
    @Query("UPDATE ${GenerationResultContract.TABLE} SET ${GenerationResultContract.LIKED} = :liked WHERE ${GenerationResultContract.ID} IN (:idList)")
    suspend fun updateLikedByIdList(idList: List<Long>, liked: Boolean)

    /**
     * Inserts a complete gallery row; nonzero conflicting IDs replace existing rows.
     *
     * @param item Full image row; use ID zero for additive backup import.
     * @author Dmitriy Moroz
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GenerationResultEntity): Long

    /**
     * Removes one saved row by local identity.
     *
     * @param id Local persisted row identity.
     * @author Dmitriy Moroz
     */
    @Query("DELETE FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} = :id")
    suspend fun deleteById(id: Long)

    /**
     * Removes only the explicitly selected saved rows.
     *
     * @param idList Explicit local row identities to read or update.
     * @author Dmitriy Moroz
     */
    @Query("DELETE FROM ${GenerationResultContract.TABLE} WHERE ${GenerationResultContract.ID} IN (:idList)")
    suspend fun deleteByIdList(idList: List<Long>)

    /**
     * Removes every saved gallery and hidden history row.
     *
     * @author Dmitriy Moroz
     */
    @Query("DELETE FROM ${GenerationResultContract.TABLE}")
    suspend fun deleteAll()

    /**
     * Observes stable global positions without image payloads.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT id FROM generation_results ORDER BY created_at DESC, id DESC")
    fun observeIds(): Flow<List<Long>>

    /**
     * Reads a bounded metadata window.
     *
     * @param limit Maximum rows.
     * @param offset Global starting position.
     * @author Dmitriy Moroz
     */
    @Query("SELECT id, created_at, generation_type, prompt, negative_prompt, width, height, sampling_steps, cfg_scale, restore_faces, sampler, seed, sub_seed, sub_seed_strength, denoising_strength, hidden, liked, model_name, image_revision, provider_key, acceleration, generation_duration_millis, device_name, os_version, civitai_url FROM generation_results ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset")
    fun observeMetadata(limit: Int, offset: Int): Flow<List<GenerationResultMetadataEntity>>

    /**
     * Loads metadata for selected rows, without materializing originals.
     *
     * @param ids Selected IDs, chunked by the caller.
     * @author Dmitriy Moroz
     */
    @Query("SELECT id, created_at, generation_type, prompt, negative_prompt, width, height, sampling_steps, cfg_scale, restore_faces, sampler, seed, sub_seed, sub_seed_strength, denoising_strength, hidden, liked, model_name, image_revision, provider_key, acceleration, generation_duration_millis, device_name, os_version, civitai_url FROM generation_results WHERE id IN (:ids)")
    suspend fun queryMetadata(ids: List<Long>): List<GenerationResultMetadataEntity>

    /**
     * Reads lightweight flags for selection across metadata-window eviction.
     *
     * @param ids Selected identities, chunked by the caller.
     * @author Dmitriy Moroz
     */
    @Query("SELECT id, hidden, liked, image_revision FROM generation_results WHERE id IN (:ids)")
    suspend fun queryFlags(ids: List<Long>): List<GenerationResultPreviewEntity>

    /**
     * Observes one metadata row, including same-ID replacement.
     *
     * @param id Persisted identity.
     * @author Dmitriy Moroz
     */
    @Query("SELECT id, created_at, generation_type, prompt, negative_prompt, width, height, sampling_steps, cfg_scale, restore_faces, sampler, seed, sub_seed, sub_seed_strength, denoising_strength, hidden, liked, model_name, image_revision, provider_key, acceleration, generation_duration_millis, device_name, os_version, civitai_url FROM generation_results WHERE id = :id LIMIT 1")
    fun observeMetadataItem(id: Long): Flow<GenerationResultMetadataEntity?>

    /**
     * Loads history parameters without image payloads.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT id, created_at, generation_type, prompt, negative_prompt, width, height, sampling_steps, cfg_scale, restore_faces, sampler, seed, sub_seed, sub_seed_strength, denoising_strength, hidden, liked, model_name, image_revision, provider_key, acceleration, generation_duration_millis, device_name, os_version, civitai_url FROM generation_results WHERE hidden = 0 ORDER BY created_at DESC, id DESC")
    suspend fun queryHistoryMetadata(): List<GenerationResultMetadataEntity>

    /**
     * Counts UTF8 payload bytes inside SQLite.
     *
     * @author Dmitriy Moroz
     */
    @Query("SELECT COALESCE(SUM(length(CAST(image_base_64 AS BLOB)) + length(CAST(original_image_base_64 AS BLOB))), 0) FROM generation_results")
    suspend fun queryImageStorageBytes(): Long

    /**
     * Reads a revision-matched image only when its encoded size fits the budget.
     *
     * @param id Persisted image ID.
     * @param revision Expected content identity.
     * @param isInput Whether to read the original input.
     * @param maxChars Maximum Base64 characters admitted before materialization.
     * @author Dmitriy Moroz
     */
    @Query(
        """SELECT CASE WHEN :isInput THEN original_image_base_64 ELSE image_base_64 END
        FROM generation_results WHERE id = :id AND image_revision = :revision
        AND length(CASE WHEN :isInput THEN original_image_base_64 ELSE image_base_64 END) <= :maxChars LIMIT 1"""
    )
    suspend fun queryImage(id: Long, revision: String, isInput: Boolean, maxChars: Int): String?
}
