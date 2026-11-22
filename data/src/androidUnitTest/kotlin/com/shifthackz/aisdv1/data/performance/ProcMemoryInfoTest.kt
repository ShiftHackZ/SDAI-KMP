package com.shifthackz.aisdv1.data.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Guards the difference between unused RAM and reclaimable file cache in Linux counters.
 *
 * @author Dmitriy Moroz
 */
class ProcMemoryInfoTest {
    @Test
    fun `given device memory accounting, model cache is reclaimable, expected free counter excludes no cached pages`() {
        // Given
        val content =
            "MemTotal: 8000 kB\nMemAvailable: 6000 kB\nCached: 5000 kB\nMemFree:       1000 kB\n"

        // When
        val used = 8000 * 1024L - parseFreeMemoryBytes(content, 8000 * 1024L)

        // Then
        assertEquals(7000 * 1024L, used)
    }

    @Test
    fun `given device memory accounting, no free pages remain, expected full physical capacity in use`() {
        // Given
        val total = 8000 * 1024L

        // When
        val free = parseFreeMemoryBytes("MemFree:\t0 kB\n", total)

        // Then
        assertEquals(0L, free)
        assertEquals(total, total - free)
    }

    @Test
    fun `given device memory accounting, free counter is missing, expected read failure instead of available memory substitution`() {
        // Given
        val content = "MemAvailable: 6000 kB\nCached: 5000 kB\n"

        // When
        val failure = assertThrows(NoSuchElementException::class.java) {
            parseFreeMemoryBytes(
                content,
                8000 * 1024L
            )
        }

        // Then
        assertEquals(NoSuchElementException::class.java, failure.javaClass)
    }

    @Test
    fun `given device memory accounting, free counter has invalid units format or range, expected failed samples`() {
        // Given
        val counters = listOf("1 MB", "1", "-1 kB", "9000 kB", "9223372036854775807 kB", "oops kB")

        // When
        val failures = counters.map {
            runCatching {
                parseFreeMemoryBytes(
                    "MemFree: $it",
                    8000 * 1024L
                )
            }.isFailure
        }

        // Then
        assertEquals(List(counters.size) { true }, failures)
    }
}
