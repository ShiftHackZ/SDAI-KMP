package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.data.mappers.mapRawToCheckpointDomain
import com.shifthackz.aisdv1.data.mocks.mockDownloadableModelsResponse
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class DownloadableModelRemoteDataSourceTest {

    private val stubException = Throwable("Internal server error.")
    private val stubApi = mockk<SdaiAppApi>()
    private val stubFileDownloader = mockk<DownloadableModelFileDownloader>()

    private val remoteDataSource = DownloadableModelRemoteDataSource(
        api = stubApi,
        fileDownloader = stubFileDownloader,
    )

    @Test
    fun `Onnx catalog fetch is isolated and failures propagate`() = runTest {
        coEvery { stubApi.fetchOnnxModels() } returns mockDownloadableModelsResponse
        Assert.assertEquals(
            mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.ONNX),
            remoteDataSource.fetchCatalog(LocalAiModel.Type.ONNX),
        )
        coVerify(exactly = 1) { stubApi.fetchOnnxModels() }
        confirmVerified(stubApi)
        coEvery { stubApi.fetchOnnxModels() } throws stubException
        Assert.assertSame(
            stubException,
            runCatching { remoteDataSource.fetchCatalog(LocalAiModel.Type.ONNX) }.exceptionOrNull()
        )
    }

    @Test
    fun `MediaPipe catalog fetch is isolated and failures propagate`() = runTest {
        coEvery { stubApi.fetchMediaPipeModels() } returns mockDownloadableModelsResponse
        Assert.assertEquals(
            mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.MediaPipe),
            remoteDataSource.fetchCatalog(LocalAiModel.Type.MediaPipe),
        )
        coVerify(exactly = 1) { stubApi.fetchMediaPipeModels() }
        confirmVerified(stubApi)
        coEvery { stubApi.fetchMediaPipeModels() } throws stubException
        Assert.assertSame(
            stubException,
            runCatching { remoteDataSource.fetchCatalog(LocalAiModel.Type.MediaPipe) }.exceptionOrNull()
        )
    }

    @Test
    fun `Sdxl catalog fetch is isolated and failures propagate`() = runTest {
        coEvery { stubApi.fetchSdxlModels() } returns mockDownloadableModelsResponse
        Assert.assertEquals(
            mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.Sdxl),
            remoteDataSource.fetchCatalog(LocalAiModel.Type.Sdxl),
        )
        coVerify(exactly = 1) { stubApi.fetchSdxlModels() }
        confirmVerified(stubApi)
        coEvery { stubApi.fetchSdxlModels() } throws stubException
        Assert.assertSame(
            stubException,
            runCatching { remoteDataSource.fetchCatalog(LocalAiModel.Type.Sdxl) }.exceptionOrNull()
        )
    }

    @Test
    fun `CoreMl catalog fetch is isolated and failures propagate`() = runTest {
        coEvery { stubApi.fetchCoreMlModels() } returns mockDownloadableModelsResponse
        Assert.assertEquals(
            mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.CoreMl),
            remoteDataSource.fetchCatalog(LocalAiModel.Type.CoreMl),
        )
        coVerify(exactly = 1) { stubApi.fetchCoreMlModels() }
        confirmVerified(stubApi)
        coEvery { stubApi.fetchCoreMlModels() } throws stubException
        Assert.assertSame(
            stubException,
            runCatching { remoteDataSource.fetchCatalog(LocalAiModel.Type.CoreMl) }.exceptionOrNull()
        )
    }

    @Test
    fun `Bonsai catalog fetch is isolated and failures propagate`() = runTest {
        coEvery { stubApi.fetchBonsaiModels() } returns mockDownloadableModelsResponse
        Assert.assertEquals(
            mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.Bonsai),
            remoteDataSource.fetchCatalog(LocalAiModel.Type.Bonsai),
        )
        coVerify(exactly = 1) { stubApi.fetchBonsaiModels() }
        confirmVerified(stubApi)
        coEvery { stubApi.fetchBonsaiModels() } throws stubException
        Assert.assertSame(
            stubException,
            runCatching { remoteDataSource.fetchCatalog(LocalAiModel.Type.Bonsai) }.exceptionOrNull()
        )
    }

    @Test
    fun `Mnn catalog fetch is isolated and failures propagate`() = runTest {
        coEvery { stubApi.fetchMnnModels() } returns mockDownloadableModelsResponse
        Assert.assertEquals(
            mockDownloadableModelsResponse.mapRawToCheckpointDomain(LocalAiModel.Type.Mnn),
            remoteDataSource.fetchCatalog(LocalAiModel.Type.Mnn),
        )
        coVerify(exactly = 1) { stubApi.fetchMnnModels() }
        confirmVerified(stubApi)
        coEvery { stubApi.fetchMnnModels() } throws stubException
        Assert.assertSame(
            stubException,
            runCatching { remoteDataSource.fetchCatalog(LocalAiModel.Type.Mnn) }.exceptionOrNull()
        )
    }

    @Test
    fun `given attempt to download model, expected file downloader invoked with id and url`() =
        runTest {
            every {
                stubFileDownloader.download(MODEL_ID, MODEL_URL)
            } returns flowOf(
                DownloadState.Downloading(0),
                DownloadState.Complete(FILE_PATH),
            )

            val actual = remoteDataSource.download(MODEL_ID, MODEL_URL).toList()

            Assert.assertEquals(
                listOf(
                    DownloadState.Downloading(0),
                    DownloadState.Complete(FILE_PATH),
                ),
                actual,
            )
            verify {
                stubFileDownloader.download(MODEL_ID, MODEL_URL)
            }
        }

    private companion object {
        const val MODEL_ID = "model_id"
        const val MODEL_URL = "https://example.com/model.zip"
        const val FILE_PATH = "/storage/emulated/0/model.zip"
    }
}
