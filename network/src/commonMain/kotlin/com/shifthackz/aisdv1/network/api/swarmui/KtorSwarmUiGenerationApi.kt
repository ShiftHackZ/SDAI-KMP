package com.shifthackz.aisdv1.network.api.swarmui

import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.client.recordUsageBytes
import com.shifthackz.aisdv1.network.client.utf8Size
import com.shifthackz.aisdv1.network.response.SwarmUiProgressFrame
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

import com.shifthackz.aisdv1.network.auth.BasicHttpAuthorization
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.createConfiguredHttpClient
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedByteArrayBody
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.exception.SwarmUiBadSessionException
import com.shifthackz.aisdv1.network.request.SwarmUiGenerationRequest
import com.shifthackz.aisdv1.network.response.KtorSwarmUiGenerationResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.basicAuth
import com.shifthackz.aisdv1.network.client.boundedGet
import com.shifthackz.aisdv1.network.client.boundedPost
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom

/**
 * Ktor implementation of SwarmUI generation calls with counted inference traffic.
 *
 * @param httpClient Configured Ktor client used to send provider requests.
 * @param generationTimeoutProvider Reads the current waiting limit for each image generation call.
 *
 * @author Dmitriy Moroz
 */
class KtorSwarmUiGenerationApi(
    /**
     * Configured Ktor client used for authenticated provider traffic.
     *
     * @author Dmitriy Moroz
     */
    private val httpClient: HttpClient = createConfiguredHttpClient(),
    private val generationTimeoutProvider: GenerationTimeoutProvider = GenerationTimeoutProvider.Default,
) : SwarmUiGenerationApi {

    /**
     * Receives workflow progress and final images without replaying a failed generation.
     * @param baseUrl Captured server root, retaining any reverse-proxy path.
     * @param request Generation parameters and session.
     * @param authorization Optional Basic authentication.
     * @param onProgress Finite overall workflow fraction.
     * @author Dmitriy Moroz
     */
    override suspend fun generateWithProgress(
        baseUrl: String,
        request: SwarmUiGenerationRequest,
        authorization: BasicHttpAuthorization?,
        onProgress: (Float) -> Unit,
    ): KtorSwarmUiGenerationResponse = mapSessionError {
        val limit = generationTimeoutProvider.timeoutMillis()
        try {
            withTimeout(if (limit > 0) limit.milliseconds else Duration.INFINITE) {
                val images = linkedMapOf<Int, String>()
                var hasCompleted = false
                httpClient.webSocket(request = {
                    url.takeFrom(baseUrl)
                    url.protocol =
                        if (url.protocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
                    url.appendPathSegments(PATH_API, "GenerateText2ImageWS")
                    applyAuthorization(authorization)
                }) {
                    val body = defaultNetworkJson.encodeToString(request)
                    recordUsageBytes(NetworkUsageCategory.INFERENCE, body.utf8Size())
                    send(body)
                    for (frame in incoming) {
                        recordUsageBytes(NetworkUsageCategory.INFERENCE, frame.data.size.toLong())
                        if (frame !is Frame.Text) continue
                        val update =
                            defaultNetworkJson.decodeFromString<SwarmUiProgressFrame>(frame.readText())
                        if (update.errorId == "invalid_session_id") throw SwarmUiBadSessionException()
                        check(update.error == null) { update.error.orEmpty() }
                        update.progress?.overallPercentage?.takeIf { it.isFinite() }?.let {
                            onProgress(it.coerceIn(0f, 1f))
                        }
                        val index = update.batchIndex?.toIntOrNull()
                        if (index != null && index >= 0) update.image?.let { images[index] = it }
                        update.discardIndices.forEach(images::remove)
                        if (update.socketIntention == "close") {
                            hasCompleted = true
                            break
                        }
                    }
                }
                check(hasCompleted) { "SwarmUI generation stream ended before completion" }
                check(images.isNotEmpty()) { "SwarmUI returned no images" }
                KtorSwarmUiGenerationResponse(images.values.toList())
            }
        } catch (failure: TimeoutCancellationException) {
            coroutineContext.ensureActive()
            throw HttpRequestTimeoutException(baseUrl, limit, failure)
        }
    }

    override suspend fun generate(
        baseUrl: String,
        request: SwarmUiGenerationRequest,
        authorization: BasicHttpAuthorization?,
    ): KtorSwarmUiGenerationResponse = mapSessionError {
        httpClient
            .boundedPost {
                url.takeFrom(baseUrl)
                url.appendPathSegments(PATH_API, PATH_GENERATE)
                applyGenerationTimeout(generationTimeoutProvider)
                applyAuthorization(authorization)
                setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
            }
            .trackedJsonBody(NetworkUsageCategory.INFERENCE)
    }

    override suspend fun downloadImage(
        url: String,
        authorization: BasicHttpAuthorization?,
    ): ByteArray = httpClient
        .boundedGet {
            this.url.takeFrom(url)
            applyGenerationTimeout(generationTimeoutProvider)
            trackUsage(NetworkUsageCategory.INFERENCE)
            applyAuthorization(authorization)
        }
        .trackedByteArrayBody(NetworkUsageCategory.INFERENCE)

    private fun HttpRequestBuilder.applyAuthorization(authorization: BasicHttpAuthorization?) {
        authorization?.let { credentials ->
            basicAuth(credentials.login, credentials.password)
        }
    }

    private suspend fun <T : Any> mapSessionError(block: suspend () -> T): T = try {
        block()
    } catch (t: ClientRequestException) {
        if (t.response.status == HttpStatusCode.Unauthorized) {
            throw SwarmUiBadSessionException()
        }
        throw t
    }

    private companion object {
        const val PATH_API = "API"
        const val PATH_GENERATE = "GenerateText2Image"
    }
}
