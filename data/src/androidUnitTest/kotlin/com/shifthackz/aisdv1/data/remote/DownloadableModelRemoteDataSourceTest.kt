package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.mappers.mapRawToCheckpointDomain
import com.shifthackz.aisdv1.data.mocks.mockDownloadableModelsResponse
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies provider-owned strict HTTP dispatch independently from download transport.
 *
 * @author Dmitriy Moroz
 */
class DownloadableModelRemoteDataSourceTest {
    private val api = mockk<SdaiAppApi>()
    private val downloader = mockk<DownloadableModelFileDownloader>()
    private val source = DownloadableModelRemoteDataSource(api, downloader)

    @Test
    fun `given ONNX catalog fetch, HTTP succeeds, expected only owning endpoint and typed catalog`() =
        runTest {
            // Given
            coEvery { api.fetchOnnxModels() } returns mockDownloadableModelsResponse

            // When
            val actual = source.fetchCatalog(LocalAiModel.Type.ONNX)

            // Then
            assertEquals(
                mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.ONNX),
                actual
            )
            coVerify(exactly = 1) { api.fetchOnnxModels() }
            confirmVerified(api)
        }

    @Test
    fun `given ONNX catalog fetch, HTTP fails, expected original failure without empty fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { api.fetchOnnxModels() } throws failure

            // When
            val actual =
                runCatching { source.fetchCatalog(LocalAiModel.Type.ONNX) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { api.fetchOnnxModels() }
            confirmVerified(api)
        }

    @Test
    fun `given MediaPipe catalog fetch, HTTP succeeds, expected only owning endpoint and typed catalog`() =
        runTest {
            // Given
            coEvery { api.fetchMediaPipeModels() } returns mockDownloadableModelsResponse

            // When
            val actual = source.fetchCatalog(LocalAiModel.Type.MediaPipe)

            // Then
            assertEquals(
                mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.MediaPipe),
                actual
            )
            coVerify(exactly = 1) { api.fetchMediaPipeModels() }
            confirmVerified(api)
        }

    @Test
    fun `given MediaPipe catalog fetch, HTTP fails, expected original failure without empty fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { api.fetchMediaPipeModels() } throws failure

            // When
            val actual =
                runCatching { source.fetchCatalog(LocalAiModel.Type.MediaPipe) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { api.fetchMediaPipeModels() }
            confirmVerified(api)
        }

    @Test
    fun `given Sdxl catalog fetch, HTTP succeeds, expected only owning endpoint and typed catalog`() =
        runTest {
            // Given
            coEvery { api.fetchSdxlModels() } returns mockDownloadableModelsResponse

            // When
            val actual = source.fetchCatalog(LocalAiModel.Type.Sdxl)

            // Then
            assertEquals(
                mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.Sdxl),
                actual
            )
            coVerify(exactly = 1) { api.fetchSdxlModels() }
            confirmVerified(api)
        }

    @Test
    fun `given Sdxl catalog fetch, HTTP fails, expected original failure without empty fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { api.fetchSdxlModels() } throws failure

            // When
            val actual =
                runCatching { source.fetchCatalog(LocalAiModel.Type.Sdxl) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { api.fetchSdxlModels() }
            confirmVerified(api)
        }

    @Test
    fun `given CoreMl catalog fetch, HTTP succeeds, expected only owning endpoint and typed catalog`() =
        runTest {
            // Given
            coEvery { api.fetchCoreMlModels() } returns mockDownloadableModelsResponse

            // When
            val actual = source.fetchCatalog(LocalAiModel.Type.CoreMl)

            // Then
            assertEquals(
                mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.CoreMl),
                actual
            )
            coVerify(exactly = 1) { api.fetchCoreMlModels() }
            confirmVerified(api)
        }

    @Test
    fun `given CoreMl catalog fetch, HTTP fails, expected original failure without empty fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { api.fetchCoreMlModels() } throws failure

            // When
            val actual =
                runCatching { source.fetchCatalog(LocalAiModel.Type.CoreMl) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { api.fetchCoreMlModels() }
            confirmVerified(api)
        }

    @Test
    fun `given Bonsai catalog fetch, HTTP succeeds, expected only owning endpoint and typed catalog`() =
        runTest {
            // Given
            coEvery { api.fetchBonsaiModels() } returns mockDownloadableModelsResponse

            // When
            val actual = source.fetchCatalog(LocalAiModel.Type.Bonsai)

            // Then
            assertEquals(
                mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.Bonsai),
                actual
            )
            coVerify(exactly = 1) { api.fetchBonsaiModels() }
            confirmVerified(api)
        }

    @Test
    fun `given Bonsai catalog fetch, HTTP fails, expected original failure without empty fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { api.fetchBonsaiModels() } throws failure

            // When
            val actual =
                runCatching { source.fetchCatalog(LocalAiModel.Type.Bonsai) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { api.fetchBonsaiModels() }
            confirmVerified(api)
        }

    @Test
    fun `given Mnn catalog fetch, HTTP succeeds, expected only owning endpoint and typed catalog`() =
        runTest {
            // Given
            coEvery { api.fetchMnnModels() } returns mockDownloadableModelsResponse

            // When
            val actual = source.fetchCatalog(LocalAiModel.Type.Mnn)

            // Then
            assertEquals(
                mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.Mnn),
                actual
            )
            coVerify(exactly = 1) { api.fetchMnnModels() }
            confirmVerified(api)
        }

    @Test
    fun `given Mnn catalog fetch, HTTP fails, expected original failure without empty fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { api.fetchMnnModels() } throws failure

            // When
            val actual =
                runCatching { source.fetchCatalog(LocalAiModel.Type.Mnn) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 1) { api.fetchMnnModels() }
            confirmVerified(api)
        }

    @Test
    fun `given model download transport, ID and mirror are submitted, expected unchanged downloader states`() =
        runTest {
            // Given
            val states =
                listOf(DownloadState.Downloading(0), DownloadState.Complete("/owned/model"))
            every {
                downloader.download(
                    "model_id",
                    "https://example.com/model.zip"
                )
            } returns flowOf(*states.toTypedArray())

            // When
            val actual = source.download("model_id", "https://example.com/model.zip").toList()

            // Then
            assertEquals(states, actual)
            verify(exactly = 1) { downloader.download("model_id", "https://example.com/model.zip") }
        }
}
