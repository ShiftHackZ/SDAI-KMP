package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import io.mockk.every
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies independent provider recovery, strict cache failures and cancellation at the shared domain boundary.
 *
 * @author Dmitriy Moroz
 */
class RefreshLocalModelCatalogUseCaseImplTest {
    private val repository = mockk<DownloadableModelRepository>()
    private val buildInfoProvider =
        mockk<BuildInfoProvider> { every { type } returns BuildType.FULL }
    private val useCase = RefreshLocalModelCatalogUseCaseImpl(repository, buildInfoProvider)

    @Test
    fun `FOSS never fetches or reads MediaPipe choices`() = runTest {
        every { buildInfoProvider.type } returns BuildType.FOSS
        assertEquals(emptyList<LocalAiModel>(), useCase(LocalAiModel.Type.MediaPipe).models)
        assertEquals(
            emptyList<LocalAiModel>(),
            GetCachedLocalModelCatalogUseCaseImpl(
                repository,
                buildInfoProvider
            )(LocalAiModel.Type.MediaPipe)
        )
        coVerify(exactly = 0) { repository.fetchCatalog(any()) }
        coVerify(exactly = 0) { repository.readCachedCatalog(any()) }
    }

    @Test
    fun `all provider failures remain visible alongside custom and downloaded cached choices`() =
        runTest {
            val failure = IllegalStateException("offline")
            LocalAiModel.Type.entries.forEach { type ->
                val cached = listOf(
                    LocalAiModel.CustomMnn.copy(
                        id = "cached-${type.key}",
                        type = type,
                        downloaded = true
                    )
                )
                coEvery { repository.fetchCatalog(type) } throws failure
                coEvery { repository.readCachedCatalog(type) } returns cached
                val result = useCase(type)
                assertSame(failure, result.failure)
                assertEquals(cached, result.models)
            }
            coEvery { repository.fetchCatalog(LocalAiModel.Type.Mnn) } returns Unit
            assertNull(useCase(LocalAiModel.Type.Mnn).failure)
        }

    @Test
    fun `fetch cancellation never reads cache and cache failures never become success`() = runTest {
        val cancelled = CancellationException("cancelled")
        coEvery { repository.fetchCatalog(LocalAiModel.Type.ONNX) } throws cancelled
        assertSame(cancelled, runCatching { useCase(LocalAiModel.Type.ONNX) }.exceptionOrNull())
        coVerify(exactly = 0) { repository.readCachedCatalog(any()) }
        val failure = IllegalStateException("storage unavailable")
        coEvery { repository.fetchCatalog(LocalAiModel.Type.ONNX) } returns Unit
        coEvery { repository.readCachedCatalog(LocalAiModel.Type.ONNX) } throws failure
        assertSame(failure, runCatching { useCase(LocalAiModel.Type.ONNX) }.exceptionOrNull())
        assertSame(
            failure,
            runCatching {
                GetCachedLocalModelCatalogUseCaseImpl(
                    repository,
                    buildInfoProvider
                )(LocalAiModel.Type.ONNX)
            }.exceptionOrNull()
        )
    }
}
