package com.shifthackz.aisdv1.work

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Ensures hosts without the native bridge never admit unsupported GPU work.
 *
 * @author Dmitriy Moroz
 */
@Suppress("INVALID_CHARACTERS_NATIVE_ERROR")
class NoOpIosWorkExecutionTest {
    @Test
    fun `given a host without native execution, CPU work is submitted, expected immediate readiness without rejection`() {
        // Given
        val execution = NoOpIosWorkExecution()
        val events = mutableListOf<String>()
        // When
        execution.begin(
            "owner", "Work", "Loading", false,
            { events += "expired" }, { events += "ready" }, { events += "unavailable" })
        // Then
        assertEquals(listOf("ready"), events)
    }

    @Test
    fun `given a host without native execution, GPU work is submitted, expected rejection without readiness`() {
        // Given
        val execution = NoOpIosWorkExecution()
        val events = mutableListOf<String>()
        // When
        execution.begin(
            "owner", "Work", "Loading", true,
            { events += "expired" }, { events += "ready" }, { events += "unavailable" })
        // Then
        assertEquals(listOf("unavailable"), events)
    }

    @Test
    fun `given a host without native execution, unknown work is updated and ended, expected harmless cleanup`() {
        // Given
        val execution = NoOpIosWorkExecution()
        // When
        val outcome = runCatching {
            execution.update("unknown", "Work", "Loading", null)
            execution.end("unknown", false, "Cancelled", "")
            execution.end("unknown", false, "Cancelled", "")
        }
        // Then
        assertTrue(outcome.isSuccess)
    }
}
