package com.shifthackz.aisdv1.feature.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRepository
import com.shifthackz.aisdv1.domain.gateway.BenchmarkDeviceProbe
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRunner
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Runs a synthetic hardware score, saves its recommendations and resets warning suppression after saving.
 * Failures and cancellation propagate; the preference reset occurs only after persistence succeeds.
 *
 * @param deviceProbe Captures device capabilities without initializing an inference runtime.
 * @param scoreEngine Executes the synthetic CPU/memory workload and derives conservative recommendations.
 * @param repository Persists the completed result and assigns its durable identity.
 * @param preferenceManager Resets recommendation-warning suppression after a new result is saved.
 *
 * @author Dmitriy Moroz
 */
internal class BenchmarkManagerImpl internal constructor(
    private val deviceProbe: BenchmarkDeviceProbe,
    private val scoreEngine: BenchmarkScoreEngine,
    private val repository: BenchmarkRepository,
    private val preferenceManager: PreferenceManager,
) : BenchmarkRunner {

    override suspend fun runBenchmark(): BenchmarkResult {
        val result = scoreEngine.score(deviceProbe.capture())
        val saved = repository.save(result)
        preferenceManager.benchmarkRecommendationWarningSuppressed = false
        return saved
    }
}
