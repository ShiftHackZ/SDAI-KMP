package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Applies the strict refresh and cache contract independently to every local catalog family.
 *
 * @param type Runtime family supplied by the parameterized fixture.
 *
 * @author Dmitriy Moroz
 */
@RunWith(Parameterized::class)
class RefreshLocalModelCatalogUseCaseImplTest(private val type: LocalAiModel.Type) {
    private val repository = mockk<DownloadableModelRepository>()
    private val buildInfo =
        mockk<BuildInfoProvider> { every { this@mockk.type } returns BuildType.FULL }
    private val cached =
        listOf(LocalAiModel("catalog", type, "Model", "1 GB", emptyList(), downloaded = true))
    private val useCase = RefreshLocalModelCatalogUseCaseImpl(repository, buildInfo)

    @Test
    fun `given catalog refresh, fetch succeeds, expected cache read after strict fetch`() =
        runTest {
            // Given
            coEvery { repository.fetchCatalog(type) } returns Unit
            coEvery { repository.readCachedCatalog(type) } returns cached

            // When
            val result = useCase(type)

            // Then
            assertEquals(cached, result.models)
            assertNull(result.failure)
            coVerifyOrder { repository.fetchCatalog(type); repository.readCachedCatalog(type) }
        }

    @Test
    fun `given catalog refresh, network fails, expected cached choices with explicit failure`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            coEvery { repository.fetchCatalog(type) } throws failure
            coEvery { repository.readCachedCatalog(type) } returns cached

            // When
            val result = useCase(type)

            // Then
            assertSame(failure, result.failure)
            assertEquals(cached, result.models)
            coVerify(exactly = 1) { repository.readCachedCatalog(type) }
        }

    @Test
    fun `given catalog refresh, fetch is cancelled, expected cancellation without cached fallback`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            coEvery { repository.fetchCatalog(type) } throws cancellation

            // When
            val failure = runCatching { useCase(type) }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            coVerify(exactly = 0) { repository.readCachedCatalog(any()) }
        }

    @Test
    fun `given catalog refresh, cache fails after successful fetch, expected storage error propagated`() =
        runTest {
            // Given
            val failure = IllegalStateException("storage unavailable")
            coEvery { repository.fetchCatalog(type) } returns Unit
            coEvery { repository.readCachedCatalog(type) } throws failure

            // When
            val actual = runCatching { useCase(type) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
        }

    @Test
    fun `given cached catalog read, choices exist, expected result without network access`() =
        runTest {
            // Given
            coEvery { repository.readCachedCatalog(type) } returns cached
            val read = GetCachedLocalModelCatalogUseCaseImpl(repository, buildInfo)

            // When
            val actual = read(type)

            // Then
            assertEquals(cached, actual)
            coVerify(exactly = 0) { repository.fetchCatalog(any()) }
        }

    @Test
    fun `given cached catalog read, storage fails, expected original error without recovery`() =
        runTest {
            // Given
            val failure = IllegalStateException("storage unavailable")
            coEvery { repository.readCachedCatalog(type) } throws failure
            val read = GetCachedLocalModelCatalogUseCaseImpl(repository, buildInfo)

            // When
            val actual = runCatching { read(type) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            coVerify(exactly = 0) { repository.fetchCatalog(any()) }
        }

    @Test
    fun `given MediaPipe catalog policy, build is FOSS, expected no network or cached access`() =
        runTest {
            // Given
            every { buildInfo.type } returns BuildType.FOSS
            val read = GetCachedLocalModelCatalogUseCaseImpl(repository, buildInfo)

            // When
            val refreshed = useCase(LocalAiModel.Type.MediaPipe)
            val result = read(LocalAiModel.Type.MediaPipe)

            // Then
            assertEquals(emptyList<LocalAiModel>(), refreshed.models)
            assertEquals(emptyList<LocalAiModel>(), result)
            assertNull(refreshed.failure)
            coVerify(exactly = 0) { repository.fetchCatalog(any()); repository.readCachedCatalog(any()) }
        }

    companion object {
        /**
         * Runs each shared contract against every persisted local model type.
         *
         * @author Dmitriy Moroz
         */
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun catalogs(): List<Array<LocalAiModel.Type>> =
            LocalAiModel.Type.entries.map { arrayOf(it) }
    }
}
