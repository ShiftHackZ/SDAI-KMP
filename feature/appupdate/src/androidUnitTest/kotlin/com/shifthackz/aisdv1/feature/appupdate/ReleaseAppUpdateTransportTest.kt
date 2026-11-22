package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.domain.entity.AppUpdate
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CancellationException
import java.io.IOException
import kotlinx.serialization.SerializationException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies production HTTP configuration, strict failure propagation and bounded metadata reads.
 *
 * @author Dmitriy Moroz
 */
class ReleaseAppUpdateTransportTest {
    private val fixture = ReleaseGatewayFixture()

    @After
    fun closeClients() {
        fixture.close()
    }

    @Test
    fun `given release discovery request, a stable check runs, expected HTTPS GET and JSON headers`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload())

            // When
            gateway.check()

            // Then
            val request = fixture.requests.single()
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("https", request.url.protocol.name)
            assertEquals("api.github.com", request.url.host)
            assertEquals("/repos/ShiftHackZ/SDAI-KMP/releases/latest", request.url.encodedPath)
            assertEquals("application/json", request.headers["Accept"])
            assertEquals("SDAI/1.2.0", request.headers["User-Agent"])
        }

    @Test
    fun `given release discovery request, a check runs, expected bounded transport timeouts`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload())

            // When
            gateway.check()

            // Then
            val timeout =
                requireNotNull(fixture.requests.single().getCapabilityOrNull(HttpTimeoutCapability))
            assertEquals(5_000L, timeout.connectTimeoutMillis)
            assertEquals(8_000L, timeout.requestTimeoutMillis)
            assertEquals(8_000L, timeout.socketTimeoutMillis)
        }

    @Test
    fun `given release discovery transport, upstream returns Unauthorized, expected the gateway propagates HTTP failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("unavailable", status = HttpStatusCode.Unauthorized)

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is ClientRequestException)
        }

    @Test
    fun `given release discovery transport, upstream returns Forbidden, expected the gateway propagates HTTP failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("unavailable", status = HttpStatusCode.Forbidden)

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is ClientRequestException)
        }

    @Test
    fun `given release discovery transport, upstream returns NotFound, expected the gateway propagates HTTP failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("unavailable", status = HttpStatusCode.NotFound)

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is ClientRequestException)
        }

    @Test
    fun `given release discovery transport, upstream returns TooManyRequests, expected the gateway propagates HTTP failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("unavailable", status = HttpStatusCode.TooManyRequests)

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is ClientRequestException)
        }

    @Test
    fun `given release discovery transport, upstream returns InternalServerError, expected the gateway propagates HTTP failure`() =
        runBlocking {
            // Given
            val gateway =
                fixture.gateway("unavailable", status = HttpStatusCode.InternalServerError)

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is ServerResponseException)
        }

    @Test
    fun `given release metadata decoding, invalid JSON, expected a decoding failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("not JSON")

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is SerializationException)
        }

    @Test
    fun `given release metadata decoding, required fields are absent, expected a decoding failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("{}")

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is SerializationException)
        }

    @Test
    fun `given release metadata decoding, an HTML error page is returned, expected a decoding failure`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway("<html>unavailable</html>")

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is SerializationException)
        }

    @Test
    fun `given release metadata limit, the response exceeds one MiB, expected bounded reader rejection`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(" ".repeat(1_048_577))

            // When
            val failure = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            assertEquals("Update metadata exceeds the size limit", failure?.message)
        }

    @Test
    fun `given release metadata limit, a valid envelope exactly fills one MiB, expected an update`() =
        runBlocking {
            // Given
            val payload = releasePayload().padEnd(1_048_576, ' ')
            val gateway = fixture.gateway(payload)

            // When
            val result = gateway.check()

            // Then
            assertEquals("1.3.0", result?.version)
        }

    @Test
    fun `given release discovery transport, an IO failure occurs, expected the gateway preserves its cause`() =
        runBlocking {
            // Given
            val failure = IOException("Disconnected")
            val gateway = fixture.gateway("", failure = failure)

            // When
            val result = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(generateSequence(result) { it.cause }.any { it === failure })
        }

    @Test
    fun `given release discovery transport, the request is cancelled, expected cancellation reaches the caller`() =
        runBlocking {
            // Given
            val cancellation = CancellationException("Host disposed")
            val gateway = fixture.gateway("", failure = cancellation)

            // When
            val result = runCatching { gateway.check() }.exceptionOrNull()

            // Then
            assertTrue(result is CancellationException)
            assertTrue(generateSequence(result) { it.cause }.any { it === cancellation })
        }
}
