package com.shifthackz.aisdv1.network.api.civitai

import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Measures bodies produced and consumed by the real Civitai adapter, isolated from provider inference totals.
 *
 * @author Dmitriy Moroz
 */
class CivitaiNetworkUsageTest {
    private val records = mutableListOf<Pair<NetworkUsageCategory, Long>>()

    @Before
    fun setup() {
        NetworkUsageCounter.recorder = { category, bytes -> records += category to bytes }
    }

    @After
    fun cleanup() {
        NetworkUsageCounter.recorder = null
    }

    @Test
    fun `given image publication, presign PUT and post complete, expected every body counted once in Civitai only`() =
        runBlocking {
            // Given
            val presign = """{"id":"object-id","uploadURL":"https://objects.example/upload"}"""
            val reply = """{"result":{"data":{"json":{"id":123}}}}"""
            val bytes = byteArrayOf(1, 2, 3, 4)
            CivitaiApiFixture(presign).use { fixture ->
                // When
                fixture.api.presign("access")
                fixture.responseBody = ""
                fixture.api.upload("https://objects.example/upload", bytes, "image/png")
                fixture.responseBody = reply
                fixture.api.createPost("access", civitaiPostRequest)
                // Then
                val postBytes = fixture.requests.last().bodyText().encodeToByteArray().size
                val expected =
                    2L + presign.encodeToByteArray().size + bytes.size + postBytes + reply.encodeToByteArray().size
                assertEquals(expected, records.sumOf { it.second })
                assertEquals(5, records.size)
                assertTrue(records.all { it.first == NetworkUsageCategory.CIVITAI })
            }
        }

    @Test
    fun `given rejected object upload, PUT fails, expected attempted image and error response remain in Civitai totals`() =
        runBlocking {
            // Given
            CivitaiApiFixture("denied", HttpStatusCode.Forbidden).use { fixture ->
                // When
                val failure = runCatching {
                    fixture.api.upload(
                        "https://objects.example/upload",
                        byteArrayOf(1, 2, 3),
                        "image/png"
                    )
                }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_UPLOAD_FAILED", failure?.message)
                assertEquals(
                    listOf(
                        NetworkUsageCategory.CIVITAI to 3L,
                        NetworkUsageCategory.CIVITAI to 6L
                    ), records
                )
            }
        }

    @Test
    fun `given unsafe upload URL, publication is rejected locally, expected no traffic counted`() =
        runBlocking {
            // Given
            CivitaiApiFixture("").use { fixture ->
                // When
                val failure = runCatching {
                    fixture.api.upload(
                        "http://objects.example/upload",
                        byteArrayOf(1),
                        "image/png"
                    )
                }.exceptionOrNull()
                // Then
                assertTrue(failure is IllegalArgumentException)
                assertTrue(records.isEmpty())
                assertTrue(fixture.requests.isEmpty())
            }
        }

    @Test
    fun `given OAuth exchange, encoded form and reply transfer, expected Civitai byte counts without credential records`() =
        runBlocking {
            // Given
            val reply = """{"access_token":"test-access","expires_in":3600}"""
            CivitaiApiFixture(reply).use { fixture ->
                // When
                fixture.api.exchange("test-code +/=", "test-verifier")
                // Then
                assertEquals(
                    listOf(
                        NetworkUsageCategory.CIVITAI to fixture.requests.single().bodyText()
                            .encodeToByteArray().size.toLong(),
                        NetworkUsageCategory.CIVITAI to reply.encodeToByteArray().size.toLong(),
                    ), records
                )
            }
        }

    @Test
    fun `given account query, profile reply arrives, expected response traffic counted in Civitai rather than config sync`() =
        runBlocking {
            // Given
            val reply = """{"result":{"data":{"id":7,"username":"creator"}}}"""
            CivitaiApiFixture(reply).use { fixture ->
                // When
                fixture.api.account("test-access")
                // Then
                assertEquals(
                    listOf(NetworkUsageCategory.CIVITAI to reply.encodeToByteArray().size.toLong()),
                    records
                )
            }
        }

    @Test
    fun `given failed upload allocation, presign is rejected, expected request and failure bytes counted without retry`() =
        runBlocking {
            // Given
            CivitaiApiFixture("unavailable", HttpStatusCode.ServiceUnavailable).use { fixture ->
                // When
                val failure = runCatching { fixture.api.presign("access") }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_HTTP_503", failure?.message)
                assertEquals(
                    listOf(
                        NetworkUsageCategory.CIVITAI to 2L,
                        NetworkUsageCategory.CIVITAI to 11L
                    ), records
                )
                assertEquals(1, fixture.requests.size)
            }
        }
}
