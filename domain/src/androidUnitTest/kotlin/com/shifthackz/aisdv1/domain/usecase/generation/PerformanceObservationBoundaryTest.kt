package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.CpuUsageCounters
import com.shifthackz.aisdv1.domain.entity.PerformanceReading
import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.gateway.PerformanceGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks counter discontinuities and preference changes at the sampler's public flow boundary.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PerformanceObservationBoundaryTest {
    @Test
    fun `given disabled performance observation, time passes, expected empty history and no platform reads`() =
        runTest {
            // Given
            val observation = observeReadings(reading(1))
            observation.settings.value = Settings(shouldShowPerformanceOverlay = false)

            // When
            runCurrent()
            advanceTimeBy(10_000)
            runCurrent()

            // Then
            assertEquals(listOf(emptyList<PerformanceSample>()), observation.values)
            coVerify(exactly = 0) { observation.gateway.read() }
        }

    @Test
    fun `given performance observation, unrelated setting changes, expected history and sampling cadence retained`() =
        runTest {
            // Given
            val observation = observeReadings(reading(1), reading(2))
            runCurrent()

            // When
            observation.settings.value = observation.settings.value.copy(languageCode = "de")
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertEquals(2, observation.values.last().size)
            assertEquals(25f, observation.values.last().last().cpuPercent)
            coVerify(exactly = 2) { observation.gateway.read() }
        }

    @Test
    fun `given performance observation, worker incarnation changes, expected new worker lifetime excluded from interval CPU`() =
        runTest {
            // Given
            val observation = observeReadings(
                reading(1).copy(cpuTimeNanos = mapOf("host" to 0L, "old-worker" to 900_000_000L)),
                reading(2).copy(
                    cpuTimeNanos = mapOf(
                        "host" to 1_000_000_000L,
                        "new-worker" to 80_000_000_000L
                    )
                ),
            )

            // When
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertEquals(25f, observation.values.last().last().cpuPercent)
        }

    @Test
    fun `given performance observation, worker exits, expected host CPU continues without negative worker delta`() =
        runTest {
            // Given
            val observation = observeReadings(
                reading(1).copy(cpuTimeNanos = mapOf("host" to 0L, "worker" to 80_000_000_000L)),
                reading(2).copy(cpuTimeNanos = mapOf("host" to 1_000_000_000L)),
            )

            // When
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertEquals(25f, observation.values.last().last().cpuPercent)
        }

    @Test
    fun `given performance observation, cumulative process CPU decreases, expected zero instead of negative usage`() =
        runTest {
            // Given
            val observation = observeReadings(reading(2).copy(elapsedNanos = 0), reading(1))

            // When
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertEquals(0f, observation.values.last().last().cpuPercent)
        }

    @Test
    fun `given performance observation, elapsed clock stalls or reverses then recovers, expected CPU gaps then fresh interval`() =
        runTest {
            // Given
            val observation = observeReadings(
                reading(1), reading(2).copy(elapsedNanos = 1_000_000_000L),
                reading(3).copy(elapsedNanos = 0), reading(4).copy(elapsedNanos = 1_000_000_000L),
            )

            // When
            runCurrent()
            advanceTimeBy(3_000)
            runCurrent()

            // Then
            val samples = observation.values.last()
            assertNull(samples[1].cpuPercent)
            assertNull(samples[2].cpuPercent)
            assertEquals(25f, samples[3].cpuPercent)
        }

    @Test
    fun `given performance observation, processor count is invalid and counters exceed capacity, expected CPU bounded to one hundred`() =
        runTest {
            // Given
            val observation = observeReadings(
                reading(1),
                reading(2).copy(
                    processorCount = 0,
                    cpuTimeNanos = mapOf("host" to 90_000_000_000L)
                ),
            )

            // When
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertEquals(100f, observation.values.last().last().cpuPercent)
        }

    @Test
    fun `given performance observation, device ticks stall reset or exceed capacity, expected missing invalid intervals and bounded valid usage`() =
        runTest {
            // Given
            val observation = observeReadings(
                reading(1).copy(deviceCpu = Result.success(CpuUsageCounters(10, 100))),
                reading(2).copy(deviceCpu = Result.success(CpuUsageCounters(20, 100))),
                reading(3).copy(deviceCpu = Result.success(CpuUsageCounters(0, 0))),
                reading(4).copy(deviceCpu = Result.success(CpuUsageCounters(200, 100))),
            )

            // When
            runCurrent()
            advanceTimeBy(3_000)
            runCurrent()

            // Then
            val samples = observation.values.last()
            assertNull(samples[1].deviceCpuPercent)
            assertNull(samples[2].deviceCpuPercent)
            assertEquals(100f, samples[3].deviceCpuPercent)
            assertTrue(samples.drop(1).all { it.cpuPercent == 25f })
        }

    @Test
    fun `given performance observation, device CPU becomes available, expected one baseline sample before total percentage`() =
        runTest {
            // Given
            val observation = observeReadings(
                reading(1),
                reading(2).copy(deviceCpu = Result.success(CpuUsageCounters(10, 100))),
                reading(3).copy(deviceCpu = Result.success(CpuUsageCounters(60, 200))),
            )

            // When
            runCurrent()
            advanceTimeBy(2_000)
            runCurrent()

            // Then
            val samples = observation.values.last()
            assertNull(samples[0].deviceCpuPercent)
            assertNull(samples[1].deviceCpuPercent)
            assertEquals(50f, samples[2].deviceCpuPercent)
        }

    private fun reading(second: Long) = PerformanceReading(
        elapsedNanos = second * 1_000_000_000L,
        cpuTimeNanos = mapOf("host" to second * 1_000_000_000L),
        processorCount = 4,
        residentBytes = second * 100,
        totalMemoryBytes = 1000,
    )

    private fun TestScope.observeReadings(vararg readings: PerformanceReading): Observation {
        val settings = MutableStateFlow(Settings())
        val preferences = mockk<PreferenceManager> { every { observe() } returns settings }
        val gateway = mockk<PerformanceGateway>()
        coEvery { gateway.read() } returnsMany readings.toList()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
        val observation = Observation(settings, gateway)
        val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)
        backgroundScope.launch { useCase().collect { observation.values.add(it) } }
        return observation
    }

    private class Observation(
        val settings: MutableStateFlow<Settings>,
        val gateway: PerformanceGateway,
    ) {
        val values = mutableListOf<List<PerformanceSample>>()
    }
}
