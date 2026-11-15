package com.shifthackz.aisdv1.domain.feature.auth

/**
 * Defines the `AuthorizationStore` contract for the SDAI domain layer.
 *
 * @author Dmitriy Moroz
 */
interface AuthorizationStore {
    /**
     * Awaits protected-store readiness on IO and returns committed credentials.
     * Unavailable or unreadable storage throws AuthorizationStorageFailure instead of anonymous access.
     *
     * @return Result produced by `getAuthorizationCredentials`.
     * @author Dmitriy Moroz
     */
    suspend fun getAuthorizationCredentials(): AuthorizationCredentials

    /**
     * Commits one complete credential record on IO before acknowledging replacement.
     *
     * @param credentials Complete candidate credential record.
     * @param commitConfiguration Atomically persists all configuration fields with the supplied opaque ID.
     * Must not call authorization operations recursively.
     * @author Dmitriy Moroz
     */
    suspend fun storeAuthorizationCredentials(
        credentials: AuthorizationCredentials,
        commitConfiguration: suspend (commitId: String) -> Unit,
    )

    /**
     * Removes only saved Basic Auth after an explicit user recovery confirmation.
     * Encryption keys, OAuth sessions and all unrelated application data remain intact.
     *
     * @author Dmitriy Moroz
     */
    suspend fun resetAuthorizationCredentials()
}
