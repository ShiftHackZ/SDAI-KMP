package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.file.MNN_INFERENCE_LOCK_SUFFIX
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import java.io.File
import java.io.RandomAccessFile
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import java.nio.channels.OverlappingFileLockException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Activates validated MNN archives without exposing partial weights or deleting a leased root.
 *
 * @param descriptor App-private local model root.
 * @param validator Strict full bundle integrity checker.
 * @param configuration Selected immutable custom identity; preparation never changes it.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidMnnModelFileStore(
    private val descriptor: FileProviderDescriptor,
    private val validator: MnnBundleValidator,
    private val configuration: ConfigurationStore
) : MnnModelFileStore {
    init {
        pruneUnselectedCustomSnapshots()
        File(descriptor.localModelDirPath).listFiles()?.forEach { file ->
            if (file.name.startsWith(".mnn-staging-") || file.name.startsWith(".mnn-download-") ||
                file.name.startsWith(".mnn-retired-") || file.name.startsWith(".mnn-custom-")
            ) {
                file.deleteRecursively()
            } else if (file.isDirectory && file.name.startsWith("mnn_") &&
                File(file, ".delete-requested").exists()
            ) {
                delete(file.name)
            }
        }
    }

    /**
     * Resolves an identifier without permitting path traversal or external file ownership.
     *
     * @param id Safe MNN catalog identifier.
     * @param activationId Exact custom manifest hash; empty resolves the committed selection.
     *
     * @author Dmitriy Moroz
     */
    override fun directory(id: String, activationId: String): File {
        val ownedId = if (id == LocalAiModel.CustomMnn.id) "mnn_custom" else id
        require(ownedId.matches(Regex("mnn_[A-Za-z0-9_-]+")))
        val root = File(descriptor.localModelDirPath, ownedId).canonicalFile
        if (id != LocalAiModel.CustomMnn.id) return root
        val identity = activationId.ifEmpty { configuration.localMnnCustomActivationId }
        require(identity.isEmpty() || identity.matches(Regex("[0-9a-f]{64}")))
        return File(root, identity.ifEmpty { "unselected" })
    }

    /**
     * Creates or reuses a content-addressed custom root without replacing any selected snapshot.
     *
     * @param sourcePath Existing custom-folder path, whose source bytes are preserved.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun importCustom(sourcePath: String): String {
        val source = File(sourcePath).canonicalFile
        val manifest = validator(source.path)
        val identity = manifest.activationId
        val existing = directory(LocalAiModel.CustomMnn.id, identity)
        if (File(existing, ".complete").isFile && !File(existing, ".delete-requested").exists()) {
            acquire(LocalAiModel.CustomMnn.id, identity).use { validator(existing.path) }
            return identity
        }
        val archive = File(descriptor.localModelDirPath, ".mnn-custom-${UUID.randomUUID()}.zip")
        check(archive.parentFile!!.mkdirs() || archive.parentFile!!.isDirectory)
        try {
            ZipOutputStream(archive.outputStream()).use { zip ->
                zip.setLevel(0)
                (manifest.files.map { it.path } + "manifest.json").forEach { name ->
                    currentCoroutineContext().ensureActive()
                    zip.putNextEntry(ZipEntry(name))
                    File(source, name).inputStream().use { input ->
                        val buffer = ByteArray(65_536)
                        while (true) {
                            currentCoroutineContext().ensureActive()
                            val count = input.read(buffer)
                            if (count < 0) break
                            zip.write(buffer, 0, count)
                        }
                    }
                    zip.closeEntry()
                }
            }
            activateCustom(identity, archive)
            return identity
        } finally {
            archive.delete()
        }
    }

    /**
     * Reports only a complete owned root; custom setup entries remain selectable separately.
     *
     * @param id Catalog identity or the committed custom UI identity.
     *
     * @author Dmitriy Moroz
     */
    override fun isDownloaded(id: String): Boolean = directory(id).let {
        File(it, ".complete").isFile && !File(it, ".delete-requested").exists()
    }

    /**
     * Acquires host ownership before validation and submission; child reader locks independently guard later cleanup.
     *
     * @param id Catalog identity.
     * @param activationId Captured immutable manifest identity for custom snapshots.
     *
     * @author Dmitriy Moroz
     */
    override fun acquire(id: String, activationId: String): AutoCloseable {
        val root = directory(id, activationId)
        val file = lockFile(root)
        val lock = try {
            file.channel.tryLock()
        } catch (_: OverlappingFileLockException) {
            null
        }
        if (lock == null) {
            file.close(); error("MNN bundle is busy")
        }
        if (!File(root, ".complete").isFile || File(root, ".delete-requested").exists()) {
            lock.release(); file.close(); error("MNN bundle is unavailable")
        }
        return AutoCloseable {
            file.use {
                lock.use {
                    if (File(root, ".delete-requested").exists()) withInferenceReservation(root) {
                        check(root.deleteRecursively()) { "MNN model deletion failed" }
                    }
                }
            }
        }
    }

    /**
     * Excludes deletion until domain finishes persisting this exact model selection.
     *
     * @param id Catalog or custom UI identity.
     * @param activationId Captured immutable custom identity.
     *
     * @author Dmitriy Moroz
     */
    override fun acquireSelection(id: String, activationId: String): AutoCloseable =
        reserveSelection(directory(id, activationId))

    /**
     * Defers deletion while an attempt owns the unchanged path and payload files.
     *
     * @param id Catalog identity.
     *
     * @author Dmitriy Moroz
     */
    override fun delete(id: String) {
        if (id == LocalAiModel.CustomMnn.id || id == "mnn_custom") {
            val roots = File(descriptor.localModelDirPath, "mnn_custom").listFiles()
                ?.filter(File::isDirectory)?.sortedBy { it.name }.orEmpty()
            withSelectionReservations(roots) { roots.forEach(::deleteReservedRoot) }
        } else deleteRoot(directory(id))
    }


    /**
     * Extracts only flat regular payload names under a bounded private staging root.
     *
     * @param id Catalog identity.
     * @param archive Downloaded ZIP that remains private until validation completes.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun activate(id: String, archive: File) {
        activateRoot(directory(id), archive)
    }

    private suspend fun activateCustom(identity: String, archive: File) {
        activateRoot(directory(LocalAiModel.CustomMnn.id, identity), archive)
    }

    private suspend fun activateRoot(root: File, archive: File) {
        check(root.parentFile!!.mkdirs() || root.parentFile!!.isDirectory)
        val staging = File(root.parentFile, ".mnn-staging-${UUID.randomUUID()}")
        check(staging.mkdirs())
        try {
            rejectSymbolicLinkEntries(archive)
            ZipFile(archive).use { zip ->
                val entries = zip.entries().toList()
                require(entries.size == 10 && entries.map { it.name }.toSet().size == 10)
                var total = 0L
                entries.forEach { entry ->
                    currentCoroutineContext().ensureActive()
                    require(!entry.isDirectory && entry.name.matches(Regex("[A-Za-z0-9_.-]+")))
                    require(entry.name != "." && entry.name != ".." && entry.size in 1..2_000_000_000)
                    val destination = File(staging, entry.name)
                    zip.getInputStream(entry).use { input ->
                        destination.outputStream().use { output ->
                            val buffer = ByteArray(65_536)
                            var countForFile = 0L
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                total += count; countForFile += count
                                require(total <= 2_000_000_000 && countForFile <= entry.size)
                                output.write(buffer, 0, count)
                            }
                            require(countForFile == entry.size)
                        }
                    }
                }
            }
            val manifest = validator(staging.path)
            if (root.parentFile?.name == "mnn_custom") require(root.name == manifest.activationId)
            currentCoroutineContext().ensureActive()
            File(staging, ".complete").writeText(manifest.activationId)
            lockFile(root).use { file ->
                val lock = try {
                    file.channel.tryLock()
                } catch (_: OverlappingFileLockException) {
                    null
                }
                check(lock != null) { "MNN model is in use" }
                lock.use {
                    check(withInferenceReservation(root) {
                        val retired = File(root.parentFile, ".mnn-retired-${UUID.randomUUID()}")
                        if (root.exists()) {
                            // Existing complete bundles are immutable; explicit deletion precedes replacement.
                            check(
                                !File(root, ".complete").isFile || File(
                                    root,
                                    ".delete-requested"
                                ).exists()
                            ) { "MNN model already downloaded" }
                            check(root.renameTo(retired)) { "MNN previous model retirement failed" }
                        }
                        if (!staging.renameTo(root)) {
                            if (retired.exists()) check(retired.renameTo(root)) { "MNN previous model restoration failed" }
                            error("MNN activation failed")
                        }
                        check(retired.deleteRecursively()) { "MNN retired model cleanup failed" }
                    }) { "MNN inference owns the model" }
                }
            }
        } finally {
            staging.deleteRecursively()
        }
    }

    private fun reserveSelection(root: File): AutoCloseable {
        val file = selectionLockFile(root)
        val lock = try {
            file.channel.tryLock()
        } catch (_: OverlappingFileLockException) {
            null
        }
        if (lock == null) {
            file.close(); error("MNN model selection is busy")
        }
        return AutoCloseable {
            try {
                lock.release()
            } finally {
                file.close()
            }
        }
    }


    private fun deleteRoot(root: File) {
        if (!root.exists()) return
        withSelectionReservations(listOf(root)) { deleteReservedRoot(root) }
    }

    /**
     * Reserves every target before mutation so a busy later snapshot cannot cause partial custom deletion.
     *
     * @param roots Stable owned snapshot roots selected for this deletion operation.
     * @param action Deletion work performed only after all selection reservations succeed.
     *
     * @author Dmitriy Moroz
     */
    private fun withSelectionReservations(roots: List<File>, action: () -> Unit) {
        val reservations = mutableListOf<AutoCloseable>()
        var operationFailure: Throwable? = null
        var releaseFailure: Throwable? = null
        try {
            roots.forEach { reservations += reserveSelection(it) }
            action()
        } catch (failure: Throwable) {
            operationFailure = failure
        } finally {
            reservations.asReversed().forEach { reservation ->
                try {
                    reservation.close()
                } catch (failure: Throwable) {
                    val previous = operationFailure ?: releaseFailure
                    if (previous == null) releaseFailure = failure else previous.addSuppressed(
                        failure
                    )
                }
            }
        }
        (operationFailure ?: releaseFailure)?.let { throw it }
    }

    private fun deleteReservedRoot(root: File) {
        if (!root.exists()) return
        File(root, ".delete-requested").writeText("")
        lockFile(root).use { file ->
            val lock = try {
                file.channel.tryLock()
            } catch (_: OverlappingFileLockException) {
                null
            }
            lock?.use {
                withInferenceReservation(root) {
                    check(root.deleteRecursively()) { "MNN model deletion failed" }
                }
            }
        }
    }

    /**
     * Reserves child reader ownership before deleting or replacing payload bytes.
     * A busy child defers deletion even after host cancellation or restart. Actual
     * lock, IO and mutation failures propagate instead of being classified as busy.
     *
     * @param root Owned catalog or immutable custom root whose sibling lock is stable.
     * @param action Mutation performed only while no child model reader can enter.
     *
     * @author Dmitriy Moroz
     */
    private fun withInferenceReservation(root: File, action: () -> Unit): Boolean {
        RandomAccessFile(
            File(root.parentFile, ".${root.name}$MNN_INFERENCE_LOCK_SUFFIX"),
            "rw"
        ).use { file ->
            val lock = try {
                file.channel.tryLock()
            } catch (_: OverlappingFileLockException) {
                null
            } ?: return false
            lock.use { action() }
            return true
        }
    }


    private fun pruneUnselectedCustomSnapshots() {
        val selected = configuration.localMnnCustomActivationId
        File(descriptor.localModelDirPath, "mnn_custom").listFiles()?.filter(File::isDirectory)
            ?.filter { it.name != selected || File(it, ".delete-requested").exists() }
            ?.forEach(::deleteRoot)
    }

    private fun selectionLockFile(root: File): RandomAccessFile {
        check(root.parentFile!!.mkdirs() || root.parentFile!!.isDirectory)
        return RandomAccessFile(File(root.parentFile, ".${root.name}.selection.lock"), "rw")
    }

    private fun lockFile(root: File): RandomAccessFile {
        check(root.parentFile!!.mkdirs() || root.parentFile!!.isDirectory)
        return RandomAccessFile(File(root.parentFile, ".${root.name}.lock"), "rw")
    }

    /**
     * Reads central-directory mode bits because java.util.zip does not expose Unix symlink attributes.
     *
     * @param archive Bounded, non-ZIP64 v1 profile archive.
     *
     * @author Dmitriy Moroz
     */
    private fun rejectSymbolicLinkEntries(archive: File) {
        require(archive.length() in 22..2_000_000_000)
        RandomAccessFile(archive, "r").use { input ->
            val tailSize = minOf(input.length(), 65_557).toInt()
            val tail = ByteArray(tailSize)
            input.seek(input.length() - tailSize)
            input.readFully(tail)
            val buffer = ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN)
            val end = (tailSize - 22 downTo 0).firstOrNull { buffer.getInt(it) == 0x06054b50 }
                ?: error("MNN ZIP directory missing")
            require(buffer.getShort(end + 4).toInt() == 0 && buffer.getShort(end + 6).toInt() == 0)
            val count = buffer.getShort(end + 10).toInt() and 0xffff
            require(count == 10 && (buffer.getShort(end + 8).toInt() and 0xffff) == count)
            val directoryOffset = buffer.getInt(end + 16).toLong() and 0xffffffffL
            require(directoryOffset < input.length())
            input.seek(directoryOffset)
            repeat(count) {
                val header = ByteArray(46)
                input.readFully(header)
                val fields = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
                require(fields.getInt(0) == 0x02014b50)
                require((fields.getShort(8).toInt() and 1) == 0)
                val unixMode = fields.getInt(38).ushr(16)
                require(unixMode and 0xf000 != 0xa000) { "MNN ZIP symlinks are unsupported" }
                val variableLength =
                    listOf(28, 30, 32).sumOf { fields.getShort(it).toInt() and 0xffff }
                input.seek(input.filePointer + variableLength)
            }
        }
    }
}
