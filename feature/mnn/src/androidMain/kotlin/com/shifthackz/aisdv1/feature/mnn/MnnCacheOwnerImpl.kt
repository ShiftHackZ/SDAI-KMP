package com.shifthackz.aisdv1.feature.mnn

import android.content.Context
import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock
import java.util.UUID

/**
 * Serializes private MNN working storage across child restarts; process death releases the OS lock.
 * Active mmap backing and output live outside purgeable application cache so cache clearing cannot
 * unlink the owner lock or invalidate a running attempt. Completed and stale attempts are removed.
 *
 * @param context Child process context locating application storage excluded from backups.
 * @param cleaner Removes stale attempts without traversing symlinks.
 *
 * @author Dmitriy Moroz
 */
internal class MnnCacheOwnerImpl(
    private val context: Context,
    private val cleaner: MnnCacheCleaner,
) : MnnCacheOwner {
    /**
     * Removes only canonical UUID attempt directories after acquiring exclusive ownership.
     * Unknown entries and symlinks are preserved; cleanup and lock failures propagate.
     *
     * @author Dmitriy Moroz
     */
    override fun acquire(): MnnCacheLease {
        val root = File(context.noBackupFilesDir, "mnn-work").canonicalFile
        check(root.isDirectory || root.mkdirs()) { "MNN cache directory unavailable" }
        val owner = RandomAccessFile(File(root, ".owner.lock"), "rw")
        var lock: FileLock? = null
        try {
            lock = checkNotNull(owner.channel.tryLock()) { "MNN cache already owned" }
            val entries = checkNotNull(root.listFiles()) { "MNN cache cannot be listed" }
            val attemptName = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
            entries.filter { entry ->
                attemptName.matches(entry.name) && entry.isDirectory &&
                        entry.canonicalFile == entry.absoluteFile && entry.parentFile == root
            }.forEach { entry ->
                cleaner.delete(entry)
            }
            val directory = File(root, UUID.randomUUID().toString())
            check(directory.mkdir()) { "MNN attempt directory unavailable" }
            return MnnCacheLeaseImpl(directory, owner, lock, cleaner)
        } catch (failure: Throwable) {
            try {
                lock?.release()
            } catch (cleanup: Throwable) {
                failure.addSuppressed(cleanup)
            }
            try {
                owner.close()
            } catch (cleanup: Throwable) {
                failure.addSuppressed(cleanup)
            }
            throw failure
        }
    }
}
