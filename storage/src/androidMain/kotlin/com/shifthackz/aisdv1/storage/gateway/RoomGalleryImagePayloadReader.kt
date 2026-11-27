package com.shifthackz.aisdv1.storage.gateway

import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao

/**
 * Uses Room's bulk Android BLOB projection while retaining its query and transaction ownership.
 *
 * @param dao Shared persistent image DAO.
 * @author Dmitriy Moroz
 */
internal class RoomGalleryImagePayloadReader(private val dao: GenerationResultDao) :
    GalleryImagePayloadReader {
    /**
     * Forwards revision and size admission to SQLite before encoded bytes are materialized.
     *
     * @param id Saved image identity.
     * @param revision Expected content revision.
     * @param isInput Original input or result selection.
     * @param maxChars Maximum admitted Base64 length.
     * @author Dmitriy Moroz
     */
    override suspend fun read(
        id: Long,
        revision: String,
        isInput: Boolean,
        maxChars: Int
    ): ByteArray? =
        dao.queryImage(id, revision, isInput, maxChars)
}
