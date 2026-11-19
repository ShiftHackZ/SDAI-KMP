package com.shifthackz.aisdv1.domain.usecase.splash

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.backup.GetBackupStartupStageUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.LoadPendingBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.GetPendingBackupExportUseCase

/**
 * Routes unfinished recovery before publishing normal provider state; existing installations skip the invitation.
 *
 * @param preferenceManager Existing onboarding and provider prerequisites.
 * @param getBackupStartupStage Durable fresh-install recovery progression.
 * @param loadPendingBackup Revalidates pending restore intent and recovers checked credential associations.
 * @param getPendingExport Checks for a staged native export that needs its backup route restored.
 *
 * @author Dmitriy Moroz
 */
internal class SplashNavigationUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val getBackupStartupStage: GetBackupStartupStageUseCase,
    private val loadPendingBackup: LoadPendingBackupUseCase,
    private val getPendingExport: GetPendingBackupExportUseCase,
) : SplashNavigationUseCase {

    override suspend fun invoke(): SplashNavigationUseCase.Action = when {
        !preferenceManager.onBoardingComplete -> SplashNavigationUseCase.Action.LAUNCH_ONBOARDING
        loadPendingBackup() != null -> if (getBackupStartupStage() == BackupStartupStage.RESTORE) SplashNavigationUseCase.Action.LAUNCH_INITIAL_RESTORE else SplashNavigationUseCase.Action.LAUNCH_BACKUP_RESTORE
        getBackupStartupStage() == BackupStartupStage.INVITATION -> SplashNavigationUseCase.Action.LAUNCH_BACKUP_WELCOME
        getBackupStartupStage() == BackupStartupStage.RESTORE -> SplashNavigationUseCase.Action.LAUNCH_INITIAL_RESTORE
        getBackupStartupStage() == BackupStartupStage.CONFIGURATION -> SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP
        getPendingExport() -> SplashNavigationUseCase.Action.LAUNCH_BACKUP_RESTORE
        preferenceManager.forceSetupAfterUpdate -> SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP
        preferenceManager.automatic1111ServerUrl.isEmpty() && preferenceManager.source == ServerSource.AUTOMATIC1111 -> SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP
        else -> SplashNavigationUseCase.Action.LAUNCH_HOME
    }
}
