package com.shifthackz.aisdv1.domain.entity

/**
 * Rotating protected-session values, kept separate from public account and gallery metadata.
 *
 * @param accessToken Opaque scoped bearer token.
 * @param refreshToken Rotating renewal token, or null for an access-only session that requires reauthorization on expiry.
 * @param expiresIn Access-token lifetime in seconds.
 * @author Dmitriy Moroz
 */
class CivitaiOAuthTokens(val accessToken: String, val refreshToken: String?, val expiresIn: Long)
