package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore

/**
 * Loads editable configuration even when protected credentials require recovery.
 * A failure remains explicit in the snapshot; protected network actions still read the auth store.
 *
 * @param configurationStore Existing provider settings, independent of protected Basic Auth.
 * @param authorizationStore Lazy protected credential owner.
 * @author Dmitriy Moroz
 */
internal class DefaultGetConfigurationUseCaseImpl(
    private val configurationStore: ConfigurationStore,
    private val authorizationStore: AuthorizationStore,
) : GetConfigurationUseCase {
    /**
     * Returns existing settings with either confirmed credentials or a distinct safe storage error.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(): Configuration = try {
        configurationStore.getConfiguration(authorizationStore.getAuthorizationCredentials())
    } catch (error: AuthorizationStorageFailure) {
        configurationStore.getConfiguration(AuthorizationCredentials.None)
            .copy(authorizationStorageFailure = error.reason)
    }
}
