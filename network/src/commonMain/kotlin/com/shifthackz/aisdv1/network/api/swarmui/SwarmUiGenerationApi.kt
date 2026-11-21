package com.shifthackz.aisdv1.network.api.swarmui

import com.shifthackz.aisdv1.network.auth.BasicHttpAuthorization
import com.shifthackz.aisdv1.network.request.SwarmUiGenerationRequest
import com.shifthackz.aisdv1.network.response.KtorSwarmUiGenerationResponse

/**
 * SwarmUI generation transport using HTTP results or WebSocket workflow updates.
 *
 * @author Dmitriy Moroz
 */
interface SwarmUiGenerationApi {
    /**
     * Generates once over the server's WebSocket API, receiving workflow measurements as they arrive.
     * @param baseUrl Captured server root.
     * @param request Single-image inputs and session identity.
     * @param authorization Optional server Basic authentication.
     * @param onProgress Measured workflow fraction, independent of node-local sampling progress.
     * @return Final image paths after discard messages have been applied.
     * @author Dmitriy Moroz
     */
    suspend fun generateWithProgress(
        baseUrl: String,
        request: SwarmUiGenerationRequest,
        authorization: BasicHttpAuthorization?,
        onProgress: (Float) -> Unit,
    ): KtorSwarmUiGenerationResponse


    /**
     * Submits one SwarmUI image, then downloads its final output through the normal result path.
     *
     * @param baseUrl Captured provider server root.
     * @param request Serialized generation parameters and submission identity.
     * @param authorization Optional Basic authentication for this server.
     * @return Result produced by `generate`.
     * @author Dmitriy Moroz
     */
    suspend fun generate(
        baseUrl: String,
        request: SwarmUiGenerationRequest,
        authorization: BasicHttpAuthorization?,
    ): KtorSwarmUiGenerationResponse

    /**
     * Downloads generated image bytes using the captured authentication.
     *
     * @param url remote URL used by the operation.
     * @param authorization Optional Basic authentication for this server.
     * @return Result produced by `downloadImage`.
     * @author Dmitriy Moroz
     */
    suspend fun downloadImage(
        url: String,
        authorization: BasicHttpAuthorization?,
    ): ByteArray
}
