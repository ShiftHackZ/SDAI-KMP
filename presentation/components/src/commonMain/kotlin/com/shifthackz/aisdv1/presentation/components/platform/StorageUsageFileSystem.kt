package com.shifthackz.aisdv1.presentation.components.platform

/**
 * Measures app-private cache and explicitly selected catalog model directories for usage screens.
 * No operation targets custom-model source files in user-managed storage.
 *
 * @author Dmitriy Moroz
 */
interface StorageUsageFileSystem {
    /**
     * Measures the app cache directory in bytes using the platform's existing accounting rules.
     * @author Dmitriy Moroz
     */
    fun getAppCacheBytes(): Long

    /**
     * Converts filesystem residue to the amount displayed by the platform's usage UI.
     * @param bytes Raw measured byte count.
     * @author Dmitriy Moroz
     */
    fun mapStorageBytesForUi(bytes: Long): Long

    /**
     * Removes app-private cache children while retaining the cache directory itself.
     * @author Dmitriy Moroz
     */
    fun clearAppCache()

    /**
     * Measures the distinct app-private catalog model directories selected by the caller.
     * @param modelIds Catalog model directory identifiers, excluding custom models.
     * @author Dmitriy Moroz
     */
    fun getDownloadedModelsBytes(modelIds: List<String>): Long
}
