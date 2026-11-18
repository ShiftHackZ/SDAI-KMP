package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult

/**
 * Runs the synthetic hardware workload and persists its recommendations.
 *
 * @author Dmitriy Moroz
 */
interface BenchmarkRunner {
    /**
     * Returns the saved result; cancellation and workload/storage failures propagate.
     *
     * @author Dmitriy Moroz
     */
    suspend fun runBenchmark(): BenchmarkResult
}
