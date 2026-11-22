package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.feature.civitai.config.BuildConfig
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.CivitaiAuthorizationCancelled
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Framework entry point for AppAuth's browser lifecycle; credentials never enter Activity extras.
 *
 * @author Dmitriy Moroz
 */
class CivitaiOAuthActivity : Activity(), KoinComponent {
    private val browser: AndroidCivitaiOAuthBrowser by inject()
    private var service: AuthorizationService? = null

    /**
     * Starts AppAuth once and preserves its external-browser operation across Activity recreation.
     *
     * @param savedInstanceState Existing Activity state; nonnull means authorization is already active.
     * @author Dmitriy Moroz
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) return
        val configuration = AuthorizationServiceConfiguration(
            Uri.parse(CivitaiOAuthConfiguration.AUTHORIZE_URL),
            Uri.parse(CivitaiOAuthConfiguration.TOKEN_URL),
        )
        val request = AuthorizationRequest.Builder(
            configuration, BuildConfig.CIVITAI_CLIENT_ID,
            ResponseTypeValues.CODE, Uri.parse(BuildConfig.CIVITAI_REDIRECT_URI)
        )
            .setScope(CivitaiOAuthConfiguration.SCOPES).build()
        try {
            val authorization = AuthorizationService(this)
            service = authorization
            @Suppress("DEPRECATION")
            startActivityForResult(authorization.getAuthorizationRequestIntent(request), 310)
        } catch (_: Exception) {
            browser.complete(null, IllegalStateException("CIVITAI_AUTH_FAILED"))
            finish()
        }
    }

    /**
     * Returns AppAuth's state-checked code and verifier, or a typed cancellation without response data.
     *
     * @param requestCode Native request identity that distinguishes the Civitai authorization result.
     * @param resultCode Framework completion status when no AppAuth result is available.
     * @param data AppAuth response or error supplied by its browser lifecycle Activity.
     * @author Dmitriy Moroz
     */
    @Deprecated("AppAuth returns authorization through the Activity result contract")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 310) return
        val response = data?.let(AuthorizationResponse::fromIntent)
        val code = response?.authorizationCode
        val verifier = response?.request?.codeVerifier
        val failure = data?.let(AuthorizationException::fromIntent)
        val cancellation = AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW
        val denial = AuthorizationException.AuthorizationRequestErrors.ACCESS_DENIED
        val hasCancelled = (data == null && resultCode == RESULT_CANCELED) ||
                (failure?.type == cancellation.type && failure.code == cancellation.code) ||
                (failure?.type == denial.type && failure.code == denial.code)
        browser.complete(
            if (code != null && verifier != null) CivitaiOAuthCode(code, verifier) else null,
            if (hasCancelled) CivitaiAuthorizationCancelled() else IllegalStateException("CIVITAI_AUTH_FAILED")
        )
        finish()
    }

    /**
     * Releases the browser-service binding when this Activity leaves the native lifecycle.
     *
     * @author Dmitriy Moroz
     */
    override fun onDestroy() {
        service?.dispose()
        super.onDestroy()
    }
}
