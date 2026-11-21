package com.shifthackz.aisdv1.network.api.automatic1111

import com.shifthackz.aisdv1.network.response.Automatic1111ProgressResponse

import com.shifthackz.aisdv1.network.auth.BasicHttpAuthorization
import com.shifthackz.aisdv1.network.request.ImageToImageRequest
import com.shifthackz.aisdv1.network.request.TextToImageRequest
import com.shifthackz.aisdv1.network.response.SdGenerationResponse


/**
 * Authenticated AUTOMATIC1111 generation, cancellation and request-correlated progress transport.
 *
 * @author Dmitriy Moroz
 */
interface Automatic1111GenerationApi {
    /**
     * Reads job progress without requesting an expensive preview image.
     *
     * @param baseUrl Captured server root.
     * @param authorization Optional server Basic authentication.
     * @return Progress plus active task identity when supported by the server.
     * @author Dmitriy Moroz
     */
    suspend fun progress(
        baseUrl: String,
        authorization: BasicHttpAuthorization?
    ): Automatic1111ProgressResponse


    /**
     * Checks server reachability using the captured authentication.
     *
     * @param baseUrl Captured provider server root.
     * @param authorization Optional Basic authentication for this server.
     * @author Dmitriy Moroz
     */
    suspend fun healthCheck(
        baseUrl: String,
        authorization: BasicHttpAuthorization?,
    )

    /**
     * Submits one text generation request and returns the provider results.
     *
     * @param baseUrl Captured provider server root.
     * @param authorization Optional Basic authentication for this server.
     * @param request Serialized generation parameters and submission identity.
     * @return Result produced by `textToImage`.
     * @author Dmitriy Moroz
     */
    suspend fun textToImage(
        baseUrl: String,
        authorization: BasicHttpAuthorization?,
        request: TextToImageRequest,
    ): SdGenerationResponse

    /**
     * Submits one image transformation request and returns the provider results.
     *
     * @param baseUrl Captured provider server root.
     * @param authorization Optional Basic authentication for this server.
     * @param request Serialized generation parameters and submission identity.
     * @return Result produced by `imageToImage`.
     * @author Dmitriy Moroz
     */
    suspend fun imageToImage(
        baseUrl: String,
        authorization: BasicHttpAuthorization?,
        request: ImageToImageRequest,
    ): SdGenerationResponse

    /**
     * Performs the SDAI side effect handled by `interrupt`.
     *
     * @param baseUrl Captured provider server root.
     * @param authorization Optional Basic authentication for this server.
     * @author Dmitriy Moroz
     */
    suspend fun interrupt(
        baseUrl: String,
        authorization: BasicHttpAuthorization?,
    )
}
