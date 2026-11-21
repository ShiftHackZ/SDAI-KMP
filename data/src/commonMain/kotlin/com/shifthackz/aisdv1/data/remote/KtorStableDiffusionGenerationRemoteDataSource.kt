package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import kotlin.coroutines.coroutineContext

import com.shifthackz.aisdv1.data.mappers.mapStableDiffusionImageToImageResult
import com.shifthackz.aisdv1.data.mappers.mapStableDiffusionTextToImageResult
import com.shifthackz.aisdv1.data.mappers.mapToBasicHttpAuthorization
import com.shifthackz.aisdv1.data.mappers.mapToStableDiffusionRequest
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionGenerationDataSource
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111GenerationApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps domain generation payloads to authenticated AUTOMATIC1111 calls and normalizes returned images.
 *
 * @param api Provider generation and cancellation transport.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalTime::class)
class KtorStableDiffusionGenerationRemoteDataSource(
    /**
     * Provider transport used for generation, cancellation and progress reads.
     *
     * @author Dmitriy Moroz
     */
    private val api: Automatic1111GenerationApi,
) : StableDiffusionGenerationDataSource.Remote {

    /**
     * Checks whether the captured server connection can be reached; transport failures propagate.
     *
     * @param baseUrl Captured provider server root.
     * @param credentials Captured credentials for this provider connection.
     * @author Dmitriy Moroz
     */
    override suspend fun checkAvailability(
        baseUrl: String,
        credentials: AuthorizationCredentials,
    ) {
        api.healthCheck(baseUrl, credentials.mapToBasicHttpAuthorization())
    }

    /**
     * Submits one text generation request and returns the provider results.
     *
     * @param baseUrl Captured provider server root.
     * @param credentials Captured credentials for this provider connection.
     * @param payload Immutable generation inputs mapped to the provider request.
     * @author Dmitriy Moroz
     */
    override suspend fun textToImage(
        baseUrl: String,
        credentials: AuthorizationCredentials,
        payload: TextToImagePayload,
    ) = api
        .textToImage(
            baseUrl = baseUrl,
            authorization = credentials.mapToBasicHttpAuthorization(),
            request = payload.mapToStableDiffusionRequest()
                .copy(forceTaskId = coroutineContext[RemoteGenerationProgressContext]?.taskId),
        )
        .let { response -> payload to response }
        .mapStableDiffusionTextToImageResult(
            createdAtMillis = Clock.System.now().toEpochMilliseconds(),
        )

    /**
     * Submits one image transformation request and returns the provider results.
     *
     * @param baseUrl Captured provider server root.
     * @param credentials Captured credentials for this provider connection.
     * @param payload Immutable generation inputs mapped to the provider request.
     * @author Dmitriy Moroz
     */
    override suspend fun imageToImage(
        baseUrl: String,
        credentials: AuthorizationCredentials,
        payload: ImageToImagePayload,
    ) = api
        .imageToImage(
            baseUrl = baseUrl,
            authorization = credentials.mapToBasicHttpAuthorization(),
            request = payload.mapToStableDiffusionRequest()
                .copy(forceTaskId = coroutineContext[RemoteGenerationProgressContext]?.taskId),
        )
        .let { response -> payload to response }
        .mapStableDiffusionImageToImageResult(
            createdAtMillis = Clock.System.now().toEpochMilliseconds(),
        )

    /**
     * Performs the SDAI side effect handled by `interruptGeneration`.
     *
     * @param baseUrl Captured provider server root.
     * @param credentials Captured credentials for this provider connection.
     * @author Dmitriy Moroz
     */
    override suspend fun interruptGeneration(
        baseUrl: String,
        credentials: AuthorizationCredentials,
    ) {
        api.interrupt(baseUrl, credentials.mapToBasicHttpAuthorization())
    }
}
