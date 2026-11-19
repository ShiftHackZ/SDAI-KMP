package com.shifthackz.aisdv1.feature.backup.restore

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultGenerationRestoreGateTest {
    @Test
    fun `given active generation, restore attempts admission, expected rejection without disturbing generation`() =
        runTest {
            // Given
            val gate = DefaultGenerationRestoreGate()
            val finish = CompletableDeferred<Unit>()
            val generation = async { gate.generating { finish.await() } }
            runCurrent()
            // When
            val failure = try {
                gate.restoring { }; null
            } catch (error: IllegalStateException) {
                error
            }
            // Then
            assertTrue(failure != null)
            assertTrue(generation.isActive)
            finish.complete(Unit)
            generation.await()
        }

    @Test
    fun `given concurrent generation requests, both start before restore, expected existing concurrency preserved`() =
        runTest {
            // Given
            val gate = DefaultGenerationRestoreGate()
            val finish = CompletableDeferred<Unit>()
            var active = 0
            // When
            val first = async { gate.generating { active++; finish.await() } }
            val second = async { gate.generating { active++; finish.await() } }
            runCurrent()
            // Then
            assertTrue(active == 2)
            finish.complete(Unit)
            first.await(); second.await()
        }

    @Test
    fun `given active restore, generation is submitted, expected dispatch waits until restore completes`() =
        runTest {
            // Given
            val gate = DefaultGenerationRestoreGate()
            val finish = CompletableDeferred<Unit>()
            val restoration = async { gate.restoring { finish.await() } }
            runCurrent()
            var hasDispatched = false
            // When
            val generation = async { gate.generating { hasDispatched = true } }
            runCurrent()
            // Then
            assertFalse(hasDispatched)
            finish.complete(Unit)
            restoration.await(); generation.await()
            assertTrue(hasDispatched)
        }

    @Test
    fun `given generation cancellation, its owner exits, expected subsequent restore admitted`() =
        runTest {
            // Given
            val gate = DefaultGenerationRestoreGate()
            val waiting = CompletableDeferred<Unit>()
            val generation = async { gate.generating { waiting.await() } }
            runCurrent()
            // When
            generation.cancelAndJoin()
            val hasRestored = gate.restoring { true }
            // Then
            assertTrue(hasRestored)
        }
}
