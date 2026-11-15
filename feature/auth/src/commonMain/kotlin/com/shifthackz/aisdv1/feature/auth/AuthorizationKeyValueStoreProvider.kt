package com.shifthackz.aisdv1.feature.auth

/**
 * Opens protected storage on the authorization owner's serialized IO context.
 * Implementations have cheap constructors and preserve encrypted data on failure.
 *
 * @author Dmitriy Moroz
 */
internal interface AuthorizationKeyValueStoreProvider {
    /**
     * Opens storage without deleting unreadable state; failed opens remain retryable.
     *
     * @author Dmitriy Moroz
     */
    fun getAuthorizationStore(): AuthorizationKeyValueStore

    /**
     * Recreates only this credential store after explicit user confirmation, retaining shared keys.
     *
     * @author Dmitriy Moroz
     */
    fun resetAuthorizationStore(): AuthorizationKeyValueStore
}
