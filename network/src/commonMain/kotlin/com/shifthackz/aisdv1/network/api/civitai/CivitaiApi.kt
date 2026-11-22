package com.shifthackz.aisdv1.network.api.civitai

import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiAccountResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiTokenResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiUploadResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostRequest
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostResponse

/**
 * Fixed-origin Civitai HTTP operations expressed only in transport models.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiApi {
    /**
     * Resolves the bearer token's account.
     *
     * @param key User API key or OAuth access token.
     * @author Dmitriy Moroz
     */
    suspend fun account(key: String): CivitaiAccountResponse

    /**
     * Exchanges a PKCE grant.
     *
     * @param code One-time authorization code.
     * @param verifier Original PKCE verifier.
     * @author Dmitriy Moroz
     */
    suspend fun exchange(code: String, verifier: String): CivitaiTokenResponse

    /**
     * Rotates an OAuth session.
     *
     * @param refreshToken Current refresh token.
     * @author Dmitriy Moroz
     */
    suspend fun refresh(refreshToken: String): CivitaiTokenResponse

    /**
     * Revokes the application's session.
     *
     * @param refreshToken Current refresh token.
     * @author Dmitriy Moroz
     */
    suspend fun revoke(refreshToken: String)

    /**
     * Allocates an object-upload URL.
     *
     * @param key User API key or OAuth access token.
     * @author Dmitriy Moroz
     */
    suspend fun presign(key: String): CivitaiUploadResponse

    /**
     * Sends original output bytes without bearer credentials.
     *
     * @param destination Short-lived HTTPS object-upload URL returned by Civitai.
     * @param bytes Original encoded output bytes.
     * @param mime Actual encoded-image MIME type.
     * @author Dmitriy Moroz
     */
    suspend fun upload(destination: String, bytes: ByteArray, mime: String)

    /**
     * Creates and publishes one post without automatic retries.
     *
     * @param key User API key or OAuth access token.
     * @param request Ordered uploaded image and visible attribution.
     * @author Dmitriy Moroz
     */
    suspend fun createPost(key: String, request: CivitaiPostRequest): CivitaiPostResponse
}
