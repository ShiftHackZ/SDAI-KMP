package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Protects strict upscaler catalog storage, checkpoint lookup and independent cache dispatch.
 *
 * @author Dmitriy Moroz
 */
class UpscalerCatalogRepositoryTest {
    private val remote = mockk<DownloadableModelDataSource.Remote>()
    private val local = mockk<DownloadableModelDataSource.Local>()
    private val repository = DownloadableModelRepositoryImpl(remote, local)
    private val model = LocalAiModel(
        "upscaler_mnn_anime",
        LocalAiModel.Type.Upscaler,
        "MNN Anime",
        "17 MB",
        listOf("https://models.example/model.mnn"),
        downloaded = true
    )

    @Test
    fun `given upscaler catalog repository, remote fetch succeeds, expected fetched models persisted before completion`() =
        runTest {
            // Given
            coEvery { remote.fetchCatalog(LocalAiModel.Type.Upscaler) } returns listOf(model)
            coEvery { local.save(listOf(model)) } returns Unit
            // When
            repository.fetchCatalog(LocalAiModel.Type.Upscaler)
            // Then
            coVerifyOrder { remote.fetchCatalog(LocalAiModel.Type.Upscaler); local.save(listOf(model)) }
        }

    @Test
    fun `given upscaler catalog repository, fetch fails, expected original failure and no cache mutation`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { remote.fetchCatalog(LocalAiModel.Type.Upscaler) } throws failure
            // When
            val result = runCatching { repository.fetchCatalog(LocalAiModel.Type.Upscaler) }
            // Then
            assertSame(failure, result.exceptionOrNull())
            coVerify(exactly = 0) { local.save(any()) }
        }

    @Test
    fun `given upscaler catalog repository, saving fetched catalog fails, expected failure without successful cached fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("disk full")
            coEvery { remote.fetchCatalog(LocalAiModel.Type.Upscaler) } returns listOf(model)
            coEvery { local.save(any()) } throws failure
            // When
            val result = runCatching { repository.fetchCatalog(LocalAiModel.Type.Upscaler) }
            // Then
            assertSame(failure, result.exceptionOrNull())
            coVerify(exactly = 0) { local.getAllUpscalers() }
        }

    @Test
    fun `given upscaler catalog repository, refresh is cancelled, expected cancellation and no cache write`() =
        runTest {
            // Given
            val failure = CancellationException("cancelled")
            coEvery { remote.fetchCatalog(LocalAiModel.Type.Upscaler) } throws failure
            // When
            val result = runCatching { repository.fetchCatalog(LocalAiModel.Type.Upscaler) }
            // Then
            assertSame(failure, result.exceptionOrNull())
            coVerify(exactly = 0) { local.save(any()) }
        }

    @Test
    fun `given upscaler catalog repository, cached catalog is read, expected dedicated family without remote fetch`() =
        runTest {
            // Given
            coEvery { local.getAllUpscalers() } returns listOf(model)
            // When
            val result = repository.readCachedCatalog(LocalAiModel.Type.Upscaler)
            // Then
            assertEquals(listOf(model), result)
            coVerify(exactly = 0) { remote.fetchCatalog(any()) }
        }

    @Test
    fun `given upscaler catalog repository, cached lookup fails, expected original storage failure`() =
        runTest {
            // Given
            val failure = IllegalStateException("cache unavailable")
            coEvery { local.getById(model.id) } throws failure
            // When
            val result = runCatching { repository.getById(model.id) }
            // Then
            assertSame(failure, result.exceptionOrNull())
        }

    @Test
    fun `given upscaler catalog repository, downloaded model path is requested, expected exact owned path without remote access`() {
        // Given
        every { local.getModelPath(model) } returns "/owned/upscaler/model.mnn"
        // When
        val result = repository.getModelPath(model)
        // Then
        assertEquals("/owned/upscaler/model.mnn", result)
        verify(exactly = 1) { local.getModelPath(model) }
        coVerify(exactly = 0) { remote.fetchCatalog(any()) }
    }
}
