package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.PerformanceReading
import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import com.shifthackz.aisdv1.domain.gateway.PerformanceGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive

/**
 * Samples only during visible opt-in collection, with bounded history and explicit unavailable
 * points on transient platform failures. Cancellation always propagates.
 *
 * @param preferences Persisted overlay flag and its invalidation stream.
 * @param gateway Platform counters, including Android inference children.
 * @param dispatchersProvider IO dispatcher for system and process-file reads.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class ObserveGenerationPerformanceUseCaseImpl(
    private val preferences: PreferenceManager,
    private val gateway: PerformanceGateway,
    private val dispatchersProvider: DispatchersProvider,
) : ObserveGenerationPerformanceUseCase {
    override fun invoke() = preferences.observe()
        .map { it.shouldShowPerformanceOverlay }
        .distinctUntilChanged()
        .flatMapLatest { isEnabled ->
            if (!isEnabled) flowOf(emptyList()) else history()
        }
        .flowOn(dispatchersProvider.io)

    private fun history() = flow {
        var previous: PerformanceReading? = null
        var samples = emptyList<PerformanceSample>()
        while (currentCoroutineContext().isActive) {
            val sample = try {
                val reading = gateway.read()
                val interval = previous?.let { reading.elapsedNanos - it.elapsedNanos } ?: 0L
                val cpu = previous?.takeIf { interval > 0L }?.let { baseline ->
                    val delta = reading.cpuTimeNanos.entries.sumOf { (identity, time) ->
                        baseline.cpuTimeNanos[identity]?.let { (time - it).coerceAtLeast(0L) } ?: 0L
                    }
                    (100.0 * delta / interval / reading.processorCount.coerceAtLeast(1))
                        .toFloat().coerceIn(0f, 100f)
                }
                val deviceCpu = reading.deviceCpu.getOrNull()?.let { current ->
                    previous?.deviceCpu?.getOrNull()?.let { baseline ->
                        val total = current.totalTicks - baseline.totalTicks
                        val active = current.activeTicks - baseline.activeTicks
                        if (total > 0 && active >= 0) (100.0 * active / total).toFloat()
                            .coerceIn(0f, 100f) else null
                    }
                }
                previous = reading
                PerformanceSample(
                    cpu,
                    reading.residentBytes,
                    reading.totalMemoryBytes,
                    deviceCpu,
                    reading.deviceUsedMemoryBytes
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                previous = null
                PerformanceSample()
            }
            samples = (samples + sample).takeLast(60)
            emit(samples)
            delay(1_000)
        }
    }
}
