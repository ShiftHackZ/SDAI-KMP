package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.mocks.mockImageToImagePayload
import com.shifthackz.aisdv1.data.mocks.mockKtorSwarmUiGenerationResponse
import com.shifthackz.aisdv1.data.mocks.mockTextToImagePayload
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiGenerationApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/** Verifies workflow measurements are forwarded while result download and failures keep their normal path. @author Dmitriy Moroz */
class SwarmUiProgressDataSourceTest {
    private val api = mockk<SwarmUiGenerationApi>()
    private val source = KtorSwarmUiGenerationRemoteDataSource(api)

    @Test
    fun `given SwarmUI text-to-image, progress context is active, expected unknown reset then workflow fraction and normal image download`() =
        runTest {
            // Given
            val progress = mutableListOf<Float?>()
            coEvery { api.generateWithProgress(any(), any(), any(), any()) } coAnswers {
                arg<(Float) -> Unit>(3)(0.4f)
                mockKtorSwarmUiGenerationResponse
            }
            coEvery { api.downloadImage(any(), any()) } returns byteArrayOf(1, 2, 3)
            // When
            val result = withContext(RemoteGenerationProgressContext("task") { progress += it }) {
                source.textToImage(
                    "https://server.example",
                    "session",
                    "sd15",
                    AuthorizationCredentials.None,
                    mockTextToImagePayload
                )
            }
            // Then
            assertEquals(listOf(null, 0.4f), progress)
            assertEquals("AQID", result.image)
            coVerify(exactly = 0) { api.generate(any(), any(), any()) }
            coVerify(exactly = 1) { api.downloadImage("https://server.example/tmp/img.jpg", null) }
        }

    @Test
    fun `given SwarmUI image-to-image, progress context is active, expected unknown reset then workflow fraction and normal image download`() =
        runTest {
            // Given
            val progress = mutableListOf<Float?>()
            coEvery { api.generateWithProgress(any(), any(), any(), any()) } coAnswers {
                arg<(Float) -> Unit>(3)(0.4f)
                mockKtorSwarmUiGenerationResponse
            }
            coEvery { api.downloadImage(any(), any()) } returns byteArrayOf(1, 2, 3)
            // When
            val result = withContext(RemoteGenerationProgressContext("task") { progress += it }) {
                source.imageToImage(
                    "https://server.example",
                    "session",
                    "sd15",
                    AuthorizationCredentials.None,
                    mockImageToImagePayload
                )
            }
            // Then
            assertEquals(listOf(null, 0.4f), progress)
            assertEquals("AQID", result.image)
            coVerify(exactly = 0) { api.generate(any(), any(), any()) }
            coVerify(exactly = 1) { api.downloadImage("https://server.example/tmp/img.jpg", null) }
        }

    @Test
    fun `given SwarmUI progress generation, stream fails, expected no fallback submission or result download`() =
        runTest {
            // Given
            val failure = IllegalStateException("stream failed")
            coEvery { api.generateWithProgress(any(), any(), any(), any()) } throws failure
            // When
            val result = runCatching {
                withContext(RemoteGenerationProgressContext("task") {}) {
                    source.textToImage(
                        "https://server.example",
                        "session",
                        "sd15",
                        AuthorizationCredentials.None,
                        mockTextToImagePayload
                    )
                }
            }
            // Then
            assertSame(failure, generateSequence(result.exceptionOrNull()) { it.cause }.last())
            coVerify(exactly = 1) { api.generateWithProgress(any(), any(), any(), any()) }
            coVerify(exactly = 0) {
                api.generate(any(), any(), any())
                api.downloadImage(any(), any())
            }
        }

    @Test
    fun `given SwarmUI progress generation, final image download fails, expected failure without regeneration`() =
        runTest {
            // Given
            val failure = IllegalStateException("download failed")
            coEvery {
                api.generateWithProgress(
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns mockKtorSwarmUiGenerationResponse
            coEvery { api.downloadImage(any(), any()) } throws failure
            // When
            val result = runCatching {
                withContext(RemoteGenerationProgressContext("task") {}) {
                    source.textToImage(
                        "https://server.example",
                        "session",
                        "sd15",
                        AuthorizationCredentials.None,
                        mockTextToImagePayload
                    )
                }
            }
            // Then
            assertSame(failure, generateSequence(result.exceptionOrNull()) { it.cause }.last())
            coVerify(exactly = 1) { api.generateWithProgress(any(), any(), any(), any()) }
            coVerify(exactly = 1) { api.downloadImage(any(), any()) }
        }
}
