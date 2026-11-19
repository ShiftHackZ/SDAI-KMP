package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage

/**
 * Reads durable recovery routing without starting work or changing the active provider.
 * @author Dmitriy Moroz
 */
interface GetBackupStartupStageUseCase {
    /**
     * Reads durable recovery routing without starting work or changing the active provider.
     * @author Dmitriy Moroz
     */
    operator fun invoke(): BackupStartupStage
}
