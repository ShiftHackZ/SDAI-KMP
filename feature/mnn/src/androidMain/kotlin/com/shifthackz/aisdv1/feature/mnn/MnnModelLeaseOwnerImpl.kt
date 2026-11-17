package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.core.common.file.MNN_INFERENCE_LOCK_SUFFIX
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException

/**
 * Holds a separate process lock so host teardown or restart cannot release a live native reader.
 *
 * @param modelDirectory Canonical application-owned model directory configured by the child graph.
 *
 * @author Dmitriy Moroz
 */
internal class MnnModelLeaseOwnerImpl(private val modelDirectory: File) : MnnModelLeaseOwner {
    /**
     * Fails promptly on busy, unavailable or changed roots; IO and lock errors remain failures.
     *
     * @param modelPath Canonical app-owned model root supplied by validated IPC.
     * @param activationId Exact immutable manifest identity captured by the host.
     *
     * @author Dmitriy Moroz
     */
    override fun acquire(modelPath: String, activationId: String): Closeable {
        require(activationId.matches(Regex("[0-9a-f]{64}")))
        val root = File(modelPath)
        require(root.isAbsolute && root.canonicalFile == root.absoluteFile)
        val owned = modelDirectory.canonicalFile
        val isCatalogRoot = root.parentFile == owned && root.name != "mnn_custom" &&
                root.name.matches(Regex("mnn_[A-Za-z0-9_-]+"))
        val isCustomRoot = root.parentFile == File(owned, "mnn_custom") && root.name == activationId
        require(isCatalogRoot || isCustomRoot) { "MNN model must be app-owned" }
        val file =
            RandomAccessFile(File(root.parentFile, ".${root.name}$MNN_INFERENCE_LOCK_SUFFIX"), "rw")
        var lock: FileLock? = null
        try {
            lock = try {
                file.channel.tryLock()
            } catch (_: OverlappingFileLockException) {
                null
            }
            check(lock != null) { "MNN model reader is busy" }
            val completion = File(root, ".complete")
            check(
                !File(
                    root,
                    ".delete-requested"
                ).exists() && completion.isFile
            ) { "MNN model is unavailable" }
            check(completion.length() == 64L && completion.readText() == activationId) { "MNN model identity changed" }
            val ownedLock = lock
            return Closeable {
                file.use {
                    ownedLock.use { }
                }
            }
        } catch (failure: Throwable) {
            try {
                lock?.release()
            } catch (cleanup: Throwable) {
                failure.addSuppressed(cleanup)
            }
            try {
                file.close()
            } catch (cleanup: Throwable) {
                failure.addSuppressed(cleanup)
            }
            throw failure
        }
    }
}
