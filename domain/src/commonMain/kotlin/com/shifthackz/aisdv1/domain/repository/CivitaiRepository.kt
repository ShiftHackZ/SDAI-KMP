package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Strict Civitai account, publication and receipt persistence boundary. Failures propagate to callers.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiRepository {
    /**
     * Exchanges a validated native PKCE proof and saves tokens only after account validation.
     *
     * @param proof One-time authorization code and its original verifier.
     * @author Dmitriy Moroz
     */
    suspend fun connectOAuth(proof: CivitaiOAuthCode)

    /**
     * Reads the protected account, or null when signed out or an access-only session requires reauthorization.
     *
     * @author Dmitriy Moroz
     */
    suspend fun account(): CivitaiAccount?

    /**
     * Validates a key remotely before atomically replacing the protected account.
     *
     * @param apiKey User-provided Civitai API key with UserRead and MediaWrite permissions.
     * @author Dmitriy Moroz
     */
    suspend fun connect(apiKey: String)

    /**
     * Removes local authorization and disables automatic publishing. Existing posts are retained.
     *
     * @author Dmitriy Moroz
     */
    suspend fun disconnect()

    /**
     * Reads the explicit automatic publishing preference
     * absent means disabled.
     *
     * @author Dmitriy Moroz
     */
    suspend fun isAutoUploadEnabled(): Boolean

    /**
     * Persists explicit consent to automatically publish subsequent generations.
     *
     * @param isEnabled Whether subsequent completed images may be published.
     * @author Dmitriy Moroz
     */
    suspend fun setAutoUploadEnabled(isEnabled: Boolean)

    /**
     * Returns a receipt before contacting Civitai, preventing duplicates across restarts.
     *
     * @param imageId Persistent gallery identity.
     * @author Dmitriy Moroz
     */
    suspend fun receipt(imageId: Long): CivitaiUpload?

    /**
     * Publishes an output image and returns its public URL. Does not retry ambiguous failures.
     *
     * @param result Complete recorded output, never the input image.
     * @param description Public SDAI attribution and captured generation settings.
     * @author Dmitriy Moroz
     */
    suspend fun publish(result: AiGenerationResult, description: String): CivitaiUpload
}
