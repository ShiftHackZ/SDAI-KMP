package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.mnn.MnnFileMetadata

/**
 * Reads bundle metadata without following symbolic links; hashes are streamed in bounded chunks.
 *
 * @author Dmitriy Moroz
 */
interface MnnBundleFileSystem {
    /**
     * Resolves platform aliases in the trusted bundle root before checking its children.
     *
     * @param path Absolute model root supplied by owned storage or validated custom selection.
     * @return Canonical root used for every subsequent regular-file check.
     *
     * @author Dmitriy Moroz
     */
    @Throws(Exception::class)
    fun canonicalRoot(path: String): String

    /**
     * Reads the bounded UTF-8 integrity manifest.
     *
     * @param path Absolute owned manifest path.
     * @return Complete JSON text; missing, linked or oversized files fail.
     * @author Dmitriy Moroz
     */
    @Throws(Exception::class)
    fun readManifest(path: String): String

    /**
     * Computes the complete file digest.
     *
     * @param path Absolute regular payload path.
     * @return Lowercase SHA256; IO failures propagate.
     * @author Dmitriy Moroz
     */
    @Throws(Exception::class)
    fun sha256(path: String): String

    /**
     * Rejects links, directories and missing paths.
     *
     * @param path Absolute payload path.
     * @return Whether this path is a regular unlinked file.
     * @author Dmitriy Moroz
     */
    fun isRegularFile(path: String): Boolean

    /**
     * Inspects a regular file and returns its exact byte length.
     *
     * @param path Absolute regular payload path.
     * @return File metadata; missing files and IO errors fail.
     * @author Dmitriy Moroz
     */
    @Throws(Exception::class)
    fun metadata(path: String): MnnFileMetadata

    /**
     * Enumerates immediate children without recursion.
     *
     * @param path Absolute bundle root.
     * @return Every child filename; listing failures propagate.
     * @author Dmitriy Moroz
     */
    @Throws(Exception::class)
    fun entries(path: String): List<String>
}
