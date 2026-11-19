package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.gateway.backup.BackupChangesGateway
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.usecase.backup.DiscardBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.feature.backup.restore.BackupRestoreGuard
import com.shifthackz.aisdv1.feature.backup.restore.BackupSessionCodec
import com.shifthackz.aisdv1.storage.gateway.BackupStorage
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Abandons an unfinished operation only after checked configuration/credentials are trusted; removes private staging only.
 * @param archives Owns generated private archive paths.
 * @param preferences Recovers an already-started checked typed settings batch before startup or abandonment.
 * @param database Durable pending intent records.
 * @param configuration Checked preference quarantine owner.
 * @param authorization Protected Basic Auth recovery owner.
 * @param selectionGuard Serializes association recovery before abandonment.
 * @param restoreGuard Prevents deletion during active import.
 * @param dispatchers Runs cleanup on IO.
 * @param sessions Reads category intent when a previous replay committed without a section marker.
 * @param changes Refreshes retained preference readers after protected association recovery.
 * @author Dmitriy Moroz
 */
internal class DiscardBackupUseCaseImpl(
    private val archives: BackupArchiveStore,
    private val database: BackupStorage,
    private val preferences: BackupPreferencesGateway,
    private val configuration: ConfigurationStore,
    private val authorization: AuthorizationStore,
    private val selectionGuard: ProviderSelectionGuard,
    private val restoreGuard: BackupRestoreGuard,
    private val dispatchers: DispatchersProvider,
    private val sessions: BackupSessionCodec,
    private val changes: BackupChangesGateway,
) : DiscardBackupUseCase {

    override suspend fun invoke(handle: String) = withContext(dispatchers.io) {
        restoreGuard.exclusive {
            val pending =
                database.restoreSessionDao().pending().filter { it.archiveHandle == handle }
            if (pending.isNotEmpty()) selectionGuard.withSelection {
                val recovered = pending.flatMap { sessions.decode(it.selection).categories }
                    .filter { it == BackupCategory.PROVIDERS || it == BackupCategory.SETTINGS }
                    .toMutableSet()
                if (configuration.retryPendingConfiguration()) recovered += BackupCategory.PROVIDERS
                recovered += preferences.recoverPendingWrite()
                authorization.getAuthorizationCredentials()
                withContext(NonCancellable) { changes.notifyChanged(recovered) }
                if (pending.any { it.requiresConfiguration } && preferences.readStartupStage() in setOf(
                        BackupStartupStage.INVITATION,
                        BackupStartupStage.RESTORE
                    )) preferences.writeStartupStage(
                    BackupStartupStage.CONFIGURATION
                )
                pending.forEach {
                    database.restoreSessionDao().finish(it.id)
                    if (it.requiresConfiguration) database.restoreSessionDao()
                        .acknowledgeConfiguration(it.id)
                }
            }
            archives.discard(handle)
        }
    }
}
