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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveGenerationPerformanceUseCaseImplTest {
    private val preferences = mockk<PreferenceManager>()
    private val gateway = mockk<PerformanceGateway>()
    private val settings = MutableStateFlow(Settings(shouldShowPerformanceOverlay = true))

    @Test
    fun `given performance observation, host and worker consume CPU, expected device-normalized sum and resident bytes`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
            every { preferences.observe() } returns settings
            coEvery { gateway.read() } returnsMany listOf(
                PerformanceReading(
                    1_000_000_000,
                    mapOf("host" to 100_000_000, "worker" to 100_000_000),
                    4,
                    100,
                    1000,
                    Result.success(CpuUsageCounters(200, 1000)),
                    600,
                ),
                PerformanceReading(
                    2_000_000_000,
                    mapOf("host" to 600_000_000, "worker" to 1_600_000_000),
                    4,
                    700,
                    1000,
                    Result.success(CpuUsageCounters(700, 2000)),
                    800,
                ),
            )
            val values = mutableListOf<List<PerformanceSample>>()
            val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)

            // When
            backgroundScope.launch { useCase().collect { values.add(it) } }
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertNull(values.first().last().cpuPercent)
            assertEquals(50f, values.last().last().cpuPercent)
            assertEquals(700L, values.last().last().residentBytes)
            assertEquals(1000L, values.last().last().totalMemoryBytes)
            assertEquals(50f, values.last().last().deviceCpuPercent)
            assertEquals(800L, values.last().last().deviceUsedMemoryBytes)
        }

    @Test
    fun `given performance observation, preference is disabled then enabled, expected sampling stops and history restarts`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
            every { preferences.observe() } returns settings
            coEvery { gateway.read() } returns PerformanceReading(
                1,
                mapOf("host" to 1),
                4,
                100,
                1000
            )
            val values = mutableListOf<List<PerformanceSample>>()
            val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)
            backgroundScope.launch { useCase().collect { values.add(it) } }
            runCurrent()

            // When
            settings.value = Settings(shouldShowPerformanceOverlay = false)
            runCurrent()
            advanceTimeBy(5_000)
            runCurrent()

            // Then
            assertTrue(values.last().isEmpty())
            coVerify(exactly = 1) { gateway.read() }
            settings.value = Settings(shouldShowPerformanceOverlay = true)
            runCurrent()
            assertEquals(1, values.last().size)
            assertNull(values.last().last().cpuPercent)
            coVerify(exactly = 2) { gateway.read() }
        }

    @Test
    fun `given performance observation, platform read fails then recovers, expected unavailable point and fresh CPU baseline`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
            every { preferences.observe() } returns settings
            var calls = 0
            coEvery { gateway.read() } answers {
                calls++
                if (calls == 2) throw IllegalStateException("Process exited")
                PerformanceReading(
                    calls * 1_000_000_000L,
                    mapOf("host" to calls * 100_000_000L),
                    4,
                    100,
                    1000
                )
            }
            val values = mutableListOf<List<PerformanceSample>>()
            val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)

            // When
            backgroundScope.launch { useCase().collect { values.add(it) } }
            runCurrent()
            advanceTimeBy(2_000)
            runCurrent()

            // Then
            assertEquals(PerformanceSample(), values[1].last())
            assertEquals(100L, values.last().last().residentBytes)
            assertNull(values.last().last().cpuPercent)
        }

    @Test
    fun `given performance observation, collection exceeds one minute then cancels, expected bounded history and no further reads`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
            every { preferences.observe() } returns settings
            var calls = 0
            coEvery { gateway.read() } answers {
                calls++
                PerformanceReading(
                    calls * 1_000_000_000L,
                    mapOf("host" to calls * 100_000_000L),
                    4,
                    100,
                    1000
                )
            }
            var history = emptyList<PerformanceSample>()
            val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)
            val job = backgroundScope.launch { useCase().collect { history = it } }

            // When
            runCurrent()
            advanceTimeBy(65_000)
            runCurrent()
            job.cancel()
            runCurrent()
            val countAtCancellation = calls
            advanceTimeBy(5_000)
            runCurrent()

            // Then
            assertEquals(60, history.size)
            assertEquals(countAtCancellation, calls)
        }

    @Test
    fun `given performance observation, platform operation is cancelled, expected cancellation propagates without unavailable history`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
            every { preferences.observe() } returns settings
            coEvery { gateway.read() } throws CancellationException("Stopped")
            val values = mutableListOf<List<PerformanceSample>>()
            val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)

            // When
            val job = backgroundScope.launch { useCase().collect { values.add(it) } }
            runCurrent()

            // Then
            coVerify(exactly = 1) { gateway.read() }
            job.cancel()
            assertTrue(values.isEmpty())
        }

    @Test
    fun `given performance observation, device CPU access is denied, expected app values retained and total CPU unavailable`() =
        runTest {
            // Given
            val dispatcher = StandardTestDispatcher(testScheduler)
            val dispatchers = mockk<DispatchersProvider> { every { io } returns dispatcher }
            every { preferences.observe() } returns settings
            var calls = 0L
            coEvery { gateway.read() } answers {
                calls++
                PerformanceReading(
                    elapsedNanos = calls * 1_000_000_000L,
                    cpuTimeNanos = mapOf("host" to calls * 1_000_000_000L),
                    processorCount = 4,
                    residentBytes = 100,
                    totalMemoryBytes = 1000,
                    deviceCpu = Result.failure(SecurityException("Device CPU restricted")),
                    deviceUsedMemoryBytes = 800,
                )
            }
            var history = emptyList<PerformanceSample>()
            val useCase = ObserveGenerationPerformanceUseCaseImpl(preferences, gateway, dispatchers)

            // When
            backgroundScope.launch { useCase().collect { history = it } }
            runCurrent()
            advanceTimeBy(1_000)
            runCurrent()

            // Then
            assertEquals(25f, history.last().cpuPercent)
            assertNull(history.last().deviceCpuPercent)
            assertEquals(100L, history.last().residentBytes)
            assertEquals(800L, history.last().deviceUsedMemoryBytes)
        }

}
