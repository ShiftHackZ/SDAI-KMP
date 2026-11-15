package com.shifthackz.aisdv1.feature.auth

/**
 * Synchronous platform storage, accessed only by the serialized authorization IO owner.
 *
 * @author Dmitriy Moroz
 */
internal interface AuthorizationKeyValueStore {
    /**
     * Reads a protected value without masking unavailable storage as missing credentials.
     *
     * @param key Existing credential preference key.
     * @author Dmitriy Moroz
     */
    fun getString(key: String): String?

    /**
     * Commits all entries as one complete record before returning. Following a failed write,
     * further reads must reconcile the last confirmed record or fail explicitly.
     *
     * @param values Entries to update atomically; null removes a staging entry.
     * @author Dmitriy Moroz
     */
    fun putStrings(values: Map<String, String?>)
}
