package com.shifthackz.aisdv1.presentation.statistics.storageusage.platform

import com.shifthackz.aisdv1.presentation.components.model.StorageUsageByteMapper

/**
 * Platform bridge for filesystem storage usage operations.
 *
 * Common code owns the screen behavior, while platform implementations know how to read app cache
 * directories and downloaded model files safely on their filesystem.
 *
 * @author Dmitriy Moroz
 */
interface StorageUsagePlatformActions : StorageUsageByteMapper {
    /**
     * Returns the current app cache directory size in bytes.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getAppCacheBytes(): Long

    /**
     * Deletes app cache files. Callers are responsible for showing confirmation first.
     *
     * @author Dmitriy Moroz
     */
    suspend fun clearAppCache()


    /**
     * Returns total bytes occupied by downloaded model directories for [modelIds].
     *
     * @param modelIds Local model identifiers whose downloaded directories should be measured.
     *
     * @author Dmitriy Moroz
     */
    suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long
}
