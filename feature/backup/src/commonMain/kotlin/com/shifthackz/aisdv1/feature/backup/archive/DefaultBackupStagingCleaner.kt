package com.shifthackz.aisdv1.feature.backup.archive

import com.shifthackz.aisdv1.feature.backup.io.BackupFileStore
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Path

/**
 * Cleans only generated directories under the app-owned backup root before new operations are admitted.
 * A single initialization mutex prevents a concurrent create/import from allocating files during cleanup.
 *
 * @param files Platform-owned private staging root; user-selected documents are never inspected here.
 * @param database Durable restore ledger identifying archives that must remain available for recovery.
 * @param exports Durable native export ownership retained across host/process recreation.
 *
 * @author Dmitriy Moroz
 */
internal class DefaultBackupStagingCleaner(
    private val files: BackupFileStore,
    private val database: BackupStorage,
    private val exports: BackupExportStateStore,
) : BackupStagingCleaner {
    private val initialization = Mutex()
    private var hasPrepared = false

    override suspend fun prepare() = initialization.withLock {
        if (hasPrepared) return@withLock
        exports.recoverPendingWrite()
        val retained =
            database.restoreSessionDao().pending().map { it.archiveHandle }.toSet() + listOfNotNull(
                exports.read()?.handle
            )
        val root = files.root
        val metadata = FileSystem.SYSTEM.metadataOrNull(root)
        if (metadata != null) {
            check(metadata.isDirectory && metadata.symlinkTarget == null) {
                "Private backup staging root is not an owned directory"
            }
            FileSystem.SYSTEM.list(root).forEach { child ->
                currentCoroutineContext().ensureActive()
                if (child.name.matches(GENERATED_HANDLE) && child.name !in retained) {
                    deleteOwnedEntry(child)
                }
            }
        }
        hasPrepared = true
    }

    /**
     * Deletes a private tree without following symbolic links into user-managed storage.
     *
     * @param path Entry below a generated app-owned staging directory.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun deleteOwnedEntry(path: Path) {
        currentCoroutineContext().ensureActive()
        val metadata = FileSystem.SYSTEM.metadata(path)
        if (metadata.isDirectory && metadata.symlinkTarget == null) {
            FileSystem.SYSTEM.list(path).forEach { child -> deleteOwnedEntry(child) }
        }
        FileSystem.SYSTEM.delete(path)
    }

    private companion object {
        val GENERATED_HANDLE =
            Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
    }
}
