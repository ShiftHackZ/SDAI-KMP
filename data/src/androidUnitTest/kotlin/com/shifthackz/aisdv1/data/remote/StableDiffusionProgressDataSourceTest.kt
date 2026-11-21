package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.mappers.mapToStableDiffusionRequest
import com.shifthackz.aisdv1.data.mocks.mockImageToImagePayload
import com.shifthackz.aisdv1.data.mocks.mockSdGenerationResponse
import com.shifthackz.aisdv1.data.mocks.mockTextToImagePayload
import com.shifthackz.aisdv1.domain.entity.RemoteGenerationProgressContext
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.network.api.automatic1111.Automatic1111GenerationApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

/** Verifies the submitted generation payload carries the exact task used by progress polling. @author Dmitriy Moroz */
class StableDiffusionProgressDataSourceTest {
    private val api = mockk<Automatic1111GenerationApi>()
    private val source = KtorStableDiffusionGenerationRemoteDataSource(api)

    @Test
    fun `given text-to-image generation, progress context is active, expected matching forced task identity`() =
        runTest {
            // Given
            coEvery { api.textToImage(any(), any(), any()) } returns mockSdGenerationResponse
            // When
            withContext(RemoteGenerationProgressContext("task-owner") {}) {
                source.textToImage(
                    "https://server.example",
                    AuthorizationCredentials.None,
                    mockTextToImagePayload
                )
            }
            // Then
            coVerify(exactly = 1) {
                api.textToImage(
                    "https://server.example",
                    null,
                    mockTextToImagePayload.mapToStableDiffusionRequest()
                        .copy(forceTaskId = "task-owner")
                )
            }
        }

    @Test
    fun `given text-to-image generation, no progress context exists, expected no invented task identity`() =
        runTest {
            // Given
            coEvery { api.textToImage(any(), any(), any()) } returns mockSdGenerationResponse
            // When
            source.textToImage(
                "https://server.example",
                AuthorizationCredentials.None,
                mockTextToImagePayload
            )
            // Then
            coVerify(exactly = 1) {
                api.textToImage(
                    "https://server.example",
                    null,
                    mockTextToImagePayload.mapToStableDiffusionRequest().copy(forceTaskId = null)
                )
            }
        }

    @Test
    fun `given image-to-image generation, progress context is active, expected matching forced task identity`() =
        runTest {
            // Given
            coEvery { api.imageToImage(any(), any(), any()) } returns mockSdGenerationResponse
            // When
            withContext(RemoteGenerationProgressContext("task-owner") {}) {
                source.imageToImage(
                    "https://server.example",
                    AuthorizationCredentials.None,
                    mockImageToImagePayload
                )
            }
            // Then
            coVerify(exactly = 1) {
                api.imageToImage(
                    "https://server.example",
                    null,
                    mockImageToImagePayload.mapToStableDiffusionRequest()
                        .copy(forceTaskId = "task-owner")
                )
            }
        }

    @Test
    fun `given image-to-image generation, no progress context exists, expected no invented task identity`() =
        runTest {
            // Given
            coEvery { api.imageToImage(any(), any(), any()) } returns mockSdGenerationResponse
            // When
            source.imageToImage(
                "https://server.example",
                AuthorizationCredentials.None,
                mockImageToImagePayload
            )
            // Then
            coVerify(exactly = 1) {
                api.imageToImage(
                    "https://server.example",
                    null,
                    mockImageToImagePayload.mapToStableDiffusionRequest().copy(forceTaskId = null)
                )
            }
        }

}
