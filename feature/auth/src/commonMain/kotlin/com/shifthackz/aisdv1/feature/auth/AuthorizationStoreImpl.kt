package com.shifthackz.aisdv1.feature.auth

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationStore
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.feature.auth.transaction.PendingAuthorizationUpdate
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Serializes lazy protected-store initialization and complete credential transactions on IO.
 * Invalid records are preserved and never become anonymous access.
 *
 * @param provider Opens storage without doing work during dependency construction.
 * @param configurationStore Supplies trusted atomic configuration-commit proof for recovery.
 * @param dispatchers Supplies the IO context for disk and crypto operations.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalUuidApi::class)
internal class AuthorizationStoreImpl(
    private val provider: AuthorizationKeyValueStoreProvider,
    private val dispatchers: DispatchersProvider,
    private val configurationStore: ConfigurationStore,
) : AuthorizationStore {
    private val mutex = Mutex()
    private var store: AuthorizationKeyValueStore? = null

    /**
     * Reads a complete legacy record; unavailable or unreadable storage fails explicitly.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun getAuthorizationCredentials(): AuthorizationCredentials =
        withContext(dispatchers.io) {
            mutex.withLock {
                // Failed atomic configuration writes cannot certify even a legacy credential record.
                configurationStore.getAuthorizationCommitId()
                val current = openStore()
                try {
                    reconcilePending(current)
                    val rawKey = current.getString(KEY_CREDENTIALS_TYPE)
                    val rawValue = current.getString(KEY_CREDENTIALS_RAW)
                    if (rawKey.isNullOrEmpty() && rawValue.isNullOrEmpty()) return@withLock AuthorizationCredentials.None
                    val key = AuthorizationCredentials.Key.entries.firstOrNull { it.key == rawKey }
                        ?: throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNREADABLE)
                    if (key == AuthorizationCredentials.Key.NONE) return@withLock AuthorizationCredentials.None
                    if (rawValue.isNullOrEmpty()) throw AuthorizationStorageFailure(
                        AuthorizationStorageFailure.Reason.UNREADABLE
                    )
                    parseByKeyValueToRaw(key, rawValue).toDomain()
                } catch (error: CancellationException) {
                    throw error
                } catch (error: AuthorizationStorageFailure) {
                    throw error
                } catch (_: Exception) {
                    throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNREADABLE)
                }
            }
        }

    /**
     * Stages candidates without replacing installed credentials, commits configuration and promotes.
     * An interrupted update is resolved from its durable configuration association before any read.
     *
     * @param credentials Complete replacement record, including an explicit anonymous selection.
     * @param commitConfiguration Checked atomic configuration writer; must not reenter this store.
     * @author Dmitriy Moroz
     */
    override suspend fun storeAuthorizationCredentials(
        credentials: AuthorizationCredentials,
        commitConfiguration: suspend (String) -> Unit,
    ) = withContext(dispatchers.io) {
        mutex.withLock {
            try {
                val current = openStore()
                reconcilePending(current)
                val pending = PendingAuthorizationUpdate(
                    id = Uuid.random().toString(),
                    previousId = configurationStore.getAuthorizationCommitId(),
                    type = credentials.key.key,
                    raw = credentials.toRaw().toJson(),
                )
                current.putStrings(mapOf(KEY_PENDING to Json.encodeToString(pending)))
                commitConfiguration(pending.id)
                promote(current, pending)
            } catch (error: CancellationException) {
                throw error
            } catch (error: AuthorizationStorageFailure) {
                throw error
            } catch (_: Exception) {
                throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
            }
        }
    }

    /**
     * Resolves a durable staging record only from trusted complete configuration persistence.
     *
     * @param current Protected store, owned by this operation under the IO mutex.
     * @author Dmitriy Moroz
     */
    private fun reconcilePending(current: AuthorizationKeyValueStore) {
        val encoded = current.getString(KEY_PENDING) ?: return
        val pending = Json.decodeFromString<PendingAuthorizationUpdate>(encoded)
        if (pending.version != 1 || pending.id.isBlank()) {
            throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNREADABLE)
        }
        when (configurationStore.getAuthorizationCommitId()) {
            pending.id -> promote(current, pending)
            pending.previousId -> current.putStrings(mapOf(KEY_PENDING to null))
            else -> throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNREADABLE)
        }
    }

    /**
     * Atomically promotes a committed candidate and removes only its staging envelope.
     *
     * @param current Serialized protected store.
     * @param pending Candidate whose matching configuration is durably committed.
     * @author Dmitriy Moroz
     */
    private fun promote(current: AuthorizationKeyValueStore, pending: PendingAuthorizationUpdate) {
        current.putStrings(
            mapOf(
                KEY_CREDENTIALS_TYPE to pending.type,
                KEY_CREDENTIALS_RAW to pending.raw,
                KEY_PENDING to null,
            )
        )
    }

    /**
     * Resets only the legacy credential store after the recovery use case confirms user intent.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun resetAuthorizationCredentials() = withContext(dispatchers.io) {
        mutex.withLock {
            store = null
            try {
                store = provider.resetAuthorizationStore()
            } catch (error: CancellationException) {
                throw error
            } catch (error: AuthorizationStorageFailure) {
                throw error
            } catch (_: Exception) {
                throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
            }
        }
    }

    /**
     * Opens only on IO under the mutex; initialization failures remain retryable.
     *
     * @author Dmitriy Moroz
     */
    private fun openStore(): AuthorizationKeyValueStore = store ?: try {
        provider.getAuthorizationStore().also { store = it }
    } catch (error: CancellationException) {
        throw error
    } catch (error: AuthorizationStorageFailure) {
        throw error
    } catch (_: Exception) {
        throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.UNAVAILABLE)
    }

    private companion object {
        const val KEY_PENDING = "key_credentials_pending_update"
        const val KEY_CREDENTIALS_TYPE = "key_credentials_type"
        const val KEY_CREDENTIALS_RAW = "key_credentials_raw"
    }
}
