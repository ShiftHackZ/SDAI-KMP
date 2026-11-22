package com.shifthackz.aisdv1.domain.usecase.appupdate

import com.shifthackz.aisdv1.domain.gateway.AppUpdateGateway
import com.shifthackz.aisdv1.domain.entity.AppUpdate
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the optional launch policy independently of distribution transport and UI lifetimes.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CheckAppUpdateUseCaseImplTest {
    @Test
    fun `given optional launch discovery, the gateway finds an update, expected the same offer`() =
        runTest {
            // Given
            val update =
                AppUpdate("1.3.0", "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.3.0")
            val useCase = CheckAppUpdateUseCaseImpl(gateway { update })

            // When
            val result = useCase()

            // Then
            assertSame(update, result)
        }

    @Test
    fun `given optional launch discovery, the installation is current, expected no offer`() =
        runTest {
            // Given
            val useCase = CheckAppUpdateUseCaseImpl(gateway { null })

            // When
            val result = useCase()

            // Then
            assertNull(result)
        }

    @Test
    fun `given optional launch discovery, a network failure occurs, expected startup continues without an offer`() =
        runTest {
            // Given
            val useCase = CheckAppUpdateUseCaseImpl(gateway { throw IOException("offline") })

            // When
            val result = useCase()

            // Then
            assertNull(result)
        }

    @Test
    fun `given optional launch discovery, metadata cannot be decoded, expected startup continues without an offer`() =
        runTest {
            // Given
            val useCase = CheckAppUpdateUseCaseImpl(gateway { error("Invalid metadata") })

            // When
            val result = useCase()

            // Then
            assertNull(result)
        }

    @Test
    fun `given optional launch discovery, the gateway suspends past ten seconds, expected cancellation and no offer`() =
        runTest {
            // Given
            var hasCancelled = false
            val useCase = CheckAppUpdateUseCaseImpl(gateway {
                try {
                    awaitCancellation()
                } finally {
                    hasCancelled = true
                }
            })
            val pending = async { useCase() }
            runCurrent()

            // When
            advanceTimeBy(9_999)
            val hasFinishedEarly = pending.isCompleted
            advanceTimeBy(1)
            runCurrent()
            val result = pending.await()

            // Then
            assertFalse(hasFinishedEarly)
            assertTrue(hasCancelled)
            assertNull(result)
            assertEquals(10_000L, testScheduler.currentTime)
        }

    @Test
    fun `given optional launch discovery, a response arrives before its deadline, expected the offer`() =
        runTest {
            // Given
            val update = AppUpdate("1.3.0", "https://example.com/release")
            val useCase = CheckAppUpdateUseCaseImpl(gateway { delay(9_999); update })

            // When
            val result = useCase()

            // Then
            assertSame(update, result)
            assertEquals(9_999L, testScheduler.currentTime)
        }

    @Test
    fun `given optional launch discovery, the gateway cancels, expected the original cancellation propagates`() =
        runTest {
            // Given
            val cancellation = CancellationException("Host destroyed")
            val useCase = CheckAppUpdateUseCaseImpl(gateway { throw cancellation })

            // When
            val failure = runCatching { useCase() }.exceptionOrNull()

            // Then
            assertTrue(generateSequence(failure) { it.cause }.any { it === cancellation })
        }

    @Test
    fun `given optional launch discovery, the host cancels its job, expected pending discovery cancels`() =
        runTest {
            // Given
            val started = CompletableDeferred<Unit>()
            var hasCancelled = false
            val useCase = CheckAppUpdateUseCaseImpl(gateway {
                started.complete(Unit)
                try {
                    awaitCancellation()
                } finally {
                    hasCancelled = true
                }
            })
            val pending = async { useCase() }
            started.await()

            // When
            pending.cancel()
            pending.join()

            // Then
            assertTrue(pending.isCancelled)
            assertTrue(hasCancelled)
        }

    @Test
    fun `given optional launch discovery, a nonrecoverable error occurs, expected the original error propagates`() =
        runTest {
            // Given
            val error = AssertionError("Broken invariant")
            val useCase = CheckAppUpdateUseCaseImpl(gateway { throw error })

            // When
            val failure = runCatching { useCase() }.exceptionOrNull()

            // Then
            assertTrue(generateSequence(failure) { it.cause }.any { it === error })
        }

    /**
     * Supplies a suspendable contract boundary for launch policy scenarios.
     *
     * @param check Distribution result or failure exercised by the scenario.
     * @author Dmitriy Moroz
     */
    private fun gateway(check: suspend () -> AppUpdate?): AppUpdateGateway =
        object : AppUpdateGateway {
            override suspend fun check(): AppUpdate? = check.invoke()
        }
}
