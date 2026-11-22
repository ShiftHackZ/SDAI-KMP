package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult

/**
 * Runs and saves the synthetic workload, propagating cancellation and failures.
 *
 * @author Dmitriy Moroz
 */
interface RunBenchmarkUseCase {
    /**
     * Returns the saved score and recommendations; workload/storage failures and cancellation propagate.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): BenchmarkResult
}
