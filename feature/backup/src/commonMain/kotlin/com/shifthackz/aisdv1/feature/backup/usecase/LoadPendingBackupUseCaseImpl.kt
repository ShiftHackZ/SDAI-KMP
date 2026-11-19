package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupRecovery
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import kotlinx.coroutines.NonCancellable
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.usecase.backup.LoadPendingBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.archive.BackupStagingCleaner
import com.shifthackz.aisdv1.feature.backup.restore.BackupSessionCodec
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import kotlinx.coroutines.withContext

/**
 * Recovers checked credential association before exposing the staged unfinished operation.
 * @param preferences Recovers an already-started checked typed settings batch before startup or abandonment.
 * @param database Restore intent ledger.
 * @param archives Revalidates retained private archive bytes.
 * @param sessions Decodes nonsecret category/provider selection.
 * @param configuration Retries an exact failed checked preference write.
 * @param authorization Recovers protected credential association before normal app publication.
 * @param guard Serializes recovery with provider transitions.
 * @param dispatchers Runs disk and protected-store work on IO.
 * @param staging Cleans previous-process orphan files once, retaining every pending restore archive.
 * @param changes Refreshes retained preference readers after protected association recovery.
 * @author Dmitriy Moroz
 */
internal class LoadPendingBackupUseCaseImpl(
    private val database: BackupStorage,
    private val preferences: BackupPreferencesGateway,
    private val archives: BackupArchiveStore,
    private val sessions: BackupSessionCodec,
    private val configuration: ConfigurationStore,
    private val authorization: AuthorizationStore,
    private val guard: ProviderSelectionGuard,
    private val dispatchers: DispatchersProvider,
    private val staging: BackupStagingCleaner,
    private val changes: BackupChangesGateway,
) : LoadPendingBackupUseCase {

    override suspend fun invoke(): BackupRecovery? = withContext(dispatchers.io) {
        staging.prepare()
        val pending =
            database.restoreSessionDao().pending().firstOrNull() ?: return@withContext null
        val selection = sessions.decode(pending.selection)
        guard.withSelection {
            val recovered =
                selection.categories.filter { it == BackupCategory.PROVIDERS || it == BackupCategory.SETTINGS }
                    .toMutableSet()
            if (configuration.retryPendingConfiguration()) recovered += BackupCategory.PROVIDERS
            recovered += preferences.recoverPendingWrite()
            authorization.getAuthorizationCredentials()
            withContext(NonCancellable) { changes.notifyChanged(recovered) }
        }
        val completed = database.restoreSessionDao().completed(pending.id).toSet()
        val hasCommittedSelection = selection.categories.all { it.key in completed }
        if (pending.requiresConfiguration && (pending.isComplete || hasCommittedSelection || preferences.readStartupStage() == BackupStartupStage.CONFIGURATION)) {
            withContext(NonCancellable) {
                if (preferences.readStartupStage() in setOf(
                        BackupStartupStage.INVITATION,
                        BackupStartupStage.RESTORE
                    )
                ) preferences.writeStartupStage(BackupStartupStage.CONFIGURATION)
                database.restoreSessionDao().finish(pending.id)
                database.restoreSessionDao().acknowledgeConfiguration(pending.id)
                archives.discard(pending.archiveHandle)
            }
            return@withContext null
        }
        val archive = archives.staged(pending.archiveHandle)
        BackupRecovery(
            BackupRestoreRequest(
                pending.archiveHandle,
                selection,
                pending.id,
                shouldProceedToConfiguration = pending.requiresConfiguration
            ), archives.summary(archive)
        )
    }
}
