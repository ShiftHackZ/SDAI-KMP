package com.shifthackz.aisdv1.data.repository

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies release ordering and single ownership even when competing callers close a selection.
 *
 * @author Dmitriy Moroz
 */
class MnnModelSelectionLeaseImplTest {
    @Test
    fun `given selection lease, close is repeated, expected payload released before selection exactly once`() {
        // Given
        val releases = mutableListOf<String>()
        val lease = MnnModelSelectionLeaseImpl(
            "identity",
            AutoCloseable { releases += "payload" },
            AutoCloseable { releases += "selection" })

        // When
        lease.close()
        lease.close()

        // Then
        assertEquals(listOf("payload", "selection"), releases)
        assertEquals("identity", lease.activationId)
    }

    @Test
    fun `given selection lease, payload release fails, expected selection released and failure propagated`() {
        // Given
        val failure = IllegalStateException("release")
        var isSelectionReleased = false
        val lease = MnnModelSelectionLeaseImpl(
            "identity",
            AutoCloseable { throw failure },
            AutoCloseable { isSelectionReleased = true })

        // When
        val result = runCatching { lease.close() }
        lease.close()

        // Then
        assertSame(failure, result.exceptionOrNull())
        assertEquals(true, isSelectionReleased)
    }

    @Test
    fun `given selection lease, concurrent close requests arrive, expected one release per owner`() {
        // Given
        val payloadReleases = AtomicInteger()
        val selectionReleases = AtomicInteger()
        val lease = MnnModelSelectionLeaseImpl(
            "identity",
            AutoCloseable { payloadReleases.incrementAndGet() },
            AutoCloseable { selectionReleases.incrementAndGet() })
        val executor = Executors.newFixedThreadPool(4)

        // When
        try {
            executor.invokeAll(List(20) { Callable { lease.close() } }).forEach { it.get() }
        } finally {
            executor.shutdownNow()
        }

        // Then
        assertEquals(1, payloadReleases.get())
        assertEquals(1, selectionReleases.get())
    }

    @Test
    fun `given selection lease, both owners fail during cleanup, expected payload failure with suppressed selection failure`() {
        // Given
        val payloadFailure = IllegalStateException("payload")
        val selectionFailure = IllegalStateException("selection")
        val lease = MnnModelSelectionLeaseImpl(
            "identity",
            AutoCloseable { throw payloadFailure },
            AutoCloseable { throw selectionFailure })

        // When
        val failure = runCatching { lease.close() }.exceptionOrNull()

        // Then
        assertSame(payloadFailure, failure)
        assertEquals(listOf(selectionFailure), payloadFailure.suppressed.toList())
    }
}
