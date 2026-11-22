package com.shifthackz.aisdv1.network.api.civitai.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * OAuth token endpoint response. This object contains secrets and is never logged.
 *
 * @param accessToken Opaque scoped bearer token.
 * @param refreshToken Rotating renewal token, absent when offline access was not granted.
 * @param expiresIn Access-token lifetime in seconds.
 * @author Dmitriy Moroz
 */
@Serializable
class CivitaiTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long,
)
