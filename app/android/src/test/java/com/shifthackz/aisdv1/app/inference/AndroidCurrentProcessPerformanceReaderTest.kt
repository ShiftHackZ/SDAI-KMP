package com.shifthackz.aisdv1.app.inference

import android.app.Application
import android.os.Debug
import android.os.Process
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

/**
 * Checks the worker's public OS-counter conversion without reading a sibling process.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class AndroidCurrentProcessPerformanceReaderTest {
    @Before
    fun setUp() {
        mockkStatic(Debug::class, Process::class)
        every { Debug.getMemoryInfo(any()) } answers {
            firstArg<Debug.MemoryInfo>().nativePss = 4096
        }
        every { Process.myPid() } returns 42
        every { Process.getStartElapsedRealtime() } returns 123L
        every { Process.getElapsedCpuTime() } returns 75L
    }

    @After
    fun tearDown() {
        unmockkStatic(Debug::class, Process::class)
    }

    @Test
    fun `given worker telemetry, Android reports proportional resident kilobytes and CPU milliseconds, expected bytes nanoseconds and lifetime identity`() {
        // Given
        val reader = AndroidCurrentProcessPerformanceReader()

        // When
        val reading = reader.read()

        // Then
        assertEquals("42:123", reading.identity)
        assertEquals(75_000_000L, reading.cpuTimeNanos)
        assertEquals(4_194_304L, reading.residentBytes)
    }

    @Test
    fun `given worker telemetry, platform memory read fails, expected original failure propagated`() {
        // Given
        val failure = SecurityException("counter denied")
        every { Debug.getMemoryInfo(any()) } throws failure
        val reader = AndroidCurrentProcessPerformanceReader()

        // When
        val caught = runCatching { reader.read() }.exceptionOrNull()

        // Then
        assertSame(failure, caught)
    }

    @Test
    fun `given worker telemetry, proportional total includes swapped pages, expected only resident bytes counted as RAM`() {
        // Given
        var reportedTotalPss = 0
        every { Debug.getMemoryInfo(any()) } answers {
            val memory = firstArg<Debug.MemoryInfo>()
            memory.dalvikPss = 1024
            memory.nativePss = 2048
            memory.otherPss = 1024
            ReflectionHelpers.setField(memory, "nativeSwappedOutPss", 2048)
            reportedTotalPss = memory.totalPss
        }
        val reader = AndroidCurrentProcessPerformanceReader()

        // When
        val reading = reader.read()

        // Then
        assertEquals(6144, reportedTotalPss)
        assertEquals(4_194_304L, reading.residentBytes)
    }

    @Test
    fun `given worker telemetry, Android returns no resident counters, expected failed reading instead of misleading zero`() {
        // Given
        every { Debug.getMemoryInfo(any()) } returns Unit
        val reader = AndroidCurrentProcessPerformanceReader()

        // When
        val failure = runCatching { reader.read() }.exceptionOrNull()

        // Then
        assertEquals(IllegalStateException::class.java, failure?.javaClass)
    }
}
