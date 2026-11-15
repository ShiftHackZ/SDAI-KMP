package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Repeats an exact failed batch, resolves protected staging and publishes only confirmed settings.
 *
 * @param configurationStore Owns checked configuration persistence and its pending in-memory intent.
 * @param authorizationStore Owns encrypted staging/recovery.
 * @param selectionGuard Serializes against configuration replacement and connection snapshots.
 * @param preferences Publishes completed recovery.
 * @param dispatchers Keeps persistence off the caller's thread.
 * @author Dmitriy Moroz
 */
internal class RetryAuthorizationStorageUseCaseImpl(
    private val configurationStore: ConfigurationStore,
    private val authorizationStore: AuthorizationStore,
    private val selectionGuard: ProviderSelectionGuard,
    private val preferences: PreferenceManager,
    private val dispatchers: DispatchersProvider,
) : RetryAuthorizationStorageUseCase {
    /**
     * Completes the previously requested durable transition; never repeats generation/network calls.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = withContext(dispatchers.io) {
        selectionGuard.withSelection {
            withContext(NonCancellable) {
                configurationStore.retryPendingConfiguration()
                authorizationStore.getAuthorizationCredentials()
                preferences.refresh()
            }
        }
    }
}
