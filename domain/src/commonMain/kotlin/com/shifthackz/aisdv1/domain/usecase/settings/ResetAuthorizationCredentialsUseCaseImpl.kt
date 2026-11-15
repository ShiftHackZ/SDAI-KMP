package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Serializes explicitly confirmed credential recovery with provider selection/publication.
 *
 * @param configurationStore Resolves an untrusted configuration batch before destructive recovery.
 * @param dispatchers Keeps pending configuration persistence on IO.
 * @param authorizationStore Protected owner of the affected Basic Auth store.
 * @param selectionGuard Shared guard against conflicting provider transitions.
 * @author Dmitriy Moroz
 */
internal class ResetAuthorizationCredentialsUseCaseImpl(
    private val configurationStore: ConfigurationStore,
    private val dispatchers: DispatchersProvider,
    private val authorizationStore: AuthorizationStore,
    private val selectionGuard: ProviderSelectionGuard,
) : ResetAuthorizationCredentialsUseCase {

    /**
     * Finishes the confirmed local reset once it starts, even if the requesting screen closes.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = selectionGuard.withSelection {
        currentCoroutineContext().ensureActive()
        withContext(NonCancellable + dispatchers.io) {
            configurationStore.retryPendingConfiguration()
            authorizationStore.resetAuthorizationCredentials()
        }
    }
}
