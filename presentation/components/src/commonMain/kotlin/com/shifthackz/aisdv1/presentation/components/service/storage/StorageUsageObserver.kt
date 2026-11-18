package com.shifthackz.aisdv1.presentation.components.service.storage

import kotlinx.coroutines.flow.Flow

/**
 * Shared invalidation source for storage usage snapshots.
 *
 * Storage size comes from several non-Room sources, including cache directories and downloaded
 * model files. Those sources do not expose flows, so writers notify this observer after mutations
 * and both Settings and the standalone storage screen reload a fresh snapshot from the same stream.
 *
 * @author Dmitriy Moroz
 */
interface StorageUsageObserver {
    /**
     * Emits storage revisions, including an initial snapshot trigger.
     *
     * @author Dmitriy Moroz
     */
    fun observe(): Flow<Long>

    /**
     * Invalidates snapshots after app-owned storage changes.
     *
     * @author Dmitriy Moroz
     */
    fun notifyChanged()
}
