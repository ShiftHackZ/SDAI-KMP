package com.shifthackz.aisdv1.network.api.civitai

import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects authenticated API envelopes and credential-free object uploads without retrying mutations.
 *
 * @author Dmitriy Moroz
 */
class CivitaiPublicationTransportTest {
    private val post = civitaiPostRequest

    @Test
    fun `given authenticated account query, superjson reply arrives, expected identity and fixed-origin GET`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"result":{"data":{"json":{"id":7,"username":"creator"}}}}""").use { fixture ->
                // When
                val account = fixture.api.account("access")
                // Then
                assertEquals(7, account.id)
                assertEquals("creator", account.username)
                val request = fixture.requests.single()
                assertEquals(HttpMethod.Get, request.method)
                assertEquals("civitai.com", request.url.host)
                assertEquals("/api/trpc/user.getSelfStatus", request.url.encodedPath)
                assertEquals("Bearer access", request.headers[HttpHeaders.Authorization])
                assertEquals("{\"json\":null}", request.url.parameters["input"])
            }
        }

    @Test
    fun `given direct account envelope, profile is read, expected compatible identity decoding`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"result":{"data":{"id":7,"username":"creator"}}}""").use { fixture ->
                // When
                val account = fixture.api.account("access")
                // Then
                assertEquals("creator", account.username)
            }
        }

    @Test
    fun `given devalue account envelope, profile is read, expected referenced primitive fields decoded`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"result":{"data":"[{\"id\":1,\"username\":2,\"ignored\":99},7,\"creator\"]"}}""").use { fixture ->
                // When
                val account = fixture.api.account("access")
                // Then
                assertEquals(7, account.id)
                assertEquals("creator", account.username)
            }
        }

    @Test
    fun `given incomplete account identity, profile is read, expected strict decoding failure`() =
        runBlocking {
            // Given
            for (value in listOf("{}", "{\"id\":7}", "{\"username\":\"creator\"}")) {
                CivitaiApiFixture("""{"result":{"data":$value}}""").use { fixture ->
                    // When
                    val failure = runCatching { fixture.api.account("access") }.exceptionOrNull()
                    // Then
                    assertEquals("CIVITAI_INVALID_ACCOUNT", failure?.message)
                }
            }
        }

    @Test
    fun `given successful HTTP with procedure error, publication is requested, expected rejected mutation without retry`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"error":{"message":"private detail"}}""").use { fixture ->
                // When
                val failure =
                    runCatching { fixture.api.createPost("access", post) }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_REQUEST_FAILED", failure?.message)
                assertEquals(1, fixture.requests.size)
            }
        }

    @Test
    fun `given server failure, publication is requested, expected status failure and exactly one mutation`() =
        runBlocking {
            // Given
            CivitaiApiFixture("private detail", HttpStatusCode.ServiceUnavailable).use { fixture ->
                // When
                val failure =
                    runCatching { fixture.api.createPost("access", post) }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_HTTP_503", failure?.message)
                assertEquals(1, fixture.requests.size)
            }
        }

    @Test
    fun `given uploaded object and typed metadata, post is created, expected wrapped public request and receipt`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"result":{"data":{"json":{"id":123}}}}""").use { fixture ->
                // When
                val receipt = fixture.api.createPost("access", post)
                // Then
                assertEquals(123, receipt.id)
                val request = fixture.requests.single()
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("/api/trpc/post.createWithImages", request.url.encodedPath)
                assertEquals("Bearer access", request.headers[HttpHeaders.Authorization])
                val encoded =
                    Json.parseToJsonElement(request.bodyText()).jsonObject.getValue("json")
                assertEquals(
                    post,
                    Json.decodeFromJsonElement(CivitaiPostRequest.serializer(), encoded)
                )
                assertNull(request.url.parameters["input"])
            }
        }

    @Test
    fun `given upload allocation, presign is requested, expected authenticated API call and typed destination`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"id":"object-id","uploadURL":"https://objects.example/upload"}""").use { fixture ->
                // When
                val destination = fixture.api.presign("access")
                // Then
                val request = fixture.requests.single()
                assertEquals("https://civitai.com/api/v1/image-upload", request.url.toString())
                assertEquals("Bearer access", request.headers[HttpHeaders.Authorization])
                assertEquals("{}", request.bodyText())
                assertEquals("object-id", destination.id)
                assertEquals("https://objects.example/upload", destination.uploadUrl)
            }
        }

    @Test
    fun `given original image bytes, presigned upload is sent, expected unchanged PUT without bearer credentials`() =
        runBlocking {
            // Given
            CivitaiApiFixture("", HttpStatusCode.NoContent).use { fixture ->
                val bytes = byteArrayOf(1, 2, 3, 4)
                // When
                fixture.api.upload(
                    "https://objects.example/upload?signature=example",
                    bytes,
                    "image/png"
                )
                // Then
                val request = fixture.requests.single()
                assertEquals(HttpMethod.Put, request.method)
                assertEquals("objects.example", request.url.host)
                assertNull(request.headers[HttpHeaders.Authorization])
                assertEquals("image/png", request.body.contentType.toString())
                assertArrayEquals(bytes, (request.body as OutgoingContent.ByteArrayContent).bytes())
            }
        }

    @Test
    fun `given unsafe object destination, upload is requested, expected rejection before network access`() =
        runBlocking {
            // Given
            CivitaiApiFixture("").use { fixture ->
                for (destination in listOf(
                    "http://objects.example/file",
                    "https://user@objects.example/file",
                    "https://user:password@objects.example/file"
                )) {
                    // When
                    val failure = runCatching {
                        fixture.api.upload(
                            destination,
                            byteArrayOf(1),
                            "image/png"
                        )
                    }.exceptionOrNull()
                    // Then
                    assertTrue(failure is IllegalArgumentException)
                }
                assertTrue(fixture.requests.isEmpty())
            }
        }

    @Test
    fun `given object upload failure, PUT is rejected, expected upload failure without automatic retry`() =
        runBlocking {
            // Given
            CivitaiApiFixture("private detail", HttpStatusCode.Forbidden).use { fixture ->
                // When
                val failure = runCatching {
                    fixture.api.upload(
                        "https://objects.example/upload",
                        byteArrayOf(1),
                        "image/png"
                    )
                }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_UPLOAD_FAILED", failure?.message)
                assertEquals(1, fixture.requests.size)
            }
        }
}
