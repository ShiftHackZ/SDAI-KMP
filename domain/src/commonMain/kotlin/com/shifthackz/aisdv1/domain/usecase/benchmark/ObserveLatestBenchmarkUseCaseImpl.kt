package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes the latest persisted benchmark without starting a synthetic workload.
 *
 * @param repository Supplies the persistent latest-result stream, including null before the first saved result.
 *
 * @author Dmitriy Moroz
 */
internal class ObserveLatestBenchmarkUseCaseImpl(private val repository: BenchmarkRepository) :
    ObserveLatestBenchmarkUseCase {
    override operator fun invoke(): Flow<BenchmarkResult?> = repository.observeLatest()
}
