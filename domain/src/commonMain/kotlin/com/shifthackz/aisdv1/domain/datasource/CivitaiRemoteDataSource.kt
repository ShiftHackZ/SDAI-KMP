package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthTokens
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Remote Civitai account and image operations
 * credentials go only to the fixed API origin.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiRemoteDataSource {
    /**
     * Resolves the identity authorized by a user key.
     *
     * @param key Civitai user API key.
     * @author Dmitriy Moroz
     */
    suspend fun account(key: String): CivitaiAccount

    /**
     * Exchanges a one-time authorization code with the registered public client's PKCE verifier.
     *
     * @param code Civitai authorization code.
     * @param verifier Original browser-session PKCE verifier.
     * @author Dmitriy Moroz
     */
    suspend fun exchange(code: String, verifier: String): CivitaiOAuthTokens

    /**
     * Rotates the saved refresh token; old tokens become invalid on success.
     *
     * @param refreshToken Most recently committed refresh token.
     * @author Dmitriy Moroz
     */
    suspend fun refresh(refreshToken: String): CivitaiOAuthTokens

    /**
     * Revokes this application's refresh token and its access tokens.
     *
     * @param refreshToken Current saved refresh token.
     * @author Dmitriy Moroz
     */
    suspend fun revoke(refreshToken: String)

    /**
     * Publishes the recorded output and metadata without retrying a post mutation.
     *
     * @param key Civitai user API key.
     * @param result Original saved output.
     * @param description Public app attribution and settings.
     * @param onPublishing Commits the ambiguous-publication guard before the post mutation is sent.
     * @author Dmitriy Moroz
     */
    suspend fun publish(
        key: String,
        result: AiGenerationResult,
        description: String,
        onPublishing: suspend () -> Unit
    ): String
}
