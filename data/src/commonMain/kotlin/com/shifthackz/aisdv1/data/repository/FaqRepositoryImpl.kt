package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.FaqDataSource
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.domain.repository.FaqRepository

/**
 * Delegates FAQ operations to their respective sources and reports failures to the caller.
 *
 * @param remoteDataSource Supplies the current published categories.
 * @param localDataSource Reads and replaces the saved offline snapshot.
 * @author Dmitriy Moroz
 */
internal class FaqRepositoryImpl(
    private val remoteDataSource: FaqDataSource.Remote,
    private val localDataSource: FaqDataSource.Local,
) : FaqRepository {
    override suspend fun fetch(): Result<List<FaqCategory>> = remoteDataSource.fetch()

    override suspend fun readCache(): Result<List<FaqCategory>> = localDataSource.read()

    override suspend fun writeCache(categories: List<FaqCategory>): Result<Unit> =
        localDataSource.save(categories)
}
