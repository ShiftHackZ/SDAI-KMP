package com.shifthackz.aisdv1.feature.backup.codec

import okio.use
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json
import okio.blackholeSink
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.HashingSink
import okio.HashingSource
import okio.Path
import okio.SYSTEM
import okio.buffer

/**
 * Streams bounded, length-delimited sections with independently verified SHA-256 checksums.
 * @param json Forward-compatible JSON metadata decoder; binary payloads never become one JSON document.
 * @author Dmitriy Moroz
 */
internal class BinaryBackupArchiveCodec(private val json: Json) : BackupArchiveCodec {

    override suspend fun write(path: Path, manifest: BackupManifest, payloads: List<Path>) {
        val bytes = json.encodeToString(manifest).encodeToByteArray()
        require(bytes.size <= MAX_METADATA) { "Backup manifest is too large" }
        FileSystem.SYSTEM.sink(path).buffer().use { sink ->
            sink.write(MAGIC)
            sink.writeShortLe(1)
            sink.writeShortLe(0)
            sink.writeIntLe(bytes.size)
            sink.write(bytes)
            payloads.forEach { payload ->
                FileSystem.SYSTEM.source(payload).buffer().use { source ->
                    val buffer = Buffer()
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = source.read(buffer, 65536)
                        if (count == -1L) break
                        sink.write(buffer, count)
                    }
                }
            }
        }
    }

    override suspend fun inspect(path: Path): BackupManifest {
        FileSystem.SYSTEM.source(path).buffer().use { source ->
            require(source.readByteArray(8).contentEquals(MAGIC)) { "Not an SDAI backup" }
            require(source.readShortLe().toInt() == 1) { "Unsupported backup container version" }
            source.readShortLe() // Additive minor versions preserve framing.
            val length = source.readIntLe()
            require(length in 1..MAX_METADATA) { "Invalid backup manifest length" }
            val manifest = json.decodeFromString<BackupManifest>(source.readUtf8(length.toLong()))
            require(manifest.schemaVersion == 1 && manifest.sections.size <= 128) { "Unsupported backup manifest" }
            require(manifest.archiveId.isNotBlank() && manifest.createdAt >= 0) { "Invalid backup metadata" }
            require(manifest.sections.map { it.id }
                .distinct().size == manifest.sections.size) { "Duplicate backup sections" }
            var remaining = FileSystem.SYSTEM.metadata(path).size!! - 16L - length
            manifest.sections.forEach { section ->
                require(section.storedBytes >= 0 && section.storedBytes <= remaining && section.itemCount >= 0) { "Invalid section length" }
                require(section.schemaVersion > 0 && section.requiredReaderVersion > 0) { "Invalid section version" }
                require(section.sha256.matches(Regex("[0-9a-f]{64}"))) { "Invalid backup checksum" }
                if (section.codec == "stored") require(section.decodedBytes == section.storedBytes) { "Invalid decoded length" }
                val hashing = HashingSink.sha256(blackholeSink())
                val buffer = Buffer()
                var left = section.storedBytes
                hashing.use { output ->
                    while (left > 0) {
                        currentCoroutineContext().ensureActive()
                        val read = source.read(buffer, minOf(left, 65536L))
                        require(read > 0) { "Truncated backup section" }
                        output.write(buffer, read)
                        left -= read
                    }
                }
                require(hashing.hash.hex() == section.sha256) { "Backup checksum mismatch" }
                remaining -= section.storedBytes
            }
            require(remaining == 0L && source.exhausted()) { "Unexpected trailing backup data" }
            return manifest
        }
    }

    override fun openSection(path: Path, manifest: BackupManifest, id: String): BufferedSource {
        val source = FileSystem.SYSTEM.source(path).buffer()
        try {
            source.skip(12)
            val size = source.readIntLe()
            source.skip(size.toLong())
            for (section in manifest.sections) {
                if (section.id == id) return BoundedBackupSource(
                    source,
                    section.storedBytes
                ).buffer()
                source.skip(section.storedBytes)
            }
            error("Backup section is absent")
        } catch (failure: Throwable) {
            source.close()
            throw failure
        }
    }

    override suspend fun describe(path: Path, id: String, count: Long): BackupSection {
        val hashing = HashingSource.sha256(FileSystem.SYSTEM.source(path))
        hashing.buffer().use { input ->
            val buffer = Buffer()
            while (input.read(buffer, 65536) != -1L) {
                currentCoroutineContext().ensureActive()
                buffer.clear()
            }
        }
        val size = FileSystem.SYSTEM.metadata(path).size!!
        return BackupSection(
            id = id,
            storedBytes = size,
            decodedBytes = size,
            sha256 = hashing.hash.hex(),
            itemCount = count
        )
    }

    override fun writeJson(sink: BufferedSink, text: String) {
        val bytes = text.encodeToByteArray()
        require(bytes.size <= MAX_METADATA) { "Backup record metadata is too large" }
        sink.writeIntLe(bytes.size)
        sink.write(bytes)
    }

    override fun readJson(source: BufferedSource): String {
        val count = source.readIntLe()
        require(count in 0..MAX_METADATA) { "Invalid backup record metadata length" }
        return source.readUtf8(count.toLong())
    }

    private companion object {
        const val MAX_METADATA = 1024 * 1024
        val MAGIC = byteArrayOf(83, 68, 65, 73, 66, 65, 75, 0)
    }
}
