package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.domain.entity.AiGenerationResultPreview
import kotlinx.coroutines.flow.Flow

/**
 * Reads gallery projections without retaining original image payloads in list state.
 *
 * @author Dmitriy Moroz
 */
interface GalleryReadGateway {
    /**
     * Observes deterministic created-at/ID descending positions.
     *
     * @author Dmitriy Moroz
     */
    fun observeIds(): Flow<List<Long>>

    /**
     * Observes a bounded parameter window.
     *
     * @param limit Maximum metadata rows.
     * @param offset Global position.
     * @author Dmitriy Moroz
     */
    fun observeMetadata(limit: Int, offset: Int): Flow<List<GalleryItemMetadata>>

    /**
     * Reads parameters for existing IDs; omitted IDs have been deleted.
     *
     * @param ids Requested identities.
     * @author Dmitriy Moroz
     */
    suspend fun metadata(ids: List<Long>): List<GalleryItemMetadata>

    /**
     * Reads only selection flags and content revisions, without prompt or image payloads.
     *
     * @param ids Selected identities; deleted rows are omitted.
     * @author Dmitriy Moroz
     */
    suspend fun flags(ids: List<Long>): List<AiGenerationResultPreview>

    /**
     * Observes one row including content replacement and deletion.
     *
     * @param id Persisted identity.
     * @author Dmitriy Moroz
     */
    fun observeItem(id: Long): Flow<GalleryItemMetadata?>

    /**
     * Reads visible history parameters without images.
     *
     * @author Dmitriy Moroz
     */
    suspend fun history(): List<GalleryItemMetadata>

    /**
     * Counts persisted UTF8 image and input payload bytes in storage.
     *
     * @author Dmitriy Moroz
     */
    suspend fun storageBytes(): Long

    /**
     * Reads one encoded image with a pre-materialization size and revision guard.
     *
     * @param id Persisted identity.
     * @param revision Expected content revision.
     * @param isInput Whether the input image is requested.
     * @param maxChars Maximum admitted encoded characters.
     * @author Dmitriy Moroz
     */
    suspend fun image(id: Long, revision: String, isInput: Boolean, maxChars: Int): String?
}
