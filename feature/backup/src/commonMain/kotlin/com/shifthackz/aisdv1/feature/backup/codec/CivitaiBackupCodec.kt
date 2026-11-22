package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import okio.BufferedSink
import okio.BufferedSource

/**
 * Frames public upload receipts independently from protected account state.
 *
 * @author Dmitriy Moroz
 */
internal interface CivitaiBackupCodec {
    /**
     * Writes one receipt without its destination-specific database ID.
     *
     * @param sink Bounded archive-section writer.
     * @param row Public metadata from a consistent storage snapshot.
     * @author Dmitriy Moroz
     */
    fun write(sink: BufferedSink, row: CivitaiUploadEntity)

    /**
     * Validates one receipt and allocates no destination ID; unknown status names return null.
     * Interrupted uploads restore as uncertain attempts and cannot trigger automatic reposting.
     *
     * @param source Length-bounded archive-section reader.
     * @author Dmitriy Moroz
     */
    fun read(source: BufferedSource): CivitaiUploadEntity?
}
