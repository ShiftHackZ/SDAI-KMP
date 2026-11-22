package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.network.api.civitai.CivitaiApi
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiAccountResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostRequest
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiPostResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiTokenResponse
import com.shifthackz.aisdv1.network.api.civitai.model.CivitaiUploadResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.io.encoding.Base64

/**
 * Protects original-image metadata mapping and the durable fence before non-idempotent publication.
 *
 * @author Dmitriy Moroz
 */
class CivitaiRemoteDataSourceTest {
    private val api = mockk<CivitaiApi>(relaxed = true)
    private val source = CivitaiRemoteDataSourceImpl(api)
    private val destination = "https://objects.example/upload"
    private val png =
        Base64.decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a6n8AAAAASUVORK5CYII=")
    private val result = AiGenerationResult(
        id = 12, image = Base64.encode(png), inputImage = "private source", createdAt = 100,
        type = AiGenerationResult.Type.IMAGE_TO_IMAGE, prompt = "cat", negativePrompt = "blur",
        width = 512, height = 768, samplingSteps = 20, cfgScale = 7f, restoreFaces = true,
        sampler = "Euler", seed = "42", subSeed = "9", subSeedStrength = 0f,
        denoisingStrength = 0.5f, hidden = false, modelName = "Recorded model",
        providerKey = "local_mnn", acceleration = "Metal", deviceName = "Original device",
        osVersion = "iOS 27", generationDurationMillis = 1234,
    )

    @Before
    fun setup() {
        coEvery { api.presign("access") } returns CivitaiUploadResponse("object-id", destination)
        coEvery { api.createPost("access", any()) } returns CivitaiPostResponse(123)
    }

    @Test
    fun `given validated account reply, account is requested, expected domain identity mapped without alteration`() =
        runTest {
            // Given
            coEvery { api.account("access") } returns CivitaiAccountResponse(7, "creator")
            // When
            val account = source.account("access")
            // Then
            assertEquals(7, account.id)
            assertEquals("creator", account.username)
        }

    @Test
    fun `given access-only token response, PKCE exchange completes, expected nullable refresh retained`() =
        runTest {
            // Given
            coEvery { api.exchange("code", "verifier") } returns CivitaiTokenResponse(
                "access",
                null,
                3600
            )
            // When
            val tokens = source.exchange("code", "verifier")
            // Then
            assertEquals("access", tokens.accessToken)
            assertNull(tokens.refreshToken)
            assertEquals(3600, tokens.expiresIn)
        }

    @Test
    fun `given renewable token response, refresh completes, expected rotated domain tokens`() =
        runTest {
            // Given
            coEvery { api.refresh("old") } returns CivitaiTokenResponse("new", "rotated", 60)
            // When
            val tokens = source.refresh("old")
            // Then
            assertEquals("new", tokens.accessToken)
            assertEquals("rotated", tokens.refreshToken)
        }

    @Test
    fun `given invalid token fields, exchange completes, expected invalid domain session rejected`() =
        runTest {
            // Given
            for (tokens in listOf(
                CivitaiTokenResponse("", null, 60),
                CivitaiTokenResponse("access", "", 60),
                CivitaiTokenResponse("access", null, 0)
            )) {
                coEvery { api.exchange(any(), any()) } returns tokens
                // When
                val failure = runCatching { source.exchange("code", "verifier") }.exceptionOrNull()
                // Then
                assertTrue(failure is IllegalStateException)
            }
        }

    @Test
    fun `given revocation transport failure, revoke is requested, expected exact failure propagated`() =
        runTest {
            // Given
            val error = IllegalStateException("unreachable")
            coEvery { api.revoke("refresh") } throws error
            // When
            val failure = runCatching { source.revoke("refresh") }.exceptionOrNull()
            // Then
            assertSame(error, failure)
        }

    @Test
    fun `given saved PNG output, publication completes, expected original bytes metadata and fence before post`() =
        runTest {
            // Given
            val request = slot<CivitaiPostRequest>()
            val bytes = slot<ByteArray>()
            val order = mutableListOf<String>()
            coEvery {
                api.upload(
                    destination,
                    capture(bytes),
                    "image/png"
                )
            } coAnswers { order += "upload" }
            coEvery { api.createPost("access", capture(request)) } coAnswers {
                order += "post"
                CivitaiPostResponse(123)
            }
            // When
            val url = source.publish("access", result, "Attribution") { order += "fence" }
            // Then
            assertEquals("https://civitai.com/posts/123", url)
            assertEquals(listOf("upload", "fence", "post"), order)
            assertArrayEquals(png, bytes.captured)
            assertEquals("Attribution", request.captured.detail)
            assertTrue(request.captured.shouldPublish)
            val image = request.captured.images.single()
            assertEquals("object-id", image.url)
            assertEquals(512, image.width)
            assertEquals(768, image.height)
            assertEquals("cat", image.meta.prompt)
            assertEquals("blur", image.meta.negativePrompt)
            assertEquals(42L, image.meta.seed)
            assertEquals(20, image.meta.steps)
            assertEquals(7f, image.meta.cfgScale)
            assertEquals("Euler", image.meta.sampler)
            assertEquals("https://sdai.moroz.cc", image.meta.external.source.homepage)
            val details = image.meta.external.details
            assertEquals("local_mnn", details.provider)
            assertEquals("Recorded model", details.model)
            assertEquals("Original device", details.device)
            assertEquals("iOS 27", details.platform)
            assertEquals("Metal", details.acceleration)
            assertEquals(1234L, details.durationMillis)
            assertEquals("img2img", details.type)
            assertTrue(details.shouldRestoreFaces)
            assertEquals("9", details.variationSeed)
            assertEquals(0.5f, details.denoisingStrength)
            coVerifyOrder {
                api.presign("access"); api.upload(
                destination,
                any(),
                "image/png"
            ); api.createPost("access", any())
            }
        }

    @Test
    fun `given supported encoded formats, publication starts, expected PNG JPEG and WebP MIME detection`() =
        runTest {
            // Given
            val cases = listOf(
                png to "image/png",
                byteArrayOf(
                    0xff.toByte(),
                    0xd8.toByte(),
                    0xff.toByte(),
                    0xd9.toByte()
                ) to "image/jpeg",
                "RIFF0000WEBP".encodeToByteArray() to "image/webp",
            )
            for ((bytes, mime) in cases) {
                // When
                source.publish(
                    "access",
                    result.copy(image = "data:$mime;base64," + Base64.encode(bytes)),
                    "description"
                ) {}
                // Then
                coVerify { api.upload(destination, match { it.contentEquals(bytes) }, mime) }
            }
        }

    @Test
    fun `given invalid image content, publication requested, expected rejection before allocation or post`() =
        runTest {
            // Given
            for (image in listOf(
                "",
                "not base64!",
                Base64.encode("unsupported".encodeToByteArray())
            )) {
                // When
                val failure = runCatching {
                    source.publish(
                        "access",
                        result.copy(image = image),
                        "description"
                    ) {}
                }.exceptionOrNull()
                // Then
                assertTrue(failure != null)
            }
            coVerify(exactly = 0) { api.presign(any()) }
            coVerify(exactly = 0) { api.createPost(any(), any()) }
        }

    @Test
    fun `given blank upload identity, presign completes, expected rejection before image PUT`() =
        runTest {
            // Given
            coEvery { api.presign("access") } returns CivitaiUploadResponse("", destination)
            // When
            val failure =
                runCatching { source.publish("access", result, "description") {} }.exceptionOrNull()
            // Then
            assertEquals("CIVITAI_INVALID_UPLOAD", failure?.message)
            coVerify(exactly = 0) { api.upload(any(), any(), any()) }
        }

    @Test
    fun `given image PUT failure, upload stops, expected exact failure with no publication fence or post`() =
        runTest {
            // Given
            val error = IllegalStateException("object unavailable")
            coEvery { api.upload(any(), any(), any()) } throws error
            var hasFenced = false
            // When
            val failure = runCatching {
                source.publish("access", result, "description") {
                    hasFenced = true
                }
            }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            assertTrue(!hasFenced)
            coVerify(exactly = 0) { api.createPost(any(), any()) }
        }

    @Test
    fun `given durable fence failure, post is about to start, expected no non-idempotent mutation`() =
        runTest {
            // Given
            val error = IllegalStateException("storage full")
            // When
            val failure = runCatching {
                source.publish(
                    "access",
                    result,
                    "description"
                ) { throw error }
            }.exceptionOrNull()
            // Then
            assertSame(error, failure)
            coVerify(exactly = 0) { api.createPost(any(), any()) }
        }

    @Test
    fun `given invalid post receipt, mutation returns, expected no fabricated public URL`() =
        runTest {
            // Given
            coEvery { api.createPost(any(), any()) } returns CivitaiPostResponse(0)
            // When
            val failure =
                runCatching { source.publish("access", result, "description") {} }.exceptionOrNull()
            // Then
            assertEquals("CIVITAI_INVALID_RECEIPT", failure?.message)
        }

    @Test
    fun `given legacy unknown seed and model, publication completes, expected absent optional metadata`() =
        runTest {
            // Given
            val request = slot<CivitaiPostRequest>()
            coEvery { api.createPost(any(), capture(request)) } returns CivitaiPostResponse(123)
            // When
            source.publish(
                "access",
                result.copy(seed = "unknown", modelName = ""),
                "description"
            ) {}
            // Then
            assertNull(request.captured.images.single().meta.seed)
            assertNull(request.captured.images.single().meta.external.details.model)
        }

    @Test
    fun `given cancelled image PUT, publication is stopped, expected cancellation propagated without post`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancel")
            coEvery { api.upload(any(), any(), any()) } throws cancellation
            // When
            val failure =
                runCatching { source.publish("access", result, "description") {} }.exceptionOrNull()
            // Then
            assertSame(cancellation, failure)
            coVerify(exactly = 0) { api.createPost(any(), any()) }
        }
}
