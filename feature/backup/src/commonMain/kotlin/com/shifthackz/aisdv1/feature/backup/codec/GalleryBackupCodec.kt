package com.shifthackz.aisdv1.feature.backup.codec

import okio.BufferedSink
import okio.BufferedSource
import com.shifthackz.aisdv1.storage.db.persistent.entity.GenerationResultEntity

/**
 * Frames gallery metadata and validated image bytes without carrying source database identities.
 *
 * @author Dmitriy Moroz
 */
internal interface GalleryBackupCodec {
    /**
     * Writes one metadata record and its binary output/input images without serializing local row identity.
     *
     * @param sink Section writer owned by the export operation.
     * @param row Saved gallery row from a consistent database snapshot.
     * @author Dmitriy Moroz
     */
    fun write(sink: BufferedSink, row: GenerationResultEntity)

    /**
     * Decodes and validates one row with a fresh destination ID/revision; unknown generation types are skipped.
     *
     * @param source Bounded gallery section reader.
     * @param revision Fresh destination image revision unrelated to archived identity.
     * @author Dmitriy Moroz
     */
    fun read(source: BufferedSource, revision: String): GenerationResultEntity?
}
