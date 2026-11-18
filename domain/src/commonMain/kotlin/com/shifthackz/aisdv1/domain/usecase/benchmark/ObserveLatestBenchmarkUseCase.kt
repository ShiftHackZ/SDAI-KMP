package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import kotlinx.coroutines.flow.Flow

/** Observes the persisted latest synthetic benchmark without starting a workload.
 * @author Dmitriy Moroz
 */
interface ObserveLatestBenchmarkUseCase {
    /**
     * Returns a flow of the latest saved result, emitting null until a benchmark is stored.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Flow<BenchmarkResult?>
}
