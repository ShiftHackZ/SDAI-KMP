package com.shifthackz.aisdv1.feature.backup.archive

/**
 * Initializes private staging once per process before any archive operation may create files.
 * Journal-owned archives survive cleanup; later calls never rescan files owned by active operations.
 *
 * @author Dmitriy Moroz
 */
internal interface BackupStagingCleaner {
    /**
     * Removes generated staging left by a previous process, preserving every pending restore handle.
     * Concurrent callers wait for the same initialization; failure propagates without marking it complete.
     *
     * @author Dmitriy Moroz
     */
    suspend fun prepare()
}
