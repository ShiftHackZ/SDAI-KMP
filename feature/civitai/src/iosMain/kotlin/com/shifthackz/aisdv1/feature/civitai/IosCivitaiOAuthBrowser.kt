@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.feature.civitai.config.BuildConfig
import com.shifthackz.aisdv1.domain.entity.CivitaiOAuthCode
import com.shifthackz.aisdv1.domain.entity.CivitaiAuthorizationCancelled
import platform.AuthenticationServices.ASWebAuthenticationSessionErrorDomain
import com.shifthackz.aisdv1.domain.gateway.CivitaiOAuthBrowser
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.AuthenticationServices.ASWebAuthenticationSession
import okio.ByteString.Companion.encodeUtf8
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Security.SecRandomCopyBytes
import platform.Security.kSecRandomDefault
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.io.encoding.Base64

/**
 * Presents Civitai in the system authentication sheet and verifies PKCE callback state before returning.
 * Cancellation closes the native session
 * passwords remain entirely in Civitai's hosted page.
 *
 * @param presentationContext Retained native presentation-anchor provider for the authentication sheet.
 * @author Dmitriy Moroz
 */
internal class IosCivitaiOAuthBrowser(
    private val presentationContext: IosCivitaiOAuthPresentationContext,
) : CivitaiOAuthBrowser {
    private val mutex = Mutex()
    private var session: ASWebAuthenticationSession? = null

    /**
     * Presents a ten-minute PKCE session and distinguishes an intentional close from protocol failure.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun authorize(): CivitaiOAuthCode = mutex.withLock {
        withContext(Dispatchers.Main) {
            withTimeout(600_000) {
                val verifier = randomValue()
                val state = randomValue()
                val challenge = sha256(verifier)
                val url = CivitaiOAuthConfiguration.AUTHORIZE_URL + "?response_type=code" +
                        "&client_id=" + BuildConfig.CIVITAI_CLIENT_ID.encodeURLParameter() +
                        "&redirect_uri=" + BuildConfig.CIVITAI_REDIRECT_URI.encodeURLParameter() +
                        "&scope=" + CivitaiOAuthConfiguration.SCOPES.encodeURLParameter() +
                        "&state=" + state + "&code_challenge=" + challenge + "&code_challenge_method=S256"
                suspendCancellableCoroutine { continuation ->
                    val native = ASWebAuthenticationSession(
                        NSURL(string = url),
                        "cc.moroz.sdai"
                    ) { callback, failure ->
                        session = null
                        if (continuation.isActive) {
                            try {
                                if (failure?.domain == ASWebAuthenticationSessionErrorDomain && failure?.code == 1L) {
                                    throw CivitaiAuthorizationCancelled()
                                }
                                val parsed =
                                    Url(callback?.absoluteString ?: error("CIVITAI_AUTH_CANCELLED"))
                                check(parsed.protocol.name == "cc.moroz.sdai" && parsed.host == "oauth" && parsed.encodedPath == "/civitai")
                                check(parsed.parameters.getAll("state") == listOf(state))
                                if (parsed.parameters["error"] == "access_denied") throw CivitaiAuthorizationCancelled()
                                check(parsed.parameters["error"] == null)
                                val code = parsed.parameters.getAll("code")?.singleOrNull()
                                    ?.takeIf { it.isNotBlank() }
                                    ?: error("CIVITAI_AUTH_FAILED")
                                continuation.resume(CivitaiOAuthCode(code, verifier))
                            } catch (cancelled: CivitaiAuthorizationCancelled) {
                                continuation.resumeWithException(cancelled)
                            } catch (_: Exception) {
                                continuation.resumeWithException(IllegalStateException("CIVITAI_AUTH_FAILED"))
                            }
                        }
                    }
                    native.presentationContextProvider = presentationContext
                    session = native
                    continuation.invokeOnCancellation {
                        NSOperationQueue.mainQueue.addOperationWithBlock {
                            native.cancel()
                            session = null
                        }
                    }
                    if (!native.start() && continuation.isActive) {
                        session = null
                        continuation.resumeWithException(IllegalStateException("CIVITAI_AUTH_FAILED"))
                    }
                }
            }
        }
    }

    /**
     * Generates unguessable PKCE/state values using the platform CSPRNG.
     *
     * @author Dmitriy Moroz
     */
    private fun randomValue(): String {
        val bytes = ByteArray(32)
        bytes.usePinned {
            check(
                SecRandomCopyBytes(
                    kSecRandomDefault,
                    bytes.size.convert(),
                    it.addressOf(0)
                ) == 0
            )
        }
        return Base64.UrlSafe.encode(bytes).trimEnd('=')
    }

    /**
     * Produces the S256 PKCE challenge as unpadded URL-safe Base64.
     *
     * @param value Original verifier whose bytes must be hashed without normalization.
     * @author Dmitriy Moroz
     */
    private fun sha256(value: String): String {
        return value.encodeUtf8().sha256().base64Url().trimEnd('=')
    }
}
