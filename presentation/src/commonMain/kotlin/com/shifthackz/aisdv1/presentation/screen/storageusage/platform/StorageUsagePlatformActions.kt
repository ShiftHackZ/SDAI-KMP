package com.shifthackz.aisdv1.presentation.screen.storageusage.platform

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.model.StorageUsageByteMapper

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

/**
 * Fallback implementation for previews and platforms where storage inspection is unavailable.
 *
 * @author Dmitriy Moroz
 */
object NoOpStorageUsagePlatformActions : StorageUsagePlatformActions {
    override suspend fun getAppCacheBytes(): Long = 0L

    override suspend fun clearAppCache() = Unit


    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long = 0L
}

/**
 * Remembers the platform-specific storage bridge used by the storage usage screen.
 *
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberStorageUsagePlatformActions(): StorageUsagePlatformActions
