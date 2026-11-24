package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.releasePost
import com.shifthackz.aisdv1.domain.datasource.BlogDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Confirms blog repository operations route to the intended source without applying fallback policy.
 *
 * @author Dmitriy Moroz
 */
class BlogRepositoryImplTest {
    private val remote = mockk<BlogDataSource.Remote>()
    private val local = mockk<BlogDataSource.Local>()
    private val repository = BlogRepositoryImpl(remote, local)
    private val online = listOf(releasePost)
    private val cached = listOf(releasePost.copy(title = "Saved release"))

    @Test
    fun `given blog repository with remote and local sources, fetch read and write run, expected each operation delegates exactly once`() =
        runTest {
            // Given
            coEvery { remote.fetch() } returns Result.success(online)
            coEvery { local.read() } returns Result.success(cached)
            coEvery { local.save(online) } returns Result.success(Unit)

            // When
            val fetched = repository.fetch().getOrThrow()
            val read = repository.readCache().getOrThrow()
            repository.writeCache(fetched).getOrThrow()

            // Then
            assertSame(online, fetched)
            assertSame(cached, read)
            coVerifySequence {
                remote.fetch()
                local.read()
                local.save(online)
            }
        }

    @Test
    fun `given blog repository and remote failure, online content is fetched, expected failure propagates without reading or writing cache`() =
        runTest {
            // Given
            val failure = IllegalStateException("Offline")
            coEvery { remote.fetch() } returns Result.failure(failure)

            // When
            val result = repository.fetch()

            // Then
            assertSame(failure, result.exceptionOrNull())
            coVerify(exactly = 0) { local.read(); local.save(any()) }
        }

    @Test
    fun `given blog repository and local failures, cache is read and saved, expected failures propagate without contacting remote`() =
        runTest {
            // Given
            val readFailure = IllegalStateException("Cache unavailable")
            val writeFailure = IllegalStateException("Write refused")
            coEvery { local.read() } returns Result.failure(readFailure)
            coEvery { local.save(online) } returns Result.failure(writeFailure)

            // When
            val read = repository.readCache()
            val write = repository.writeCache(online)

            // Then
            assertSame(readFailure, read.exceptionOrNull())
            assertSame(writeFailure, write.exceptionOrNull())
            coVerify(exactly = 0) { remote.fetch() }
        }

    @Test
    fun `given blog repository and cancelled download, fetch starts, expected cancellation is thrown without reading cache`() =
        runTest {
            // Given
            val cancellation = CancellationException("Blog route closed")
            coEvery { remote.fetch() } throws cancellation

            // When
            val result = runCatching { repository.fetch() }

            // Then
            assertSame(cancellation, result.exceptionOrNull())
            coVerify(exactly = 0) { local.read() }
        }
}
