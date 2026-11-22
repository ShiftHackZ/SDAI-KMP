package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode

/**
 * Native system-browser authorization with PKCE and validated callback state.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiOAuthBrowser {
    /**
     * Shows Civitai's hosted sign-in/consent UI. Returns a verified code or throws on cancellation/failure.
     * Passwords and browser cookies never enter SDAI.
     *
     * @author Dmitriy Moroz
     */
    suspend fun authorize(): CivitaiOAuthCode
}
