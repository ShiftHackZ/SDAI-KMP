package com.shifthackz.aisdv1.data.civitai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Keystore-encrypted session envelope. A synchronous single-key commit replaces/deletes atomically.
 *
 * @param context Application context used only for protected app-private storage.
 * @author Dmitriy Moroz
 */
@Suppress("DEPRECATION")
internal class SecureCivitaiCredentialStore(context: Context) : CivitaiCredentialStore {
    private var hasWriteFailed = false
    private val preferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "civitai_account",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Decrypts the stored envelope with the Android Keystore-backed preferences.
     *
     * @return Complete session envelope, or null when signed out
     * decryption failures propagate.
     *
     * @author Dmitriy Moroz
     */
    override fun read(): String? {
        check(!hasWriteFailed) { "Protected account storage requires a successful write" }
        return preferences.getString("session", null)
    }

    /**
     * Commits one encrypted session entry synchronously
     * storage failures propagate to the caller.
     *
     * @param value Complete encoded envelope, or null to delete the protected entry.
     * @author Dmitriy Moroz
     */
    override fun replace(value: String?) {
        val hasCommitted = preferences.edit().apply {
            if (value == null) remove("session") else putString("session", value)
        }.commit()
        hasWriteFailed = !hasCommitted
        check(hasCommitted) { "Protected session write failed" }
    }
}
