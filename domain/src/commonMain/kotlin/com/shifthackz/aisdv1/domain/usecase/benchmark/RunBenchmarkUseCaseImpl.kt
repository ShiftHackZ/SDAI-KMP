package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRunner

/**
 * Runs the synthetic workload and persists its score/recommendations.
 *
 * @param runner Owns hardware probing, workload execution and saving the completed result.
 *
 * @author Dmitriy Moroz
 */
internal class RunBenchmarkUseCaseImpl(private val runner: BenchmarkRunner) : RunBenchmarkUseCase {
    override suspend operator fun invoke(): BenchmarkResult = runner.runBenchmark()
}
