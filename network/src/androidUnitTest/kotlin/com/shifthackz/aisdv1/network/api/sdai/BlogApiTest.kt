package com.shifthackz.aisdv1.network.api.sdai

import com.shifthackz.aisdv1.network.api.sdai.model.BlogResponse
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.network.client.ResponseSizeLimitException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises the typed Blog client through Ktor's request and response pipeline.
 *
 * @author Dmitriy Moroz
 */
class BlogApiTest {

    @Test
    fun `given Blog API, published feed is fetched, expected metadata route deadline and typed posts`() =
        runBlocking {
            // Given
            var request: HttpRequestData? = null
            val client = blogClient {
                request = it
                respond(
                    FEED,
                    headers = headersOf(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                )
            }

            // When
            val response = client.use { api(it).fetchBlog() }

            // Then
            val sent = requireNotNull(request)
            assertEquals("https://metadata.example/blog.json", sent.url.toString())
            assertEquals(HttpMethod.Get, sent.method)
            assertEquals(
                8_000L,
                sent.getCapabilityOrNull(HttpTimeoutCapability)?.requestTimeoutMillis
            )
            assertEquals(
                BlogResponse(
                    listOf(
                        BlogResponse.Post(
                            slug = "1.3.0-release",
                            title = "SDAI 1.3.0",
                            summary = "Generate offline",
                            publishedOn = "2026-11-24",
                            category = "Release",
                            url = "https://sdai.moroz.cc/blog/1.3.0-release/",
                            coverUrl = "https://sdai.moroz.cc/blog/1.3.0-release/header.jpg",
                            coverDescription = "SDAI release artwork",
                            markdown = "## Café\n\nGenerate with MNN.",
                        ),
                    )
                ),
                response,
            )
        }

    @Test
    fun `given Blog API and Unicode article, response is decoded, expected UTF-8 bytes recorded as configuration traffic`() =
        runBlocking {
            // Given
            val usage = mutableListOf<Pair<NetworkUsageCategory, Long>>()
            val originalRecorder = NetworkUsageCounter.recorder
            NetworkUsageCounter.recorder = { category, bytes -> usage += category to bytes }

            // When
            try {
                blogClient { respond(FEED) }.use { api(it).fetchBlog() }
            } finally {
                NetworkUsageCounter.recorder = originalRecorder
            }

            // Then
            assertEquals(
                listOf(NetworkUsageCategory.CONFIGS to FEED.encodeToByteArray().size.toLong()),
                usage
            )
        }

    @Test
    fun `given Blog API and HTTP failure, server sends valid Blog JSON, expected status error propagates`() =
        runBlocking {
            // Given
            val status = HttpStatusCode.ServiceUnavailable
            val client = blogClient { respond(FEED, status) }

            // When
            val failure = client.use { runCatching { api(it).fetchBlog() }.exceptionOrNull() }

            // Then
            assertEquals(status, (failure as? ResponseException)?.response?.status)
        }

    @Test
    fun `given Blog API and missing article body, successful response is decoded, expected serialization failure`() =
        runBlocking {
            // Given
            val client = blogClient { respond(FEED.replace(ARTICLE_BODY, "")) }

            // When
            val failure = client.use { runCatching { api(it).fetchBlog() }.exceptionOrNull() }

            // Then
            assertTrue(failure is SerializationException)
        }

    @Test
    fun `given Blog API and oversized catalog, server declares more than the metadata limit, expected response is rejected before decoding`() =
        runBlocking {
            // Given
            val client = blogClient {
                respond(
                    FEED,
                    headers = headersOf(HttpHeaders.ContentLength, (4 * 1024 * 1024 + 1).toString())
                )
            }

            // When
            val failure = client.use { runCatching { api(it).fetchBlog() }.exceptionOrNull() }

            // Then
            assertTrue(failure is ResponseSizeLimitException)
        }

    private fun blogClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        HttpClient(MockEngine(handler)) {
            expectSuccess = true
            install(HttpTimeout)
        }

    private fun api(client: HttpClient) = KtorSdaiAppApi(
        httpClient = client,
        appBaseUrl = "https://metadata.example",
        reportBaseUrl = "https://reports.example",
    )

    private companion object {
        const val ARTICLE_BODY = """, "markdown": "## Café\n\nGenerate with MNN.""""
        val FEED = """
            {
                "posts": [{
                    "slug": "1.3.0-release", "title": "SDAI 1.3.0", "summary": "Generate offline",
                    "publishedOn": "2026-11-24", "category": "Release",
                    "url": "https://sdai.moroz.cc/blog/1.3.0-release/",
                    "coverUrl": "https://sdai.moroz.cc/blog/1.3.0-release/header.jpg",
                    "coverDescription": "SDAI release artwork"$ARTICLE_BODY
                }]
            }
        """.trimIndent()
    }
}
