package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies faithful repository streaming and per-model ownership through cancellation cleanup.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadModelUseCaseImplTest {
    private val repository = mockk<DownloadableModelRepository>()
    private val useCase = DownloadModelUseCaseImpl(repository)

    @Test
    fun `given model download, repository reports progress and activation, expected states and selected mirror preserved`() =
        runTest {
            // Given
            val states = listOf(
                DownloadState.Unknown,
                DownloadState.Downloading(33),
                DownloadState.Complete("/owned/model")
            )
            every {
                repository.download(
                    "model",
                    "https://mirror/model"
                )
            } returns flowOf(*states.toTypedArray())
            // When
            val actual = useCase("model", "https://mirror/model").toList()
            // Then
            assertEquals(states, actual)
            verify(exactly = 1) { repository.download("model", "https://mirror/model") }
        }

    @Test
    fun `given model download, repository emits failure state, expected same error without fallback`() =
        runTest {
            // Given
            val failure = IllegalStateException("offline")
            every { repository.download(any(), any()) } returns flowOf(DownloadState.Error(failure))
            // When
            val actual = useCase("model", "https://mirror/model").toList().single()
            // Then
            assertSame(failure, (actual as DownloadState.Error).error)
        }

    @Test
    fun `given model download, repository throws then retry succeeds, expected original failure and released model ownership`() =
        runTest {
            // Given
            val failure = IllegalStateException("transport closed")
            every { repository.download(any(), any()) } returnsMany listOf(
                flow { throw failure },
                flowOf(DownloadState.Complete("/owned/model"))
            )
            // When
            val actual =
                runCatching { useCase("model", "https://mirror/model").collect() }.exceptionOrNull()
            val retry = useCase("model", "https://mirror/model").toList()
            // Then
            assertSame(failure, actual)
            assertEquals(listOf(DownloadState.Complete("/owned/model")), retry)
        }

    @Test
    fun `given distinct model downloads, first transfer stays active, expected second transfer progresses concurrently`() =
        runTest {
            // Given
            val started = mutableListOf<String>()
            every { repository.download(any(), any()) } answers {
                val id = firstArg<String>()
                flow { started += id; emit(DownloadState.Downloading(10)); awaitCancellation() }
            }
            // When
            val first = launch { useCase("first", "https://mirror/first").collect() }
            val second = launch { useCase("second", "https://mirror/second").collect() }
            runCurrent()
            val activeIds = started.toList()
            first.cancelAndJoin()
            val isSecondStillRunning = second.isActive
            second.cancelAndJoin()
            // Then
            assertEquals(listOf("first", "second"), activeIds)
            assertTrue(isSecondStillRunning)
        }

    @Test
    fun `given same model replacement, cancelled archive cleanup is pending, expected replacement waits before writing`() =
        runTest {
            // Given
            val cleanupStarted = CompletableDeferred<Unit>()
            val releaseCleanup = CompletableDeferred<Unit>()
            every { repository.download("model", "https://first") } returns flow {
                try {
                    emit(DownloadState.Downloading(10)); awaitCancellation()
                } finally {
                    withContext(NonCancellable) { cleanupStarted.complete(Unit); releaseCleanup.await() }
                }
            }
            every { repository.download("model", "https://replacement") } returns flowOf(
                DownloadState.Complete("/owned/model")
            )
            val result = mutableListOf<DownloadState>()
            val first = launch { useCase("model", "https://first").collect() }
            runCurrent()
            // When
            first.cancel()
            runCurrent()
            cleanupStarted.await()
            val replacement = launch { useCase("model", "https://replacement").toList(result) }
            runCurrent()
            val beforeCleanup = result.toList()
            verify(exactly = 0) { repository.download("model", "https://replacement") }
            releaseCleanup.complete(Unit)
            first.join()
            replacement.join()
            // Then
            assertTrue(beforeCleanup.isEmpty())
            assertEquals(listOf(DownloadState.Complete("/owned/model")), result)
        }

    @Test
    fun `given same model queued download, waiter is cancelled, expected no repository work and later requests still succeed`() =
        runTest {
            // Given
            every {
                repository.download(
                    "model",
                    "https://first"
                )
            } returns flow { awaitCancellation() }
            every {
                repository.download(
                    "model",
                    "https://later"
                )
            } returns flowOf(DownloadState.Complete("/owned/model"))
            val first = launch { useCase("model", "https://first").collect() }
            runCurrent()
            val waiter = launch { useCase("model", "https://cancelled").collect() }
            runCurrent()
            // When
            waiter.cancelAndJoin()
            first.cancelAndJoin()
            val later = useCase("model", "https://later").toList()
            // Then
            assertFalse(waiter.isActive)
            verify(exactly = 0) { repository.download("model", "https://cancelled") }
            assertEquals(listOf(DownloadState.Complete("/owned/model")), later)
        }
}
