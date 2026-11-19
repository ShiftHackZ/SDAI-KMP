package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Persists a selected provider and publishes it after credential commit completes.
 * Validation/network work belongs outside this short transition. Once persistence begins,
 * caller cancellation cannot leave changed credentials paired with the old running configuration.
 *
 * @param dispatchersProvider Keeps complete configuration persistence on IO.
 * @param configurationStore Persists provider settings and selection revision.
 * @param authorizationStore Commits protected Basic Auth before publishing its provider settings.
 * @param preferenceManager Refreshes observers after the complete transition.
 * @param providerSelectionGuard Serializes source changes and identity-sensitive publication.
 * @param backupPreferences Completes first-launch recovery only after a real provider configuration commit.
 * @author Dmitriy Moroz
 */
internal class DefaultSetServerConfigurationUseCaseImpl(
    private val dispatchersProvider: DispatchersProvider,
    private val configurationStore: ConfigurationStore,
    private val authorizationStore: AuthorizationStore,
    private val preferenceManager: PreferenceManager,
    private val providerSelectionGuard: ProviderSelectionGuard,
    private val backupPreferences: BackupPreferencesGateway,
) : SetServerConfigurationUseCase {
    /**
     * Commits Basic Auth only for providers that use it, preserving stored credentials otherwise.
     * An unavailable credential snapshot cannot be submitted as an anonymous replacement.
     *
     * @param configuration Validated provider settings and explicitly selected credentials.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(configuration: Configuration) =
        withContext(dispatchersProvider.io) {
            providerSelectionGuard.withSelection {
                currentCoroutineContext().ensureActive()
                withContext(NonCancellable) {
                    configurationStore.retryPendingConfiguration()
                    if (configuration.source == ServerSource.AUTOMATIC1111 || configuration.source == ServerSource.SWARM_UI) {
                        configuration.authorizationStorageFailure?.let {
                            throw AuthorizationStorageFailure(
                                it
                            )
                        }
                        authorizationStore.storeAuthorizationCredentials(configuration.authCredentials) { commitId ->
                            configurationStore.commitConfiguration(configuration, commitId)
                        }
                    } else {
                        configurationStore.commitConfiguration(
                            configuration,
                            configurationStore.getAuthorizationCommitId()
                        )
                    }
                    if (backupPreferences.readStartupStage() == BackupStartupStage.CONFIGURATION) {
                        backupPreferences.writeStartupStage(BackupStartupStage.COMPLETE)
                    }
                    preferenceManager.refresh()
                }
            }
        }
}
