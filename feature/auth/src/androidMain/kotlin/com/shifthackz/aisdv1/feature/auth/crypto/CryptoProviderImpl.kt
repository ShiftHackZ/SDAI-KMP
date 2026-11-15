package com.shifthackz.aisdv1.feature.auth.crypto

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.feature.auth.AuthorizationKeyValueStore
import java.security.GeneralSecurityException

/**
 * Opens legacy encrypted preferences without touching Keystore during construction.
 * The serialized authorization owner invokes this on IO and never deletes keys on failure.
 *
 * @param context Application context for the existing protected preference file.
 * @author Dmitriy Moroz
 */
@Suppress("DEPRECATION")
internal class CryptoProviderImpl(private val context: Context) : CryptoProvider {
    /**
     * Preserves the installed filename, master-key alias and cipher schemes.
     *
     * @author Dmitriy Moroz
     */
    override fun getAuthorizationStore(): AuthorizationKeyValueStore = try {
        val preferences = EncryptedSharedPreferences.create(
            "sdai_authorization_preference",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
        SharedPreferencesAuthorizationKeyValueStore(preferences)
    } catch (_: GeneralSecurityException) {
        throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNREADABLE)
    }

    /**
     * Deletes only the confirmed credential preference file, never its shared Keystore alias.
     *
     * @author Dmitriy Moroz
     */
    override fun resetAuthorizationStore(): AuthorizationKeyValueStore {
        if (!context.deleteSharedPreferences("sdai_authorization_preference")) {
            throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
        }
        return getAuthorizationStore()
    }
}
