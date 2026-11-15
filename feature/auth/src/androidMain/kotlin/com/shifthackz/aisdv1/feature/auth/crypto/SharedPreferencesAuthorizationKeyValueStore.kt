package com.shifthackz.aisdv1.feature.auth.crypto

import android.content.SharedPreferences
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.feature.auth.AuthorizationKeyValueStore

/**
 * Commits encrypted credential records and quarantines failed in-memory replacements.
 * All access is serialized by the common authorization owner on IO.
 *
 * @param preferences Existing encrypted preferences initialized on IO.
 * @author Dmitriy Moroz
 */
internal class SharedPreferencesAuthorizationKeyValueStore(
    private val preferences: SharedPreferences,
) : AuthorizationKeyValueStore {

    private var confirmedValues: Map<String, String?> = mapOf(
        "key_credentials_type" to preferences.getString("key_credentials_type", null),
        "key_credentials_raw" to preferences.getString("key_credentials_raw", null),
        "key_credentials_pending_update" to preferences.getString(
            "key_credentials_pending_update",
            null
        ),
    )
    private var hasUntrustedWrite = false

    /**
     * Reconciles a failed candidate before exposing any stored credential value.
     *
     * @param key Existing preference key.
     * @author Dmitriy Moroz
     */
    override fun getString(key: String): String? {
        reconcile()
        return preferences.getString(key, null)
    }

    /**
     * A failed commit never implicitly accepts SharedPreferences' changed memory snapshot.
     *
     * @param values Complete replacement credential entries.
     * @author Dmitriy Moroz
     */
    override fun putStrings(values: Map<String, String?>) {
        reconcile()
        hasUntrustedWrite = true
        if (!commit(values)) throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        confirmedValues = confirmedValues + values
        hasUntrustedWrite = false
    }

    /**
     * Restores the last confirmed record; failed rollback keeps access blocked and retryable.
     *
     * @author Dmitriy Moroz
     */
    private fun reconcile() {
        if (!hasUntrustedWrite) return
        if (!commit(confirmedValues)) throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        hasUntrustedWrite = false
    }

    /**
     * Updates both legacy entries using one editor without changing the encryption keyspace.
     *
     * @param values Candidate/confirmed entries; null removes a previously absent entry.
     * @author Dmitriy Moroz
     */
    private fun commit(values: Map<String, String?>): Boolean = preferences.edit().apply {
        values.forEach { (key, value) -> if (value == null) remove(key) else putString(key, value) }
    }.commit()
}
