package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupRecovery

/**
 * Recovers unfinished checked preferences before allowing normal startup and returns resumable restore state.
 *
 * @author Dmitriy Moroz
 */
interface LoadPendingBackupUseCase {
    /**
     * Loads one durable pending operation, or null when startup may continue normally.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): BackupRecovery?
}
