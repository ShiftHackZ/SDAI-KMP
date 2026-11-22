package com.shifthackz.aisdv1.presentation.appupdate

import com.shifthackz.aisdv1.domain.usecase.appupdate.CheckAppUpdateUseCase
import com.shifthackz.aisdv1.domain.entity.AppUpdate
import com.shifthackz.aisdv1.presentation.appupdate.di.appUpdatePresentationModule
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/**
 * Protects discovery ownership, cancellation retry and process-lived dismissal across hosts.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppUpdateLaunchStateImplTest {
    private val offer =
        AppUpdate("1.3.0", "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.3.0")

    @Test
    fun `given launch notification state, no home screen has requested discovery, expected no work or pending offer`() {
        // Given
        var calls = 0
        val state = AppUpdateLaunchStateImpl { calls++; offer }

        // When
        val pending = state.update.value

        // Then
        assertNull(pending)
        assertEquals(0, calls)
    }

    @Test
    fun `given launch notification state, first discovery returns an update, expected the offer is published`() =
        runTest {
            // Given
            val state = AppUpdateLaunchStateImpl { offer }

            // When
            state.checkOnce()

            // Then
            assertSame(offer, state.update.value)
        }

    @Test
    fun `given launch notification state, multiple hosts request discovery, expected only one use case call`() =
        runTest {
            // Given
            var calls = 0
            val state = AppUpdateLaunchStateImpl { calls++; offer }

            // When
            state.checkOnce()
            state.checkOnce()

            // Then
            assertEquals(1, calls)
            assertSame(offer, state.update.value)
        }

    @Test
    fun `given launch notification state, a concurrent host requests an in flight check, expected one discovery`() =
        runTest {
            // Given
            var calls = 0
            val response = CompletableDeferred<AppUpdate?>()
            val state = AppUpdateLaunchStateImpl { calls++; response.await() }
            val first = launch { state.checkOnce() }
            runCurrent()

            // When
            state.checkOnce()
            response.complete(offer)
            first.join()

            // Then
            assertEquals(1, calls)
            assertSame(offer, state.update.value)
        }

    @Test
    fun `given launch notification state, the user dismisses and returns home, expected no repeat offer or lookup`() =
        runTest {
            // Given
            var calls = 0
            val state = AppUpdateLaunchStateImpl { calls++; offer }
            state.checkOnce()

            // When
            state.dismiss()
            state.checkOnce()

            // Then
            assertNull(state.update.value)
            assertEquals(1, calls)
        }

    @Test
    fun `given launch notification state, discovery returns no update, expected later hosts do not retry`() =
        runTest {
            // Given
            var calls = 0
            val state = AppUpdateLaunchStateImpl { calls++; null }

            // When
            state.checkOnce()
            state.checkOnce()

            // Then
            assertNull(state.update.value)
            assertEquals(1, calls)
        }

    @Test
    fun `given launch notification state, a host is cancelled during discovery, expected another host can retry`() =
        runTest {
            // Given
            var calls = 0
            val started = CompletableDeferred<Unit>()
            val response = CompletableDeferred<AppUpdate?>()
            val state = AppUpdateLaunchStateImpl {
                calls++
                started.complete(Unit)
                if (calls == 1) response.await() else offer
            }
            val first = launch { state.checkOnce() }
            started.await()

            // When
            first.cancel()
            first.join()
            state.checkOnce()

            // Then
            assertTrue(first.isCancelled)
            assertEquals(2, calls)
            assertSame(offer, state.update.value)
        }

    @Test
    fun `given launch notification state, discovery throws cancellation, expected the same cancellation and no pending offer`() =
        runTest {
            // Given
            val cancellation = CancellationException("Host disposed")
            val state = AppUpdateLaunchStateImpl { throw cancellation }

            // When
            val failure = runCatching { state.checkOnce() }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            assertNull(state.update.value)
        }

    @Test
    fun `given launch notification state, an empty notification is dismissed repeatedly, expected no discovery`() =
        runTest {
            // Given
            var calls = 0
            val state = AppUpdateLaunchStateImpl { calls++; offer }

            // When
            state.dismiss()
            state.dismiss()

            // Then
            assertNull(state.update.value)
            assertEquals(0, calls)
        }

    @Test
    fun `given process scoped presentation DI, two hosts resolve launch state, expected shared discovery and dismissal`() =
        runTest {
            // Given
            var calls = 0
            val application = koinApplication {
                modules(
                    module { single<CheckAppUpdateUseCase> { CheckAppUpdateUseCase { calls++; offer } } },
                    appUpdatePresentationModule
                )
            }
            try {
                val first = application.koin.get<AppUpdateLaunchState>()
                val second = application.koin.get<AppUpdateLaunchState>()

                // When
                first.checkOnce()
                second.dismiss()
                first.checkOnce()

                // Then
                assertSame(first, second)
                assertEquals(1, calls)
                assertNull(first.update.value)
            } finally {
                application.close()
            }
        }

    @Test
    fun `given a completed process launch, a new process state is created, expected discovery is eligible again`() =
        runTest {
            // Given
            var calls = 0
            val check = CheckAppUpdateUseCase { calls++; offer }
            val previous = AppUpdateLaunchStateImpl(check)
            previous.checkOnce()
            previous.dismiss()
            val current = AppUpdateLaunchStateImpl(check)

            // When
            current.checkOnce()

            // Then
            assertEquals(2, calls)
            assertSame(offer, current.update.value)
        }
}
