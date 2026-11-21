package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import kotlin.coroutines.coroutineContext

import com.shifthackz.aisdv1.core.common.extensions.fixUrlSlashes
import com.shifthackz.aisdv1.data.mappers.encodeBase64NoWrap
import com.shifthackz.aisdv1.data.mappers.mapKtorImageToImageCloudResult
import com.shifthackz.aisdv1.data.mappers.mapKtorTextToImageCloudResult
import com.shifthackz.aisdv1.data.mappers.mapToBasicHttpAuthorization
import com.shifthackz.aisdv1.data.mappers.mapToKtorSwarmUiRequest
import com.shifthackz.aisdv1.domain.datasource.SwarmUiGenerationDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiGenerationApi
import com.shifthackz.aisdv1.network.request.SwarmUiGenerationRequest
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Submits captured SwarmUI requests and maps downloaded results to domain generation records.
 *
 * @param api Provider generation and image download transport.
 *
 * @throws IllegalStateException when the delegated operation cannot complete.
 * @author Dmitriy Moroz
 */
class KtorSwarmUiGenerationRemoteDataSource(
    /**
     * Provider transport used for generation, cancellation and progress reads.
     *
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private val api: SwarmUiGenerationApi,
) : SwarmUiGenerationDataSource.Remote {

    /**
     * Submits one text generation request and returns the provider results.
     *
     * @param baseUrl Captured provider server root.
     * @param sessionId session id value consumed by the API.
     * @param model model value consumed by the API.
     * @param credentials Captured credentials for this provider connection.
     * @param payload Immutable generation inputs mapped to the provider request.
     * @author Dmitriy Moroz
     */
    override suspend fun textToImage(
        baseUrl: String,
        sessionId: String,
        model: String,
        credentials: AuthorizationCredentials,
        payload: TextToImagePayload,
    ): AiGenerationResult = generate(
        baseUrl = baseUrl,
        credentials = credentials,
        payload = payload,
        request = payload.mapToKtorSwarmUiRequest(sessionId, model),
    ).let { (sourcePayload, base64) ->
        (sourcePayload to base64).mapKtorTextToImageCloudResult(currentTimeMillis())
    }

    /**
     * Submits one image transformation request and returns the provider results.
     *
     * @param baseUrl Captured provider server root.
     * @param sessionId session id value consumed by the API.
     * @param model model value consumed by the API.
     * @param credentials Captured credentials for this provider connection.
     * @param payload Immutable generation inputs mapped to the provider request.
     * @author Dmitriy Moroz
     */
    override suspend fun imageToImage(
        baseUrl: String,
        sessionId: String,
        model: String,
        credentials: AuthorizationCredentials,
        payload: ImageToImagePayload,
    ): AiGenerationResult = generate(
        baseUrl = baseUrl,
        credentials = credentials,
        payload = payload,
        request = payload.mapToKtorSwarmUiRequest(sessionId, model),
    ).let { (_, base64) ->
        (payload to base64).mapKtorImageToImageCloudResult(currentTimeMillis())
    }

    /**
     * Submits one SwarmUI image, then downloads its final output through the normal result path.
     *
     * @param baseUrl Captured provider server root.
     * @param credentials Captured credentials for this provider connection.
     * @param payload Immutable generation inputs mapped to the provider request.
     * @param request Serialized generation parameters and submission identity.
     * @return Result produced by `generate`.
     * @throws IllegalStateException when the delegated operation cannot complete.
     * @author Dmitriy Moroz
     */
    private suspend fun <T : Any> generate(
        baseUrl: String,
        credentials: AuthorizationCredentials,
        payload: T,
        request: SwarmUiGenerationRequest,
    ): Pair<T, String> {
        val authorization = credentials.mapToBasicHttpAuthorization()
        val context = coroutineContext[RemoteGenerationProgressContext]
        context?.report?.invoke(null)
        val response = if (context == null) api.generate(baseUrl, request, authorization)
        else api.generateWithProgress(baseUrl, request, authorization) { context.report(it) }
        val imageUrl = response.images
            ?.firstOrNull()
            ?.let { endpoint -> "$baseUrl/$endpoint".fixUrlSlashes() }
            ?: throw IllegalStateException("Bad response")
        val base64 = api
            .downloadImage(imageUrl, authorization)
            .encodeBase64NoWrap()
        return payload to base64
    }

    /**
     * Timestamps the completed generation using epoch milliseconds.
     *
     * @author Dmitriy Moroz
     */
    @OptIn(ExperimentalTime::class)
    private fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
