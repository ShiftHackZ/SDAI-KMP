package com.shifthackz.aisdv1.feature.backup.codec

import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.entity.isCivitaiPostUrl
import com.shifthackz.aisdv1.storage.db.persistent.entity.CivitaiUploadEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

/**
 * Serializes only typed public receipt fields and rejects invalid identities or unsafe post links on import.
 *
 * @param archive Length-bounded JSON framing shared by every archive section.
 * @param json Forward-compatible serializer; unknown fields never enter restored persistence.
 * @author Dmitriy Moroz
 */
internal class DefaultCivitaiBackupCodec(
    private val archive: BackupArchiveCodec,
    private val json: Json,
) : CivitaiBackupCodec {
    /**
     * Frames the public receipt fields without including the source database row ID.
     *
     * @param sink Archive section receiving one bounded JSON record.
     * @param row Public receipt from the current storage snapshot.
     * @author Dmitriy Moroz
     */
    override fun write(sink: BufferedSink, row: CivitaiUploadEntity) {
        archive.writeJson(
            sink, json.encodeToString(
                CivitaiBackupRecord(
                    row.imageId, row.accountId, row.url, row.createdAt,
                    row.imageCreatedAt, row.imageRevision, row.status,
                )
            )
        )
    }

    /**
     * Validates one public receipt and converts interrupted uploads to an uncertain outcome.
     * Unknown future states consume their frame and return null; malformed known records fail the import.
     *
     * @param source Reader restricted to the current archive section.
     * @author Dmitriy Moroz
     */
    override fun read(source: BufferedSource): CivitaiUploadEntity? {
        val record = json.decodeFromString<CivitaiBackupRecord>(archive.readJson(source))
        val status =
            CivitaiUploadStatus.entries.firstOrNull { it.name == record.status } ?: return null
        require(record.imageId > 0 && record.accountId > 0) { "Invalid Civitai receipt identity" }
        require(record.createdAt >= 0 && record.imageCreatedAt >= 0 && record.imageRevision.length in 1..256) {
            "Invalid Civitai receipt metadata"
        }
        require(record.url == null || record.url.isCivitaiPostUrl()) { "Invalid Civitai post URL" }
        require(status != CivitaiUploadStatus.PUBLISHED || record.url != null) { "Published receipt needs a post URL" }
        return CivitaiUploadEntity(
            imageId = record.imageId, accountId = record.accountId, url = record.url,
            createdAt = record.createdAt, imageCreatedAt = record.imageCreatedAt,
            imageRevision = record.imageRevision,
            status = if (status == CivitaiUploadStatus.UPLOADING) CivitaiUploadStatus.UNCERTAIN.name else status.name,
        )
    }
}
