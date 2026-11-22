package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.usecase.backup.SetBackupStartupStageUseCase

/**
 * Advances durable recovery progression so a stale screen cannot reopen completed provider setup.
 *
 * @param preferences Checked stage persistence shared with startup routing.
 * @author Dmitriy Moroz
 */
internal class SetBackupStartupStageUseCaseImpl(private val preferences: BackupPreferencesGateway) :
    SetBackupStartupStageUseCase {

    override operator fun invoke(stage: BackupStartupStage) {
        if (stage.ordinal < preferences.readStartupStage().ordinal) return
        preferences.writeStartupStage(stage)
    }
}
