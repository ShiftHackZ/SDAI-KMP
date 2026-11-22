package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.usecase.backup.GetBackupStartupStageUseCase

/**
 * Persists or reads the durable post-onboarding recovery stage.
 *
 * @param preferences Checked stage persistence shared with startup routing.
 * @author Dmitriy Moroz
 */
internal class GetBackupStartupStageUseCaseImpl(private val preferences: BackupPreferencesGateway) :
    GetBackupStartupStageUseCase {

    override operator fun invoke(): BackupStartupStage = preferences.readStartupStage()
}
