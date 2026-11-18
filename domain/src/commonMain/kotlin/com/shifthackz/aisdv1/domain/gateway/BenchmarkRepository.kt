package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import kotlinx.coroutines.flow.Flow

/**
 * Stores benchmark results in the local persistent database.
 *
 * @author Dmitriy Moroz
 */
interface BenchmarkRepository {
    /**
     * Observes the most recent saved result, emitting null before the first benchmark.
     * @author Dmitriy Moroz
     */
    fun observeLatest(): Flow<BenchmarkResult?>

    /**
     * Reads the latest persisted recommendation; storage failures propagate to the caller.
     * @author Dmitriy Moroz
     */
    suspend fun getLatest(): BenchmarkResult?

    /**
     * Persists the completed synthetic score and returns its assigned persistent identity.
     * @param result Completed hardware score and provider recommendations.
     * @author Dmitriy Moroz
     */
    suspend fun save(result: BenchmarkResult): BenchmarkResult
}
