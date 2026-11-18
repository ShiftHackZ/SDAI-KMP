package com.shifthackz.aisdv1.network.api.sdai

import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks production catalog decoding, URL prefixes and accounting with an isolated HTTP boundary.
 *
 * @author Dmitriy Moroz
 */
class KtorMnnCatalogTest {
    @After
    fun releaseCounter() {
        NetworkUsageCounter.recorder = null
    }

    @Test
    fun `given MNN catalog HTTP API, valid response arrives, expected typed models and configuration accounting`() =
        runBlocking {
            // Given
            val body =
                """[{"id":"mnn_sd15","name":"SD 1.5","size":"1.1 GB","sources":["https://models.example/model.zip"]}]"""
            val traffic = mutableListOf<Pair<NetworkUsageCategory, Long>>()
            NetworkUsageCounter.recorder = { category, count -> traffic += category to count }
            val engine = MockEngine { request ->
                assertEquals("/catalog/mnn.json", request.url.encodedPath)
                respond(
                    body,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    )
                )
            }
            HttpClient(engine) { install(ContentNegotiation) { json(defaultNetworkJson) } }.use { client ->
                val api = KtorSdaiAppApi(
                    client,
                    "https://metadata.example/catalog/",
                    "https://report.example"
                )

                // When
                val models = api.fetchMnnModels()

                // Then
                assertEquals("mnn_sd15", models.single().id)
                assertEquals(listOf("https://models.example/model.zip"), models.single().sources)
                assertEquals(
                    listOf(NetworkUsageCategory.CONFIGS to body.encodeToByteArray().size.toLong()),
                    traffic
                )
            }
        }

    @Test
    fun `given MNN catalog HTTP API, server returns failure, expected exception instead of empty success`() =
        runBlocking {
            // Given
            val engine = MockEngine {
                respond(
                    "[]",
                    HttpStatusCode.ServiceUnavailable,
                    headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            HttpClient(engine) {
                expectSuccess = true
                install(ContentNegotiation) { json(defaultNetworkJson) }
            }.use { client ->
                val api =
                    KtorSdaiAppApi(client, "https://metadata.example", "https://report.example")

                // When
                val failure = runCatching { api.fetchMnnModels() }.exceptionOrNull()

                // Then
                assertTrue(failure is ServerResponseException)
                assertEquals(
                    HttpStatusCode.ServiceUnavailable,
                    (failure as ServerResponseException).response.status
                )
            }
        }

    @Test
    fun `given MNN catalog HTTP API, response is malformed, expected decoding failure`() =
        runBlocking {
            // Given
            val engine = MockEngine {
                respond(
                    "[invalid]",
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
            HttpClient(engine) { install(ContentNegotiation) { json(defaultNetworkJson) } }.use { client ->
                val api =
                    KtorSdaiAppApi(client, "https://metadata.example", "https://report.example")

                // When
                val failure = runCatching { api.fetchMnnModels() }.exceptionOrNull()

                // Then
                assertTrue(failure != null)
            }
        }
}
