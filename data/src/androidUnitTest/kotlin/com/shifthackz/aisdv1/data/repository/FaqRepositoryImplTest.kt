package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.FaqDataSource
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Confirms FAQ repository operations route to the intended source without applying fallback policy.
 *
 * @author Dmitriy Moroz
 */
class FaqRepositoryImplTest {
    private val remote = mockk<FaqDataSource.Remote>()
    private val local = mockk<FaqDataSource.Local>()
    private val repository = FaqRepositoryImpl(remote, local)
    private val question = FaqQuestion("entry", "Where?", listOf("In Settings."), emptyList())
    private val online = listOf(FaqCategory("setup", "Setup", listOf(question)))
    private val cached = listOf(FaqCategory("basics", "Basics", listOf(question)))

    @Test
    fun `given FAQ repository with remote and local sources, fetch read and write run, expected each operation delegates exactly once`() =
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
    fun `given FAQ repository and remote failure, online content is fetched, expected failure propagates without reading or writing cache`() =
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
    fun `given FAQ repository and local failures, cache is read and saved, expected failures propagate without contacting remote`() =
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
}
