package com.shifthackz.aisdv1.network.api.sdai

import com.shifthackz.aisdv1.network.api.sdai.model.FaqResponse
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
 * Exercises the typed FAQ client through Ktor's request and response pipeline.
 *
 * @author Dmitriy Moroz
 */
class FaqApiTest {

    @Test
    fun `given FAQ API, published feed is fetched, expected metadata route deadline and typed questions`() =
        runBlocking {
            // Given
            var request: HttpRequestData? = null
            val client = faqClient {
                request = it
                respond(
                    FEED,
                    headers = headersOf(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                )
            }

            // When
            val response = client.use { api(it).fetchFaq() }

            // Then
            val sent = requireNotNull(request)
            assertEquals("https://metadata.example/faq.json", sent.url.toString())
            assertEquals(HttpMethod.Get, sent.method)
            assertEquals(
                8_000L,
                sent.getCapabilityOrNull(HttpTimeoutCapability)?.requestTimeoutMillis
            )
            assertEquals(
                FaqResponse(
                    listOf(
                        FaqResponse.Category(
                            id = "setup",
                            label = "Setup",
                            items = listOf(
                                FaqResponse.Question(
                                    "connect",
                                    "How do I connect?",
                                    listOf("Open Settings.", "Choose Café."),
                                    listOf("server")
                                ),
                                FaqResponse.Question(
                                    "address",
                                    "Where is the URL?",
                                    listOf("Use the host address."),
                                    emptyList()
                                ),
                            ),
                        ),
                    ),
                ),
                response,
            )
        }

    @Test
    fun `given FAQ API and Unicode answer, response is decoded, expected UTF-8 bytes recorded as configuration traffic`() =
        runBlocking {
            // Given
            val usage = mutableListOf<Pair<NetworkUsageCategory, Long>>()
            val originalRecorder = NetworkUsageCounter.recorder
            NetworkUsageCounter.recorder = { category, bytes -> usage += category to bytes }

            // When
            try {
                faqClient { respond(FEED) }.use { api(it).fetchFaq() }
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
    fun `given FAQ API and HTTP failure, server sends valid FAQ JSON, expected status error propagates`() =
        runBlocking {
            // Given
            val status = HttpStatusCode.ServiceUnavailable
            val client = faqClient { respond(FEED, status) }

            // When
            val failure = client.use { runCatching { api(it).fetchFaq() }.exceptionOrNull() }

            // Then
            assertEquals(status, (failure as? ResponseException)?.response?.status)
        }

    @Test
    fun `given FAQ API and missing answer, successful response is decoded, expected serialization failure`() =
        runBlocking {
            // Given
            val client =
                faqClient { respond("""{"categories":[{"id":"setup","label":"Setup","items":[{"id":"connect","question":"How?","tags":[]}]}]}""") }

            // When
            val failure = client.use { runCatching { api(it).fetchFaq() }.exceptionOrNull() }

            // Then
            assertTrue(failure is SerializationException)
        }

    @Test
    fun `given FAQ API and oversized catalog, server declares more than the metadata limit, expected response is rejected before decoding`() =
        runBlocking {
            // Given
            val client = faqClient {
                respond(
                    FEED,
                    headers = headersOf(HttpHeaders.ContentLength, (4 * 1024 * 1024 + 1).toString())
                )
            }

            // When
            val failure = client.use { runCatching { api(it).fetchFaq() }.exceptionOrNull() }

            // Then
            assertTrue(failure is ResponseSizeLimitException)
        }

    private fun faqClient(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
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
        val FEED = """
            {
                "title": "Frequently asked questions",
                "categories": [
                    {"id":"setup","label":"Setup","items":[
                        {"id":"connect","question":"How do I connect?","answer":["Open Settings.","Choose Café."],"tags":["server"]},
                        {"id":"address","question":"Where is the URL?","answer":["Use the host address."],"tags":[]}
                    ]}
                ]
            }
        """.trimIndent()
    }
}
