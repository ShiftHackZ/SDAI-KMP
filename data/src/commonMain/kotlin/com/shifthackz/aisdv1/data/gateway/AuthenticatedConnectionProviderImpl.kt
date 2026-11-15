package com.shifthackz.aisdv1.data.gateway

import com.shifthackz.aisdv1.core.common.extensions.fixUrlSlashes
import com.shifthackz.aisdv1.domain.entity.AuthenticatedConnection
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Serializes connection capture with credential/configuration persistence without holding HTTP calls.
 *
 * @param preferences Current configuration fields.
 * @param authorization Protected credential owner, including pending-transaction recovery.
 * @param selectionGuard Shared configuration/publication guard.
 * @author Dmitriy Moroz
 */
internal class AuthenticatedConnectionProviderImpl(
    private val preferences: PreferenceManager,
    private val authorization: AuthorizationStore,
    private val selectionGuard: ProviderSelectionGuard,
) : AuthenticatedConnectionProvider {
    /**
     * Captures protected credentials before reading the matching URL/model snapshot under the guard.
     *
     * @param automatic1111Url Optional setup destination that must still match current configuration.
     * @param swarmUiUrl Optional setup destination that must still match current configuration.
     * @author Dmitriy Moroz
     */
    override suspend fun snapshot(
        automatic1111Url: String?,
        swarmUiUrl: String?
    ): AuthenticatedConnection =
        selectionGuard.withSelection {
            val credentials = authorization.getAuthorizationCredentials()
            val automaticUrl = preferences.automatic1111ServerUrl
            val swarmUrl = preferences.swarmUiServerUrl
            if ((automatic1111Url != null && automatic1111Url.fixUrlSlashes() != automaticUrl.fixUrlSlashes()) ||
                (swarmUiUrl != null && swarmUiUrl.fixUrlSlashes() != swarmUrl.fixUrlSlashes())
            ) {
                throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNAVAILABLE)
            }
            AuthenticatedConnection(
                automaticUrl,
                swarmUrl,
                preferences.swarmUiModel,
                credentials,
                preferences.providerSelectionRevision
            )
        }
}
