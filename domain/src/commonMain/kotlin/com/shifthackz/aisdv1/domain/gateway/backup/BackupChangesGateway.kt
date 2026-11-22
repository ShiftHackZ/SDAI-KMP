package com.shifthackz.aisdv1.domain.gateway.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory

/**
 * Refreshes retained application readers after restore commits or checked preference recovery.
 *
 * @author Dmitriy Moroz
 */
interface BackupChangesGateway {
    /**
     * Refreshes settings, locale and storage snapshots against their current trusted persisted data.
     *
     * @param categories Categories affected by restore or pending preference recovery; empty requires no refresh.
     * @author Dmitriy Moroz
     */
    suspend fun notifyChanged(categories: Set<BackupCategory>)
}
