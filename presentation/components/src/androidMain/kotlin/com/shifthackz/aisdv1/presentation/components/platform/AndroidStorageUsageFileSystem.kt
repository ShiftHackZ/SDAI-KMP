package com.shifthackz.aisdv1.presentation.components.platform

import android.content.Context
import java.io.File

/**
 * Uses the Android app's existing cache and downloaded-model directory accounting for both usage views.
 *
 * @param context Application context owning the private cache and files directories.
 * @author Dmitriy Moroz
 */
internal class AndroidStorageUsageFileSystem(
    private val context: Context,
) : StorageUsageFileSystem {
    override fun getAppCacheBytes(): Long = context.cacheDir.directorySize()

    override fun mapStorageBytesForUi(bytes: Long): Long = bytes

    override fun clearAppCache() {
        context.cacheDir.listFiles()?.forEach { file ->
            runCatching { file.deleteRecursively() }
        }
    }

    override fun getDownloadedModelsBytes(modelIds: List<String>): Long {
        val modelDir = File(context.filesDir, LOCAL_MODEL_DIRECTORY)
        return modelIds.distinct().sumOf { id -> File(modelDir, id).directorySize() }
    }

    private fun File.directorySize(): Long {
        if (!exists()) return 0L
        if (isFile) return length()
        return listFiles()?.sumOf { child -> child.directorySize() } ?: 0L
    }

    private companion object {
        const val LOCAL_MODEL_DIRECTORY = "model"
    }
}
