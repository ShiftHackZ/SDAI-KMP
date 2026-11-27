package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.network.api.sdai.SdaiAppApi
import com.shifthackz.aisdv1.network.response.DownloadableModelResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies both remote catalogs are required and mapped into the dedicated upscaler model family.
 *
 * @author Dmitriy Moroz
 */
class UpscalerRemoteDataSourceTest {
    private val api = mockk<SdaiAppApi>()
    private val source = DownloadableModelRemoteDataSource(api, mockk())
    private val esrgan = DownloadableModelResponse(
        "upscaler_esrgan",
        "ESRGAN",
        "64 MB",
        listOf("https://models.example/model.pth")
    )
    private val mnn = esrgan.copy(id = "upscaler_mnn_anime", name = "MNN", size = "17 MB")

    @Test
    fun `given remote upscaler catalog, both providers succeed, expected ordered dedicated models without generation catalog fetch`() =
        runTest {
            // Given
            coEvery { api.fetchUpscalerEsrganModels() } returns listOf(esrgan)
            coEvery { api.fetchUpscalerMnnModels() } returns listOf(mnn)
            // When
            val result = source.fetchCatalog(LocalAiModel.Type.Upscaler)
            // Then
            assertEquals(listOf(esrgan.id, mnn.id), result.map { it.id })
            assertEquals(listOf("ESRGAN", "MNN"), result.map { it.name })
            assertEquals(listOf("64 MB", "17 MB"), result.map { it.size })
            assertEquals(listOf(esrgan.sources, mnn.sources), result.map { it.sources })
            assertEquals(
                listOf(LocalAiModel.Type.Upscaler, LocalAiModel.Type.Upscaler),
                result.map { it.type })
            coVerify(exactly = 1) { api.fetchUpscalerEsrganModels() }
            coVerify(exactly = 1) { api.fetchUpscalerMnnModels() }
            confirmVerified(api)
        }

    @Test
    fun `given remote upscaler catalog, ESRGAN request fails, expected original failure and no MNN request`() =
        runTest {
            // Given
            val failure = IllegalStateException("ESRGAN unavailable")
            coEvery { api.fetchUpscalerEsrganModels() } throws failure
            // When
            val result = runCatching { source.fetchCatalog(LocalAiModel.Type.Upscaler) }
            // Then
            assertSame(failure, result.exceptionOrNull())
            coVerify(exactly = 0) { api.fetchUpscalerMnnModels() }
        }

    @Test
    fun `given remote upscaler catalog, MNN request fails after ESRGAN succeeds, expected failure instead of partial catalog`() =
        runTest {
            // Given
            val failure = IllegalStateException("MNN unavailable")
            coEvery { api.fetchUpscalerEsrganModels() } returns listOf(esrgan)
            coEvery { api.fetchUpscalerMnnModels() } throws failure
            // When
            val result = runCatching { source.fetchCatalog(LocalAiModel.Type.Upscaler) }
            // Then
            assertSame(failure, result.exceptionOrNull())
        }

    @Test
    fun `given remote upscaler catalog, second request is cancelled, expected original cancellation`() =
        runTest {
            // Given
            val failure = CancellationException("cancel MNN")
            coEvery { api.fetchUpscalerEsrganModels() } returns listOf(esrgan)
            coEvery { api.fetchUpscalerMnnModels() } throws failure
            // When
            val result = runCatching { source.fetchCatalog(LocalAiModel.Type.Upscaler) }
            // Then
            assertSame(failure, result.exceptionOrNull())
        }
}
