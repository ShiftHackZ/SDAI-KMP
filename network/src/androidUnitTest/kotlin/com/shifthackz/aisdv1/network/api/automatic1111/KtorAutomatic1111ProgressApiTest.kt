package com.shifthackz.aisdv1.network.api.automatic1111

import com.shifthackz.aisdv1.network.auth.BasicHttpAuthorization
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies preview-free authenticated polling through the production request builder and decoder.
 *
 * @author Dmitriy Moroz
 */
class KtorAutomatic1111ProgressApiTest {
    @Test
    fun `given AUTOMATIC1111 progress, proxy and Basic credentials are configured, expected authenticated metadata-only GET`() =
        runBlocking {
            // Given
            var requestCount = 0
            val engine = MockEngine { request ->
                requestCount++
                assertEquals(HttpMethod.Get, request.method)
                assertEquals("/proxy/sdapi/v1/progress", request.url.encodedPath)
                assertEquals("true", request.url.parameters["skip_current_image"])
                assertEquals("Basic dXNlcjpwYXNz", request.headers[HttpHeaders.Authorization])
                respond(
                    """{"progress":0.6,"current_task":"mine","eta_relative":12,"state":{"job_count":2}}""",
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            HttpClient(engine).use { client ->
                // When
                val response = KtorAutomatic1111GenerationApi(client).progress(
                    "https://server.example/proxy",
                    BasicHttpAuthorization("user", "pass")
                )
                // Then
                assertEquals(0.6f, response.progress)
                assertEquals("mine", response.currentTask)
                assertEquals(1, requestCount)
            }
        }

    @Test
    fun `given public AUTOMATIC1111 progress, authentication is absent, expected no authorization header and nullable legacy task identity`() =
        runBlocking {
            // Given
            val engine = MockEngine { request ->
                assertNull(request.headers[HttpHeaders.Authorization])
                respond(
                    """{"progress":0.4}""",
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            HttpClient(engine).use { client ->
                // When
                val response =
                    KtorAutomatic1111GenerationApi(client).progress("http://server.example", null)
                // Then
                assertEquals(0.4f, response.progress)
                assertNull(response.currentTask)
            }
        }

    @Test
    fun `given AUTOMATIC1111 progress, server returns no measurements, expected unknown values rather than fabricated completion`() =
        runBlocking {
            // Given
            val engine = MockEngine {
                respond(
                    "{}",
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            HttpClient(engine).use { client ->
                // When
                val response =
                    KtorAutomatic1111GenerationApi(client).progress("http://server.example", null)
                // Then
                assertNull(response.progress)
                assertNull(response.currentTask)
            }
        }

    @Test
    fun `given AUTOMATIC1111 progress, unauthorized response arrives, expected transport failure for domain policy`() =
        runBlocking {
            // Given
            val engine = MockEngine { respond("failure", HttpStatusCode.Unauthorized) }
            HttpClient(engine).use { client ->
                // When
                val result = runCatching {
                    KtorAutomatic1111GenerationApi(client).progress(
                        "http://server.example",
                        null
                    )
                }
                // Then
                assertTrue(result.exceptionOrNull() is ClientRequestException)
            }
        }

    @Test
    fun `given AUTOMATIC1111 progress, server failure response arrives, expected transport failure for domain policy`() =
        runBlocking {
            // Given
            val engine = MockEngine { respond("failure", HttpStatusCode.InternalServerError) }
            HttpClient(engine).use { client ->
                // When
                val result = runCatching {
                    KtorAutomatic1111GenerationApi(client).progress(
                        "http://server.example",
                        null
                    )
                }
                // Then
                assertTrue(result.exceptionOrNull() is ServerResponseException)
            }
        }

    @Test
    fun `given AUTOMATIC1111 progress, malformed response arrives, expected decoding failure`() =
        runBlocking {
            // Given
            val engine = MockEngine {
                respond(
                    "not-json",
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            HttpClient(engine).use { client ->
                // When
                val result = runCatching {
                    KtorAutomatic1111GenerationApi(client).progress(
                        "http://server.example",
                        null
                    )
                }
                // Then
                assertTrue(result.isFailure)
            }
        }
}
