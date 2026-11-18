package com.shifthackz.aisdv1.presentation.statistics.storageusage.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.shifthackz.aisdv1.presentation.components.platform.StorageUsageFileSystem
import org.koin.mp.KoinPlatform.getKoin

/**
 * Creates the Android storage bridge backed by app-private cache and files directories.
 *
 * @author Dmitriy Moroz
 */
@Composable
actual fun rememberStorageUsagePlatformActions(): StorageUsagePlatformActions {
    val koin = remember { getKoin() }
    val fileSystem = remember(koin) { koin.get<StorageUsageFileSystem>() }
    return remember(fileSystem) {
        AndroidStorageUsagePlatformActions(fileSystem)
    }
}

/**
 * Android implementation that measures and deletes app-private storage used by the usage screen.
 *
 * @param fileSystem Shared app-private cache and catalog-model accounting.
 *
 * @author Dmitriy Moroz
 */
private class AndroidStorageUsagePlatformActions(
    private val fileSystem: StorageUsageFileSystem,
) : StorageUsagePlatformActions {

    override suspend fun getAppCacheBytes(): Long = fileSystem.getAppCacheBytes()

    override suspend fun clearAppCache() {
        fileSystem.clearAppCache()
    }


    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long =
        fileSystem.getDownloadedModelsBytes(modelIds)
}
