package com.shifthackz.aisdv1.feature.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkResult
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRepository
import com.shifthackz.aisdv1.storage.db.persistent.dao.BenchmarkResultDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Observes and replaces the latest benchmark result through Room, returning its persisted identity.
 * Database failures propagate to the caller.
 *
 * @param dao Reads the latest row and atomically replaces the stored benchmark result.
 *
 * @author Dmitriy Moroz
 */
internal class RoomBenchmarkRepository(
    private val dao: BenchmarkResultDao,
) : BenchmarkRepository {

    override fun observeLatest(): Flow<BenchmarkResult?> =
        dao.observeLatest().map { entity -> entity?.toDomain() }

    override suspend fun getLatest(): BenchmarkResult? =
        dao.queryLatest()?.toDomain()

    override suspend fun save(result: BenchmarkResult): BenchmarkResult {
        val id = dao.replaceLatest(result.toEntity())
        return result.copy(id = id)
    }
}
