package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.domain.repository.BlogRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Exercises online-first selection, offline fallback, and cancellation at the blog policy boundary.
 *
 * @author Dmitriy Moroz
 */
class FetchOrGetBlogUseCaseImplTest {
    private val repository = mockk<BlogRepository>()
    private val useCase = FetchOrGetBlogUseCaseImpl(repository)
    private val online = listOf(releaseArticle)
    private val cached = listOf(releaseArticle.copy(title = "Saved release"))
    private val networkFailure = IllegalStateException("Offline")

    @Test
    fun `given fetch-or-get blog use case and working network, blog is loaded, expected online result saved without reading cache`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns Result.success(online)
            coEvery { repository.writeCache(online) } returns Result.success(Unit)

            // When
            val content = useCase().getOrThrow()

            // Then
            assertEquals(BlogContent(online, isCached = false), content)
            coVerifySequence {
                repository.fetch()
                repository.writeCache(online)
            }
        }

    @Test
    fun `given fetch-or-get blog use case with saved articles, remote request fails, expected cached content marked and no write`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns Result.failure(networkFailure)
            coEvery { repository.readCache() } returns Result.success(cached)

            // When
            val content = useCase().getOrThrow()

            // Then
            assertEquals(BlogContent(cached, isCached = true), content)
            coVerifySequence {
                repository.fetch()
                repository.readCache()
            }
        }

    @Test
    fun `given fetch-or-get blog use case before first download, network fails with empty cache, expected original failure`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns Result.failure(networkFailure)
            coEvery { repository.readCache() } returns Result.success(emptyList())

            // When
            val result = useCase()

            // Then
            assertSame(networkFailure, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.writeCache(any()) }
        }

    @Test
    fun `given fetch-or-get blog use case with unavailable cache, both reads fail, expected original network failure is surfaced`() =
        runTest {
            // Given
            val storageFailure = IllegalStateException("Room read failed")
            coEvery { repository.fetch() } returns Result.failure(networkFailure)
            coEvery { repository.readCache() } returns Result.failure(storageFailure)

            // When
            val result = useCase()

            // Then
            assertSame(networkFailure, result.exceptionOrNull())
        }

    @Test
    fun `given fetch-or-get blog use case and working network, saving fresh articles fails, expected online articles remain available`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns Result.success(online)
            coEvery { repository.writeCache(online) } returns Result.failure(IllegalStateException("Disk full"))

            // When
            val content = useCase().getOrThrow()

            // Then
            assertEquals(BlogContent(online, isCached = false), content)
            coVerify(exactly = 0) { repository.readCache() }
        }

    @Test
    fun `given fetch-or-get blog use case with active request, online fetch is cancelled, expected cancellation without cache access`() =
        runTest {
            // Given
            val cancellation = CancellationException("Route closed")
            coEvery { repository.fetch() } throws cancellation

            // When
            val result = runCatching { useCase() }

            // Then
            assertSame(cancellation, result.exceptionOrNull())
            coVerifySequence { repository.fetch() }
        }

    @Test
    fun `given fetch-or-get blog use case with fresh articles, cache write is cancelled, expected cancellation propagates`() =
        runTest {
            // Given
            val cancellation = CancellationException("Route closed")
            coEvery { repository.fetch() } returns Result.success(online)
            coEvery { repository.writeCache(online) } throws cancellation

            // When
            val result = runCatching { useCase() }

            // Then
            assertSame(cancellation, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.readCache() }
        }

    @Test
    fun `given fetch-or-get blog use case with a saved revision, online feed returns empty, expected empty revision replaces the cache`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns Result.success(emptyList())
            coEvery { repository.writeCache(emptyList()) } returns Result.success(Unit)

            // When
            val content = useCase().getOrThrow()

            // Then
            assertEquals(BlogContent(emptyList(), isCached = false), content)
            coVerifySequence {
                repository.fetch()
                repository.writeCache(emptyList())
            }
        }
}
