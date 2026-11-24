package com.shifthackz.aisdv1.network.api.sdai

import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.network.client.ResponseSizeLimitException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Exercises both upscaler catalog endpoints through Ktor request construction and bounded decoding.
 *
 * @param isMnn Whether this run fetches the MNN catalog instead of ESRGAN.
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class UpscalerCatalogApiTest(private val isMnn: Boolean) {
    private val path get() = if (isMnn) "upscaler-mnn.json" else "upscaler-esrgan.json"
    private val feed =
        """[{"id":"upscaler_model","name":"Modèle ESRGAN","size":"17 MB","sources":["https://models.example/model.bin"]}]"""

    @Test
    fun `given upscaler catalog API, metadata is fetched, expected exact GET route and typed model`() =
        runTest {
            // Given
            val client = HttpClient(MockEngine {
                assertEquals("https://metadata.example/$path", it.url.toString())
                assertEquals(HttpMethod.Get, it.method)
                respond(feed)
            }) { expectSuccess = true }
            // When
            val models = client.use { fetch(it) }
            // Then
            assertEquals("upscaler_model", models.single().id)
            assertEquals("Modèle ESRGAN", models.single().name)
            assertEquals("17 MB", models.single().size)
            assertEquals(listOf("https://models.example/model.bin"), models.single().sources)
        }

    @Test
    fun `given upscaler catalog API, empty JSON catalog is returned, expected successful empty list`() =
        runTest {
            // Given
            val client = HttpClient(MockEngine { respond("[]") })
            // When
            val models = client.use { fetch(it) }
            // Then
            assertTrue(models.isEmpty())
        }

    @Test
    fun `given upscaler catalog API, server rejects a valid JSON body, expected original HTTP failure`() =
        runTest {
            // Given
            val client = HttpClient(MockEngine {
                respond(
                    feed,
                    HttpStatusCode.ServiceUnavailable
                )
            }) { expectSuccess = true }
            // When
            val failure = client.use { runCatching { fetch(it) }.exceptionOrNull() }
            // Then
            assertEquals(
                HttpStatusCode.ServiceUnavailable,
                (failure as? ResponseException)?.response?.status
            )
        }

    @Test
    fun `given upscaler catalog API, malformed model JSON is returned, expected decoding failure`() =
        runTest {
            // Given
            val client = HttpClient(MockEngine { respond("""[{"id":"broken","sources":42}]""") })
            // When
            val failure = client.use { runCatching { fetch(it) }.exceptionOrNull() }
            // Then
            assertTrue(failure is SerializationException)
        }

    @Test
    fun `given upscaler catalog API, response exceeds metadata byte limit, expected rejection before model decoding`() =
        runTest {
            // Given
            val client = HttpClient(MockEngine {
                respond(
                    feed,
                    headers = headersOf(HttpHeaders.ContentLength, (4 * 1024 * 1024 + 1).toString())
                )
            })
            // When
            val failure = client.use { runCatching { fetch(it) }.exceptionOrNull() }
            // Then
            assertTrue(failure is ResponseSizeLimitException)
        }

    @Test
    fun `given upscaler catalog API, transport is cancelled, expected cancellation remains a cancellation`() =
        runTest {
            // Given
            val cancelled = CancellationException("cancel catalog")
            val client = HttpClient(MockEngine { throw cancelled })
            // When
            val failure = client.use { runCatching { fetch(it) }.exceptionOrNull() }
            // Then
            assertTrue(failure is CancellationException)
            assertEquals(cancelled.message, failure?.message)
        }

    @Test
    fun `given upscaler catalog API, Unicode model catalog is decoded, expected actual UTF8 bytes counted as configuration traffic`() =
        runTest {
            // Given
            val recorded = mutableListOf<Pair<NetworkUsageCategory, Long>>()
            val previous = NetworkUsageCounter.recorder
            NetworkUsageCounter.recorder = { category, bytes -> recorded += category to bytes }
            // When
            try {
                HttpClient(MockEngine { respond(feed) }).use { fetch(it) }
            } finally {
                NetworkUsageCounter.recorder = previous
            }
            // Then
            assertEquals(
                listOf(NetworkUsageCategory.CONFIGS to feed.encodeToByteArray().size.toLong()),
                recorded
            )
        }

    /**
     * Invokes the production endpoint selected for this parameterized run.
     *
     * @param client Ktor pipeline with the controlled transport.
     * @author Dmitriy Moroz
     */
    private suspend fun fetch(client: HttpClient) = KtorSdaiAppApi(
        client, appBaseUrl = "https://metadata.example", reportBaseUrl = "https://reports.example",
    ).let { if (isMnn) it.fetchUpscalerMnnModels() else it.fetchUpscalerEsrganModels() }

    companion object {
        /**
         * Both independently hosted upscaler catalogs share this contract.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "isMnn={0}")
        fun catalogs() = listOf(arrayOf(false), arrayOf(true))
    }
}
