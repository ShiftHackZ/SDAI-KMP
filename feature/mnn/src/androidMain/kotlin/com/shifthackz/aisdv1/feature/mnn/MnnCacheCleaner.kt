package com.shifthackz.aisdv1.feature.mnn

import java.io.File

/**
 * Removes an owned attempt tree without following links inside it.
 *
 * @author Dmitriy Moroz
 */
internal interface MnnCacheCleaner {
    /**
     * Removes files and links themselves, visiting only directories identified by lstat.
     *
     * @param directory Owned private attempt root, never a model or shared download directory.
     *
     * @author Dmitriy Moroz
     */
    fun delete(directory: File)
}
