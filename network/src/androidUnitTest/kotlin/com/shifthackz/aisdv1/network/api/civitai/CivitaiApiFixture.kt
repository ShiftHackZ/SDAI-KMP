package com.shifthackz.aisdv1.network.api.civitai

import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiExternalDetails
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiExternalMetadata
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiExternalSource
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiImageMetadata
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostImage
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostRequest

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf

/**
 * Records requests emitted by the production HTTP adapter while supplying controlled transport replies.
 *
 * @param responseBody Serialized server reply for the current scenario.
 * @param status HTTP status returned without automatic exception translation.
 * @author Dmitriy Moroz
 */
internal class CivitaiApiFixture(
    var responseBody: String,
    var status: HttpStatusCode = HttpStatusCode.OK,
) : AutoCloseable {
    val requests = mutableListOf<HttpRequestData>()
    var failure: Throwable? = null
    private val client = HttpClient(MockEngine { request ->
        requests += request
        failure?.let { throw it }
        respond(
            responseBody,
            status,
            headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    })
    val api = KtorCivitaiApi(client, "public-client", "cc.moroz.sdai://oauth/civitai")

    /**
     * Releases the mock engine and HTTP client after each isolated transport scenario.
     *
     * @author Dmitriy Moroz
     */
    override fun close() = client.close()
}

/**
 * Reads the actual encoded form or JSON body delivered to the HTTP engine.
 *
 * @author Dmitriy Moroz
 */
internal fun HttpRequestData.bodyText(): String =
    (body as OutgoingContent.ByteArrayContent).bytes().decodeToString()

/**
 * Typed publication model shared by transport serialization and byte-accounting scenarios.
 *
 * @author Dmitriy Moroz
 */
internal val civitaiPostRequest = CivitaiPostRequest(
    "Created with SDAI", "Attribution", true,
    listOf(
        CivitaiPostImage(
            "uploaded-object", 0, "image", 512, 512,
            CivitaiImageMetadata(
                "cat", "blur", 20, 7f, "Euler", 42,
                CivitaiExternalMetadata(
                    CivitaiExternalSource("SDAI", "https://sdai.moroz.cc"),
                    CivitaiExternalDetails(
                        type = "txt2img",
                        shouldRestoreFaces = false,
                        variationSeed = "",
                        denoisingStrength = 0f
                    )
                )
            )
        )
    )
)
