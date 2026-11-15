package com.shifthackz.aisdv1.feature.auth

import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import platform.Foundation.NSUserDefaults

/**
 * Replaces the complete legacy credential domain on the authorization owner's serialized IO context.
 *
 * @param name Existing suite name, retained for installed credential compatibility.
 * @author Dmitriy Moroz
 */
internal class AppleAuthorizationKeyValueStore(
    private val name: String = "sdai_authorization_preference",
) : AuthorizationKeyValueStore {
    private val defaults = NSUserDefaults(suiteName = name)
    private var confirmedValues = defaults.persistentDomainForName(name).orEmpty()
    private var hasUntrustedWrite = false

    /**
     * Reads from the confirmed suite after reconciling an earlier failed replacement.
     *
     * @param key Existing credential preference key.
     * @author Dmitriy Moroz
     */
    override fun getString(key: String): String? {
        reconcile()
        return defaults.stringForKey(key)
    }

    /**
     * Replaces both credential entries using one persistent-domain replacement.
     *
     * @param values Complete credential type and payload entries.
     * @author Dmitriy Moroz
     */
    override fun putStrings(values: Map<String, String?>) {
        reconcile()
        hasUntrustedWrite = true
        val next = (confirmedValues + values).filterValues { it != null }
        defaults.setPersistentDomain(next, name)
        if (!defaults.synchronize()) throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        confirmedValues = next
        hasUntrustedWrite = false
    }

    /**
     * Restores the last confirmed domain before further access after failed persistence.
     *
     * @author Dmitriy Moroz
     */
    private fun reconcile() {
        if (!hasUntrustedWrite) return
        defaults.setPersistentDomain(confirmedValues, name)
        if (!defaults.synchronize()) throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        hasUntrustedWrite = false
    }
}
