package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Exercises prompt cancellation at the real production dispatch delivery boundary.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MnnSelectionDeliveryTest {
    @Test
    fun `given MNN selection delivery, caller cancels after validation before dispatch return, expected acquired lease closed once`() =
        runTest {
            // Given
            val dispatcher = QueuedDispatcher()
            var closeCount = 0
            val lease = object : MnnModelSelectionLease {
                override val activationId = "identity"
                override fun close() {
                    closeCount++
                }
            }
            val delivery = async { deliverMnnSelection(dispatcher) { lease } }
            runCurrent()

            // When
            dispatcher.runNext()
            delivery.cancel()
            runCurrent()

            // Then
            assertEquals(1, closeCount)
        }

    @Test
    fun `given MNN selection delivery, dispatch succeeds, expected caller retains the live lease`() =
        runTest {
            // Given
            val dispatcher = QueuedDispatcher()
            var closeCount = 0
            val lease = object : MnnModelSelectionLease {
                override val activationId = "identity"
                override fun close() {
                    closeCount++
                }
            }
            val delivery = async { deliverMnnSelection(dispatcher) { lease } }
            runCurrent()

            // When
            dispatcher.runNext()
            runCurrent()
            val result = delivery.await()

            // Then
            assertSame(lease, result)
            assertEquals(0, closeCount)
            result.close()
            assertEquals(1, closeCount)
        }
}

private class QueuedDispatcher : CoroutineDispatcher() {
    private val pending = ArrayDeque<Runnable>()
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        pending.addLast(block)
    }

    fun runNext() = pending.removeFirst().run()
}
