package com.shifthackz.aisdv1.network.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks live setup limits and independent generation overrides in the actual Ktor pipeline.
 *
 * @author Dmitriy Moroz
 */
class ConnectionTimeoutPluginTest {
    @Test
    fun `selected metadata deadline is enforced while the server is still waiting`() = runBlocking<Unit> {
        HttpClient(MockEngine {
            delay(1_000L)
            respond("{}")
        }) {
            install(HttpTimeout) { requestTimeoutMillis = 120_000L }
            install(connectionTimeoutPlugin(ConnectionTimeoutProvider { 25L }))
        }.use { client ->
            val result = runCatching { client.get("http://localhost/models") }
            assertTrue(result.exceptionOrNull() is HttpRequestTimeoutException)
        }
    }

    @Test
    fun `metadata requests read the current limit including no limit`() = runBlocking<Unit> {
        var selectedMillis = 120_000L
        val engine = MockEngine { request ->
            val timeout = requireNotNull(request.getCapabilityOrNull(HttpTimeoutCapability))
            val expected = selectedMillis.takeIf { it > 0 } ?: HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            assertEquals(expected, timeout.connectTimeoutMillis)
            assertEquals(expected, timeout.requestTimeoutMillis)
            assertEquals(expected, timeout.socketTimeoutMillis)
            respond("{}")
        }
        HttpClient(engine) {
            install(HttpTimeout) { requestTimeoutMillis = 120_000L }
            install(connectionTimeoutPlugin(ConnectionTimeoutProvider { selectedMillis }))
        }.use { client ->
            listOf(120_000L, 300_000L, 600_000L, 1_800_000L, 0L).forEach {
                selectedMillis = it
                client.get("http://localhost/sdapi/v1/loras")
            }
        }
    }

    @Test
    fun `generation request and socket limits remain independent from setup`() = runBlocking<Unit> {
        val engine = MockEngine { request ->
            val timeout = requireNotNull(request.getCapabilityOrNull(HttpTimeoutCapability))
            assertEquals(300_000L, timeout.connectTimeoutMillis)
            assertEquals(HttpTimeoutConfig.INFINITE_TIMEOUT_MS, timeout.requestTimeoutMillis)
            assertEquals(HttpTimeoutConfig.INFINITE_TIMEOUT_MS, timeout.socketTimeoutMillis)
            respond("{}")
        }
        HttpClient(engine) {
            install(HttpTimeout)
            install(connectionTimeoutPlugin(ConnectionTimeoutProvider { 300_000L }))
        }.use { client ->
            client.get("http://localhost/images/generations") {
                applyGenerationTimeout(GenerationTimeoutProvider { 0L })
            }
        }
    }
}
