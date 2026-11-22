package com.shifthackz.aisdv1.feature.civitai

/**
 * Registered public mobile client
 * the identifier is public and no client secret exists.
 *
 * @author Dmitriy Moroz
 */
internal object CivitaiOAuthConfiguration {
    const val SCOPES = "UserRead MediaRead MediaWrite offline_access"
    const val AUTHORIZE_URL = "https://auth.civitai.com/api/auth/oauth/authorize"
    const val TOKEN_URL = "https://auth.civitai.com/api/auth/oauth/token"
}
