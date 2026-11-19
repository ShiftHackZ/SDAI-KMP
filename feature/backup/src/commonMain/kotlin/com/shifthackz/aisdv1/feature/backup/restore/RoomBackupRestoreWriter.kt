package com.shifthackz.aisdv1.feature.backup.restore

import okio.use
import com.shifthackz.aisdv1.domain.entity.NetworkUsageBucket
import com.shifthackz.aisdv1.domain.entity.backup.BackupImageBudget
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
 * Imports unique output images directly into Room, preserving existing rows and avoiding generation-save auto-export.
 * @param database Destination database and category commit ledger.
 * @param codec Validated length-delimited section reader.
 * @param galleryCodec Per-image metadata and binary image decoder.
 * @param files Fresh imported image revision identities.
 * @param json Typed network counter decoder.
 * @param fingerprint Streams saved output-image identities for exact duplicate suppression.
 * @param budget Admits one ordinary image text before hashing; oversized legacy representations use chunked reads.
 * @author Dmitriy Moroz
 */
internal class RoomBackupRestoreWriter(
    private val database: BackupStorage,
    private val codec: BackupArchiveCodec,
    private val galleryCodec: GalleryBackupCodec,
    private val files: BackupFileStore,
    private val json: Json,
    private val fingerprint: BackupImageFingerprint,
    private val budget: BackupImageBudget,
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
            val knownImages = mutableSetOf<String>()
            val gallery = database.generationResultDao()
            var afterId = 0L
            while (true) {
                currentCoroutineContext().ensureActive()
                val ids = gallery.queryImageIdsAfter(afterId, 128)
                if (ids.isEmpty()) break
                ids.forEach { id ->
                    existingImageFingerprint(id)?.let(knownImages::add)
                }
                afterId = ids.last()
            }
            var count = 0L
            var skipped = 0L
            codec.openSection(archive.path, archive.manifest, "gallery").use { source ->
                var index = 0L
                while (index < descriptor.itemCount) {
                    currentCoroutineContext().ensureActive()
                    val row = galleryCodec.read(source, files.newId())
                    if (row == null) skipped++ else {
                        val identity = checkNotNull(imageFingerprint(row.imageBase64)) {
                            "Validated backup image has invalid Base64"
                        }
                        if (knownImages.add(identity)) {
                            gallery.insert(row)
                            count++
                        } else skipped++
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

    /**
     * Reads ordinary images once; exceptionally long wrapped text retains the exact-comparison fallback.
     * @param id Existing gallery/history identity inside the category transaction.
     * @author Dmitriy Moroz
     */
    private suspend fun existingImageFingerprint(id: Long): String? {
        val gallery = database.generationResultDao()
        val maxTextBytes = ((budget.maxEncodedBytes.toLong() + 2) / 3) * 4
        val text = gallery.queryImageTextForFingerprint(id, maxTextBytes)
        return if (text != null) imageFingerprint(text) else {
            fingerprint { offset, maxBytes -> gallery.queryImageChunk(id, offset + 1, maxBytes) }
        }
    }

    /**
     * Encodes an admitted string into small UTF8 chunks without allocating another full-image byte array.
     * @param text Existing admitted image or canonical Base64 returned by the validated archive decoder.
     * @author Dmitriy Moroz
     */
    private suspend fun imageFingerprint(text: String): String? {
        var characterOffset = 0
        var byteOffset = 0L
        return fingerprint { offset, maxBytes ->
            check(offset == byteOffset) { "Image fingerprint reader must advance sequentially" }
            val end = minOf(text.length, characterOffset + maxOf(1, maxBytes / 4))
            val bytes = text.substring(characterOffset, end).encodeToByteArray()
            characterOffset = end
            byteOffset += bytes.size
            bytes
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
