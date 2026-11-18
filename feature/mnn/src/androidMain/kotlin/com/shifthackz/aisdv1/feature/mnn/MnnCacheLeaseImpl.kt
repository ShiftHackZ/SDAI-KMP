package com.shifthackz.aisdv1.feature.mnn

import java.io.File
import java.io.RandomAccessFile
import java.nio.channels.FileLock

/**
 * Deletes an attempt before releasing its OS ownership; use preserves any primary exception.
 *
 * @param directory Disposable private files whose native mappings have already been destroyed.
 * @param owner Open channel keeping the cross-process lock alive.
 * @param lock Exclusive ownership acquired before stale attempt cleanup.
 * @param cleaner Deletes only this attempt tree without following links.
 *
 * @author Dmitriy Moroz
 */
internal class MnnCacheLeaseImpl(
    override val directory: File,
    private val owner: RandomAccessFile,
    private val lock: FileLock,
    private val cleaner: MnnCacheCleaner,
) : MnnCacheLease {
    /**
     * Reports cleanup failure while always closing the lock and its channel.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        owner.use {
            lock.use {
                cleaner.delete(directory)
            }
        }
    }
}
