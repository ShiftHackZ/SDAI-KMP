package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage

/**
 * Persists a forward first-launch transition before navigation; stale earlier stages are ignored.
 * @author Dmitriy Moroz
 */
interface SetBackupStartupStageUseCase {
    /**
     * Persists a forward first-launch transition before navigation; stale earlier stages are ignored.
     * @param stage New recovery stage.
     * @author Dmitriy Moroz
     */
    operator fun invoke(stage: BackupStartupStage)
}
