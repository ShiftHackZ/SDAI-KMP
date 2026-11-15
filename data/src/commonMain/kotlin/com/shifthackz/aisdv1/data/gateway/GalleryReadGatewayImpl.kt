package com.shifthackz.aisdv1.data.gateway

import com.shifthackz.aisdv1.data.mappers.mapMetadataToDomain
import com.shifthackz.aisdv1.data.mappers.mapPreviewEntityToDomain
import com.shifthackz.aisdv1.domain.entity.GalleryItemMetadata
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import kotlinx.coroutines.flow.map

/**
 * SQLite projection owner; selection reads stay below the platform bind-parameter limit.
 *
 * @param dao Persistent gallery access.
 * @author Dmitriy Moroz
 */
internal class GalleryReadGatewayImpl(private val dao: GenerationResultDao) : GalleryReadGateway {
    /**
     * Observes IDs without images.
     *
     * @author Dmitriy Moroz
     */
    override fun observeIds() = dao.observeIds()

    /**
     * Observes a bounded metadata page without materializing encoded image payloads.
     *
     * @param limit Requested metadata count, capped at 120.
     * @param offset Nonnegative global position.
     * @author Dmitriy Moroz
     */
    override fun observeMetadata(limit: Int, offset: Int) =
        dao.observeMetadata(limit.coerceIn(1, 120), offset.coerceAtLeast(0))
            .map { it.map { row -> row.mapMetadataToDomain() } }

    /**
     * Loads metadata for distinct selected identities in bounded SQL chunks; missing rows are omitted.
     *
     * @param ids Existing selected identities.
     * @author Dmitriy Moroz
     */
    override suspend fun metadata(ids: List<Long>): List<GalleryItemMetadata> =
        ids.distinct().chunked(400)
            .flatMap { dao.queryMetadata(it).map { row -> row.mapMetadataToDomain() } }

    /**
     * Loads only identity and visibility/like flags for existing distinct selected rows.
     *
     * @param ids Existing selected identities, queried without prompt/image values.
     * @author Dmitriy Moroz
     */
    override suspend fun flags(ids: List<Long>) =
        ids.distinct().chunked(400).flatMap { dao.queryFlags(it).mapPreviewEntityToDomain() }

    /**
     * Observes one item's metadata through replacement and deletion; a missing row emits null.
     *
     * @param id Persisted identity, observed through replacements.
     * @author Dmitriy Moroz
     */
    override fun observeItem(id: Long) =
        dao.observeMetadataItem(id).map { it?.mapMetadataToDomain() }

    /**
     * Reads history without encoded originals.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun history() =
        dao.queryHistoryMetadata().map { row -> row.mapMetadataToDomain() }

    /**
     * SQLite UTF8 byte aggregate.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun storageBytes() = dao.queryImageStorageBytes()

    /**
     * Reads one revision-matching encoded image only when its SQL length fits the character budget.
     * A missing, replaced or oversized image returns null before payload materialization.
     *
     * @param id Image identity.
     * @param revision Expected content identity.
     * @param isInput Input/output selection.
     * @param maxChars Pre-materialization character budget.
     * @author Dmitriy Moroz
     */
    override suspend fun image(id: Long, revision: String, isInput: Boolean, maxChars: Int) =
        dao.queryImage(id, revision, isInput, maxChars.coerceIn(1, 32 * 1024 * 1024))
}
