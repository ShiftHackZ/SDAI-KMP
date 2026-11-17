package com.shifthackz.aisdv1.feature.mnn

import android.system.Os
import android.system.OsConstants
import java.io.File

/**
 * Uses Android's no-follow stat operation so cache links cannot expand the deletion boundary.
 *
 * @author Dmitriy Moroz
 */
internal class MnnCacheCleanerImpl : MnnCacheCleaner {
    /**
     * Propagates listing, stat and removal errors to the cache owner.
     *
     * @param directory Owned root or child entry visited without following symlinks.
     *
     * @author Dmitriy Moroz
     */
    override fun delete(directory: File) {
        val attributes = Os.lstat(directory.path)
        if (OsConstants.S_ISDIR(attributes.st_mode)) {
            checkNotNull(directory.listFiles()) { "MNN cache directory cannot be listed" }
                .forEach(::delete)
        }
        check(directory.delete()) { "MNN cache entry cannot be removed" }
    }
}
