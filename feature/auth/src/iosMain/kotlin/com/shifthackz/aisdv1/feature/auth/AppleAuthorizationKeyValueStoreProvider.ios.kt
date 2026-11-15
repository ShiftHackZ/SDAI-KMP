package com.shifthackz.aisdv1.feature.auth

import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import platform.Foundation.NSUserDefaults

/**
 * Cheap factory opening the legacy defaults suite only on the authorization owner's IO context.
 *
 * @author Dmitriy Moroz
 */
internal class AppleAuthorizationKeyValueStoreProvider : AuthorizationKeyValueStoreProvider {
    /**
     * Opens the unchanged platform storage without touching other preference suites.
     *
     * @author Dmitriy Moroz
     */
    override fun getAuthorizationStore(): AuthorizationKeyValueStore =
        AppleAuthorizationKeyValueStore()

    /**
     * Removes only the legacy credential suite after explicit recovery confirmation.
     *
     * @author Dmitriy Moroz
     */
    override fun resetAuthorizationStore(): AuthorizationKeyValueStore {
        val defaults = NSUserDefaults(suiteName = "sdai_authorization_preference")
        defaults.removePersistentDomainForName("sdai_authorization_preference")
        if (!defaults.synchronize()) throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        return getAuthorizationStore()
    }
}
