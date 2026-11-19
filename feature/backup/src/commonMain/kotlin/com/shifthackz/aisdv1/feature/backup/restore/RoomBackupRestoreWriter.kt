package com.shifthackz.aisdv1.feature.backup.restore

import okio.use
import com.shifthackz.aisdv1.domain.entity.NetworkUsageBucket
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.feature.backup.archive.StagedBackup
import com.shifthackz.aisdv1.feature.backup.codec.BackupArchiveCodec
import com.shifthackz.aisdv1.feature.backup.codec.GalleryBackupCodec
import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import com.shifthackz.aisdv1.storage.db.persistent.entity.RestoreSectionEntity
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json

/**
 * Imports directly into Room, avoiding generation-save auto-export and preserving all existing rows.
 * @param database Destination database and category commit ledger.
 * @param codec Validated length-delimited section reader.
 * @param galleryCodec Per-image metadata and binary image decoder.
 * @param files Fresh imported image revision identities.
 * @param json Typed network counter decoder.
 * @author Dmitriy Moroz
 */
internal class RoomBackupRestoreWriter(
    private val database: BackupStorage,
    private val codec: BackupArchiveCodec,
    private val galleryCodec: GalleryBackupCodec,
    private val files: BackupFileStore,
    private val json: Json,
) : BackupRestoreWriter {
    override suspend fun gallery(
        sessionId: String,
        archive: StagedBackup,
        progress: (BackupProgress) -> Unit
    ) {
        val descriptor = archive.manifest.sections.single { it.id == "gallery" }
        var committedCount: Long? = null
        database.writeTransaction {
            if ("gallery" in database.restoreSessionDao()
                    .completed(sessionId)
            ) return@writeTransaction
            var count = 0L
            var skipped = 0L
            codec.openSection(archive.path, archive.manifest, "gallery").use { source ->
                var index = 0L
                while (index < descriptor.itemCount) {
                    currentCoroutineContext().ensureActive()
                    val row = galleryCodec.read(source, files.newId())
                    if (row == null) skipped++ else {
                        database.generationResultDao().insert(row)
                        count++
                    }
                    index++
                }
                require(source.exhausted()) { "Unexpected gallery payload" }
            }
            database.restoreSessionDao()
                .mark(RestoreSectionEntity(sessionId, "gallery", count, skipped))
            committedCount = count
        }
        committedCount?.let {
            progress(
                BackupProgress(
                    BackupCategory.GALLERY,
                    it,
                    descriptor.itemCount
                )
            )
        }
    }

    override suspend fun network(sessionId: String, archive: StagedBackup) {
        val values = codec.openSection(archive.path, archive.manifest, "network").use { source ->
            val counters = json.decodeFromString<Map<String, Long>>(codec.readJson(source))
            require(source.exhausted() && counters.values.all { it >= 0 }) { "Invalid network counter" }
            counters
        }
        database.writeTransaction {
            if ("network" in database.restoreSessionDao()
                    .completed(sessionId)
            ) return@writeTransaction
            val current = database.networkUsageDao().query().associate { it.category to it.bytes }
            val supported = values.filterKeys { NetworkUsageBucket.parse(it) != null }
            supported.forEach { (key, bytes) ->
                val old = current[key] ?: 0
                require(old >= 0 && bytes <= Long.MAX_VALUE - old) { "Network counter overflow" }
            }
            // Also protect the existing totalBytes aggregate from overflow.
            var total = 0L
            NetworkUsageBucket.entries.forEach { bucket ->
                val bytes = (current[bucket.key] ?: 0) + (supported[bucket.key] ?: 0)
                require(bytes >= 0 && bytes <= Long.MAX_VALUE - total) { "Network total overflow" }
                total += bytes
            }
            supported.forEach { (key, bytes) -> database.networkUsageDao().addBytes(key, bytes) }
            database.restoreSessionDao().mark(
                RestoreSectionEntity(
                    sessionId,
                    "network",
                    supported.size.toLong(),
                    (values.size - supported.size).toLong()
                )
            )
        }
    }
}
