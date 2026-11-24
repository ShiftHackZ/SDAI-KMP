package com.shifthackz.aisdv1.domain.usecase.faq

import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.entity.FaqContent
import com.shifthackz.aisdv1.domain.entity.FaqQuestion
import com.shifthackz.aisdv1.domain.repository.FaqRepository
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
 * Exercises online-first selection, offline fallback, and cancellation at the FAQ policy boundary.
 *
 * @author Dmitriy Moroz
 */
class FetchOrGetFaqUseCaseImplTest {
    private val repository = mockk<FaqRepository>()
    private val useCase = FetchOrGetFaqUseCaseImpl(repository)
    private val entry =
        FaqQuestion("url", "Which URL?", listOf("Use your server URL."), listOf("server"))
    private val online = listOf(FaqCategory("setup", "Setup", listOf(entry)))
    private val cached = listOf(FaqCategory("basics", "Basics", listOf(entry)))
    private val networkFailure = IllegalStateException("Offline")

    @Test
    fun `given fetch-or-get FAQ use case and working network, FAQ is loaded, expected online result saved without reading cache`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns online
            coEvery { repository.writeCache(online) } returns Unit

            // When
            val content = useCase()

            // Then
            assertEquals(FaqContent(online, isCached = false), content)
            coVerifySequence {
                repository.fetch()
                repository.writeCache(online)
            }
        }

    @Test
    fun `given fetch-or-get FAQ use case with saved answers, remote request fails, expected cached content marked and no write`() =
        runTest {
            // Given
            coEvery { repository.fetch() } throws networkFailure
            coEvery { repository.readCache() } returns cached

            // When
            val content = useCase()

            // Then
            assertEquals(FaqContent(cached, isCached = true), content)
            coVerifySequence {
                repository.fetch()
                repository.readCache()
            }
        }

    @Test
    fun `given fetch-or-get FAQ use case before first download, network fails with empty cache, expected original failure`() =
        runTest {
            // Given
            coEvery { repository.fetch() } throws networkFailure
            coEvery { repository.readCache() } returns emptyList()

            // When
            val result = runCatching { useCase() }

            // Then
            assertSame(networkFailure, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.writeCache(any()) }
        }

    @Test
    fun `given fetch-or-get FAQ use case with unavailable cache, both reads fail, expected storage failure propagates`() =
        runTest {
            // Given
            val storageFailure = IllegalStateException("Room read failed")
            coEvery { repository.fetch() } throws networkFailure
            coEvery { repository.readCache() } throws storageFailure

            // When
            val result = runCatching { useCase() }

            // Then
            assertSame(storageFailure, result.exceptionOrNull())
        }

    @Test
    fun `given fetch-or-get FAQ use case and working network, saving fresh answers fails, expected online answers remain available`() =
        runTest {
            // Given
            coEvery { repository.fetch() } returns online
            coEvery { repository.writeCache(online) } throws IllegalStateException("Disk full")

            // When
            val content = useCase()

            // Then
            assertEquals(FaqContent(online, isCached = false), content)
            coVerify(exactly = 0) { repository.readCache() }
        }

    @Test
    fun `given fetch-or-get FAQ use case with active request, online fetch is cancelled, expected cancellation without cache access`() =
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
    fun `given fetch-or-get FAQ use case with fresh answers, cache write is cancelled, expected cancellation propagates`() =
        runTest {
            // Given
            val cancellation = CancellationException("Route closed")
            coEvery { repository.fetch() } returns online
            coEvery { repository.writeCache(online) } throws cancellation

            // When
            val result = runCatching { useCase() }

            // Then
            assertSame(cancellation, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.readCache() }
        }
}
