package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.data.civitai.CivitaiCredentialStore
import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.domain.datasource.CivitaiUploadLocalDataSource
import com.shifthackz.aisdv1.domain.datasource.CivitaiRemoteDataSource
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Stores authorization separately from ordinary preferences and persists receipts before gallery decoration.
 * Account changes serialize with publishing so one upload cannot change owners mid-flight.
 *
 * @param credentials Protected native credential owner.
 * @param preferences Durable ordinary receipt and consent store.
 * @param remote Strict Civitai transport.
 * @param gallery Metadata-only update owner.
 * @param uploads Durable receipt guards and paginated history storage.
 * @param time Publication clock.
 * @author Dmitriy Moroz
 */
internal class CivitaiRepositoryImpl(
    private val credentials: CivitaiCredentialStore,
    private val preferences: KeyValueStore,
    private val remote: CivitaiRemoteDataSource,
    private val gallery: GenerationResultDao,
    private val uploads: CivitaiUploadLocalDataSource,
    private val time: TimeProvider,
) : CivitaiRepository {
    private val mutex = Mutex()

    /**
     * Reads the protected session record without converting invalid storage into a signed-out account.
     *
     * @author Dmitriy Moroz
     */
    private fun record(): JsonObject? =
        credentials.read()?.let { Json.parseToJsonElement(it).jsonObject }

    /**
     * Reads the protected account, returning null when signed out or an access-only session needs reauthorization.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun account(): CivitaiAccount? = mutex.withLock {
        record()?.let {
            val expiresAt = it["expiresAt"]?.jsonPrimitive?.longOrNull
            val hasRefreshToken = it["refresh"]?.jsonPrimitive?.contentOrNull != null
            if (expiresAt != null && expiresAt <= time.currentTimeMillis() + 60_000 && !hasRefreshToken) {
                return@withLock null
            }
            CivitaiAccount(
                it.getValue("id").jsonPrimitive.long,
                it.getValue("username").jsonPrimitive.content
            )
        }
    }

    /**
     * Exchanges a validated native PKCE proof and saves tokens only after account validation.
     *
     * @param proof One-time authorization code and its original verifier.
     * @author Dmitriy Moroz
     */
    override suspend fun connectOAuth(proof: CivitaiOAuthCode) = mutex.withLock {
        val tokens = remote.exchange(proof.code, proof.verifier)
        val account = remote.account(tokens.accessToken)
        preferences.putSettingsDurably(
            emptyMap(),
            mapOf("civitai_auto_upload" to false),
            emptyMap()
        )
        credentials.replace(buildJsonObject {
            put("id", account.id)
            put("username", account.username)
            put("key", tokens.accessToken)
            tokens.refreshToken?.let { put("refresh", it) }
            put(
                "expiresAt",
                time.currentTimeMillis() + tokens.expiresIn * 1000
            )
        }.toString())
    }

    /**
     * Validates a key remotely before atomically replacing the protected account.
     *
     * @param apiKey User-provided Civitai API key with UserRead and MediaWrite permissions.
     * @author Dmitriy Moroz
     */
    override suspend fun connect(apiKey: String) = mutex.withLock {
        require(apiKey.isNotBlank() && apiKey.none { it.isWhitespace() })
        val account = remote.account(apiKey)
        preferences.putSettingsDurably(
            emptyMap(),
            mapOf("civitai_auto_upload" to false),
            emptyMap()
        )
        credentials.replace(buildJsonObject {
            put("id", account.id)
            put("username", account.username)
            put("key", apiKey)
        }.toString())
    }

    /**
     * Removes local authorization and disables automatic publishing. Existing posts are retained.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun disconnect() = mutex.withLock {
        preferences.putSettingsDurably(
            emptyMap(),
            mapOf("civitai_auto_upload" to false),
            emptyMap()
        )
        record()?.get("refresh")?.jsonPrimitive?.content?.let { remote.revoke(it) }
        credentials.replace(null)
    }

    /**
     * Reads the explicit automatic publishing preference
     * absent means disabled.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun isAutoUploadEnabled(): Boolean =
        preferences.getBoolean("civitai_auto_upload")

    /**
     * Persists explicit consent to automatically publish subsequent generations.
     *
     * @param isEnabled Whether subsequent completed images may be published.
     * @author Dmitriy Moroz
     */
    override suspend fun setAutoUploadEnabled(isEnabled: Boolean) = mutex.withLock {
        if (isEnabled) checkNotNull(record())
        preferences.putSettingsDurably(
            emptyMap(),
            mapOf("civitai_auto_upload" to isEnabled),
            emptyMap()
        )
    }

    /**
     * Returns a receipt before contacting Civitai, preventing duplicates across restarts.
     *
     * @param imageId Persistent gallery identity.
     * @author Dmitriy Moroz
     */
    override suspend fun receipt(imageId: Long): CivitaiUpload? = mutex.withLock {
        val image = gallery.queryMetadata(listOf(imageId)).singleOrNull() ?: return@withLock null
        uploads.find(imageId, image.createdAt, image.imageRevision)
    }

    /**
     * Publishes an output image and returns its public URL. Does not retry ambiguous failures.
     *
     * @param result Complete recorded output, never the input image.
     * @param description Public SDAI attribution and captured generation settings.
     * @author Dmitriy Moroz
     */
    override suspend fun publish(result: AiGenerationResult, description: String): CivitaiUpload =
        mutex.withLock {
            val previous =
                uploads.find(result.id, result.createdAt, result.imageRevision)
            if (previous?.status == CivitaiUploadStatus.PUBLISHED) return@withLock previous
            check(previous?.status != CivitaiUploadStatus.UNCERTAIN) { "CIVITAI_PUBLICATION_UNCERTAIN" }
            val account = checkNotNull(record()) { "CIVITAI_SIGN_IN_REQUIRED" }
            val key = accessToken(account)
            var receipt = CivitaiUpload(
                result.id,
                account.getValue("id").jsonPrimitive.long,
                null,
                time.currentTimeMillis(),
                result.createdAt,
                result.imageRevision,
                CivitaiUploadStatus.UPLOADING
            )
            receipt = uploads.save(receipt)
            try {
                val url = remote.publish(key, result, description) {
                    receipt = receipt.copy(status = CivitaiUploadStatus.UNCERTAIN)
                    receipt = uploads.save(receipt)
                }
                // Complete local persistence even if the calling route disappears after the server succeeded.
                withContext(NonCancellable) {
                    receipt = receipt.copy(url = url, status = CivitaiUploadStatus.PUBLISHED)
                    receipt = uploads.save(receipt)
                    gallery.setCivitaiUrl(result.id, url)
                }
                receipt
            } catch (error: Exception) {
                if (receipt.status == CivitaiUploadStatus.UPLOADING) {
                    try {
                        withContext(NonCancellable) { uploads.save(receipt.copy(status = CivitaiUploadStatus.FAILED)) }
                    } catch (storageFailure: Exception) {
                        error.addSuppressed(storageFailure)
                    }
                }
                throw error
            }
        }

    /**
     * Refreshes under the account mutex and commits the rotated pair before further API use.
     *
     * @param account Complete protected session record captured by this operation.
     * @author Dmitriy Moroz
     */
    private suspend fun accessToken(account: JsonObject): String {
        val expiresAt = account["expiresAt"]?.jsonPrimitive?.longOrNull
        if (expiresAt == null || expiresAt > time.currentTimeMillis() + 60_000) return account.getValue(
            "key"
        ).jsonPrimitive.content
        val refresh = account["refresh"]?.jsonPrimitive?.contentOrNull
            ?: error("CIVITAI_SIGN_IN_REQUIRED")
        val tokens = remote.refresh(refresh)
        credentials.replace(
            JsonObject(
                (account - "refresh") + mapOf(
                    "key" to JsonPrimitive(tokens.accessToken),
                    "expiresAt" to JsonPrimitive(time.currentTimeMillis() + tokens.expiresIn * 1000),
                ) + (tokens.refreshToken?.let { mapOf("refresh" to JsonPrimitive(it)) }
                    ?: emptyMap())
            ).toString()
        )
        return tokens.accessToken
    }

}
