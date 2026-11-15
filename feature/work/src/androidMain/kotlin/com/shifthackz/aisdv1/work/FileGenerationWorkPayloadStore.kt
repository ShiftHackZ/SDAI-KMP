package com.shifthackz.aisdv1.work

import android.util.AtomicFile
import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import java.io.File
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * Atomic work snapshots remain separate from the latest explicit-retry slot.
 *
 * @param descriptor Existing private work-cache location.
 * @author Dmitriy Moroz
 */
internal class FileGenerationWorkPayloadStore(private val descriptor: FileProviderDescriptor) :
    GenerationWorkPayloadStore {
    /**
     * Atomically writes the per-work snapshot, then updates the separate explicit-retry slot.
     * Each serialized payload must fit the 64 MiB limit; these two writes are not a shared transaction.
     *
     * @param id Work UUID.
     * @param latestKey Existing retry-slot constant.
     * @param bytes Complete serialized application payload.
     * @author Dmitriy Moroz
     */
    override fun write(id: String, latestKey: String, bytes: ByteArray) {
        require(UUID.fromString(id).toString() == id)
        require(bytes.size in 1..MAX_BYTES)
        writeFile("$id.bin", bytes)
        writeFile(checkedKey(latestKey), bytes)
    }

    /**
     * Reads the required UUID snapshot for new work, or the legacy retry slot for pre-upgrade work.
     * Missing, empty or oversized content fails; a missing new snapshot never falls back to the retry slot.
     *
     * @param id Work UUID.
     * @param legacyKey Existing retry-slot constant.
     * @param hasImmutablePayload True for new work requests.
     * @author Dmitriy Moroz
     */
    override fun read(id: String, legacyKey: String, hasImmutablePayload: Boolean): ByteArray {
        require(UUID.fromString(id).toString() == id)
        return readFile(if (hasImmutablePayload) "$id.bin" else checkedKey(legacyKey))
    }

    /**
     * Reads the bounded payload retained for an explicit user retry; missing, empty or oversized files fail.
     * The caller validates the serialized payload contents.
     *
     * @param key Explicit retry-slot constant.
     * @author Dmitriy Moroz
     */
    override fun latest(key: String): ByteArray = readFile(checkedKey(key))

    /**
     * Removes UUID snapshots not owned by unfinished work, preserving active IDs and retry slots.
     *
     * @param activeIds UUIDs retained by unfinished WorkManager requests.
     * @author Dmitriy Moroz
     */
    override fun prune(activeIds: Set<String>) {
        File(descriptor.workCacheDirPath).listFiles()?.forEach { candidate ->
            val id = candidate.name.removeSuffix(".bin")
            if (candidate.extension == "bin" && runCatching {
                    UUID.fromString(id).toString() == id
                }.getOrDefault(false) && id !in activeIds) {
                AtomicFile(file(candidate.name)).delete()
            }
        }
    }

    /**
     * Deletes only the terminal worker's UUID snapshot, preserving the latest explicit-retry slot.
     *
     * @param id Terminal work UUID.
     * @author Dmitriy Moroz
     */
    override fun delete(id: String) {
        require(UUID.fromString(id).toString() == id)
        AtomicFile(file("$id.bin")).delete()
    }

    private fun checkedKey(key: String): String {
        require(key == Constants.FILE_TEXT_TO_IMAGE || key == Constants.FILE_IMAGE_TO_IMAGE)
        return key
    }

    private fun file(name: String): File {
        val directory = File(descriptor.workCacheDirPath).canonicalFile
        check(directory.mkdirs() || directory.isDirectory)
        return File(directory, name).also { require(it.canonicalFile.parentFile == directory) }
    }

    private fun readFile(name: String): ByteArray {
        val target = file(name)
        require(target.length() in 1L..MAX_BYTES.toLong())
        return AtomicFile(target).openRead().use { input ->
            ByteArrayOutputStream().use { output ->
                val buffer = ByteArray(8192)
                var total = 0L
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    total += count
                    require(total <= MAX_BYTES)
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
        }
    }

    private fun writeFile(name: String, bytes: ByteArray) {
        val atomic = AtomicFile(file(name))
        val output = atomic.startWrite()
        try {
            output.write(bytes); atomic.finishWrite(output)
        } catch (failure: Throwable) {
            atomic.failWrite(output); throw failure
        }
    }

    private companion object {
        const val MAX_BYTES = 64 * 1024 * 1024
    }
}
