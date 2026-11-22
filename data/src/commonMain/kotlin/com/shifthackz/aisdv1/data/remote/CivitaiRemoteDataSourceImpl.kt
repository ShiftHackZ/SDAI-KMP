package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.datasource.CivitaiRemoteDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthTokens
import com.shifthackz.aisdv1.network.api.civitai.CivitaiApi
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiTokenResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostRequest
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostImage
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiImageMetadata
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiExternalMetadata
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiExternalSource
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiExternalDetails
import kotlin.io.encoding.Base64

/**
 * Converts Civitai API models into domain values and sequences original-image upload before publication.
 * Transport failures propagate unchanged
 * caller policy owns recovery and retry decisions.
 *
 * @param api Fixed-origin HTTP operations with typed transport contracts.
 * @author Dmitriy Moroz
 */
internal class CivitaiRemoteDataSourceImpl(private val api: CivitaiApi) : CivitaiRemoteDataSource {
    /**
     * Resolves the identity authorized by a user key.
     *
     * @param key Civitai user API key.
     * @author Dmitriy Moroz
     */
    override suspend fun account(key: String): CivitaiAccount =
        api.account(key).let { CivitaiAccount(it.id, it.username) }

    /**
     * Exchanges a one-time authorization code with the registered public client's PKCE verifier.
     *
     * @param code Civitai authorization code.
     * @param verifier Original browser-session PKCE verifier.
     * @author Dmitriy Moroz
     */
    override suspend fun exchange(code: String, verifier: String): CivitaiOAuthTokens =
        api.exchange(code, verifier).toDomain()

    /**
     * Rotates the saved refresh token; old tokens become invalid on success.
     *
     * @param refreshToken Most recently committed refresh token.
     * @author Dmitriy Moroz
     */
    override suspend fun refresh(refreshToken: String): CivitaiOAuthTokens =
        api.refresh(refreshToken).toDomain()

    /**
     * Revokes this application's refresh token and its access tokens.
     *
     * @param refreshToken Current saved refresh token.
     * @author Dmitriy Moroz
     */
    override suspend fun revoke(refreshToken: String) = api.revoke(refreshToken)

    /**
     * Publishes the recorded output and metadata without retrying a post mutation.
     *
     * @param key Civitai user API key.
     * @param result Original saved output.
     * @param description Public app attribution and settings.
     * @param onPublishing Commits the ambiguous-publication guard before the post mutation is sent.
     * @author Dmitriy Moroz
     */
    override suspend fun publish(
        key: String,
        result: AiGenerationResult,
        description: String,
        onPublishing: suspend () -> Unit
    ): String {
        require(result.image.length <= 70_000_000) { "CIVITAI_IMAGE_TOO_LARGE" }
        val bytes = Base64.decode(result.image.substringAfter("base64,", result.image))
        require(bytes.isNotEmpty() && bytes.size <= 50 * 1024 * 1024) { "CIVITAI_IMAGE_TOO_LARGE" }
        val mime = when {
            bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() -> "image/png"
            bytes.size >= 2 && bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte() -> "image/jpeg"
            bytes.size >= 12 && bytes.decodeToString(0, 4) == "RIFF" && bytes.decodeToString(
                8,
                12
            ) == "WEBP" -> "image/webp"

            else -> error("CIVITAI_UNSUPPORTED_IMAGE")
        }
        val upload = api.presign(key)
        check(upload.id.isNotBlank()) { "CIVITAI_INVALID_UPLOAD" }
        api.upload(upload.uploadUrl, bytes, mime)
        val request = CivitaiPostRequest(
            title = "Created with SDAI", detail = description, shouldPublish = true,
            images = listOf(
                CivitaiPostImage(
                    upload.id, 0, "image", result.width, result.height,
                    CivitaiImageMetadata(
                        result.prompt, result.negativePrompt, result.samplingSteps, result.cfgScale,
                        result.sampler, result.seed.toLongOrNull(), CivitaiExternalMetadata(
                            CivitaiExternalSource("SDAI", "https://sdai.moroz.cc"),
                            CivitaiExternalDetails(
                                result.providerKey,
                                result.modelName.takeIf { it.isNotBlank() },
                                result.deviceName,
                                result.osVersion,
                                result.acceleration,
                                result.generationDurationMillis,
                                result.type.key,
                                result.restoreFaces,
                                result.subSeed,
                                result.denoisingStrength
                            ),
                        )
                    ),
                )
            ),
        )
        onPublishing()
        val post = api.createPost(key, request)
        check(post.id > 0) { "CIVITAI_INVALID_RECEIPT" }
        return "https://civitai.com/posts/${post.id}"
    }

    /**
     * Converts only the validated token fields
     * secret values are never rendered as diagnostic text.
     *
     * @author Dmitriy Moroz
     */
    private fun CivitaiTokenResponse.toDomain(): CivitaiOAuthTokens {
        check(accessToken.isNotBlank() && refreshToken?.isNotBlank() != false && expiresIn > 0)
        return CivitaiOAuthTokens(accessToken, refreshToken, expiresIn)
    }
}
