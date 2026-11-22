package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.feature.backup.codec.BackupPreferencesCodec
import okio.ByteString.Companion.encodeUtf8

/**
 * Preserves absent fields and every unselected provider, including shared Basic Auth consumers.
 *
 * @param codec Deterministic portable preference encoding for opaque consent fingerprints.
 * @author Dmitriy Moroz
 */
internal class DefaultBackupProviderPlanner(private val codec: BackupPreferencesCodec) :
    BackupProviderPlanner {

    override fun plan(
        current: BackupPreferences,
        incoming: BackupPreferences,
        selection: BackupSelection,
        credentials: AuthorizationCredentials
    ): BackupProviderPlan {
        val selected = incoming.providers.filterKeys { it in selection.providers }
        val conflicts = mutableSetOf<String>()
        val fields = selected.mapValues { (provider, values) ->
            values.filterKeys { it !in BASIC_FIELDS }.also { changes ->
                val existing = current.providers[provider].orEmpty()
                if (changes.any { (key, value) -> existing.containsKey(key) && existing[key] != value }) conflicts += provider
            }
        }
        val credentialValues =
            selected.filterKeys { it in BASIC_PROVIDERS }.values.map { it.filterKeys { key -> key in BASIC_FIELDS } }
                .filter { it.isNotEmpty() }.distinct()
        require(credentialValues.size <= 1) { "Conflicting shared Basic Auth records" }
        val incomingBasic = credentialValues.firstOrNull()
        val affected = current.providers.keys.intersect(BASIC_PROVIDERS)
        var replacement: AuthorizationCredentials? = null
        var skipped = 0
        var retainedCredentialProviders = emptySet<String>()
        if (incomingBasic != null) {
            if (!selection.providers.containsAll(affected)) {
                skipped += incomingBasic.size
                retainedCredentialProviders = affected - selection.providers
            } else {
                val old = credentials as? AuthorizationCredentials.HttpBasic
                val login = (incomingBasic["basicLogin"] as? BackupValue.Text)?.value
                    ?: old?.login.orEmpty()
                val password = (incomingBasic["basicPassword"] as? BackupValue.Text)?.value
                    ?: old?.password.orEmpty()
                val candidate = AuthorizationCredentials.HttpBasic(login, password)
                if (candidate != credentials) {
                    replacement = candidate
                    if (credentials !is AuthorizationCredentials.None) conflicts += affected.ifEmpty {
                        selected.keys.intersect(
                            BASIC_PROVIDERS
                        )
                    }
                }
            }
        }
        val credentialSnapshot = when (credentials) {
            AuthorizationCredentials.None -> emptyMap()
            is AuthorizationCredentials.HttpBasic -> mapOf(
                "login" to BackupValue.Text(credentials.login),
                "password" to BackupValue.Text(credentials.password)
            )
        }
        val fingerprint = (codec.encodeProviders(
            current.providers.filterKeys { it in selection.providers }.toList()
                .sortedBy { it.first }.toMap()
                .mapValues { it.value.toList().sortedBy { it.first }.toMap() }) +
                codec.encodeProviders(
                    selected.toList().sortedBy { it.first }.toMap().mapValues {
                        it.value.toList().sortedBy { it.first }.toMap()
                    }) + codec.encode(credentialSnapshot)).encodeUtf8().sha256().hex()
        return BackupProviderPlan(
            fields,
            replacement,
            conflicts,
            fingerprint,
            skipped,
            retainedCredentialProviders
        )
    }

    private companion object {
        val BASIC_PROVIDERS = setOf("custom", "swarm_ui")
        val BASIC_FIELDS = setOf("basicLogin", "basicPassword")
    }
}
