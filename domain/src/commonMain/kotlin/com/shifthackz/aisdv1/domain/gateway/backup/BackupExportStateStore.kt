package com.shifthackz.aisdv1.domain.gateway.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupExportIntent

/**
 * Checked nonsecret export persistence isolated from provider and credential preferences.
 * @author Dmitriy Moroz
 */
interface BackupExportStateStore {
    /**
     * Recovers only a pending export-state write before ownership or native outcomes are examined.
     * @author Dmitriy Moroz
     */
    fun recoverPendingWrite()

    /**
     * Returns the pending source/result association, or null when no export owns staging.
     * @author Dmitriy Moroz
     */
    fun read(): BackupExportIntent?

    /**
     * Checks durable publication before native presentation or destination copying starts.
     * @param intent Nonsecret staged-source and output association.
     * @author Dmitriy Moroz
     */
    fun write(intent: BackupExportIntent)

    /**
     * Acknowledges dismissal/cancellation before app-owned export staging may be removed.
     * @author Dmitriy Moroz
     */
    fun clear()
}
