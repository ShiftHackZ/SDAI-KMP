package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.BlogDataSource
import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.domain.repository.BlogRepository

/**
 * Delegates blog reads and writes to their respective sources and preserves failures.
 *
 * @param remoteDataSource Downloads the published articles.
 * @param localDataSource Persists full articles in publication order.
 * @author Dmitriy Moroz
 */
internal class BlogRepositoryImpl(
    private val remoteDataSource: BlogDataSource.Remote,
    private val localDataSource: BlogDataSource.Local,
) : BlogRepository {
    override suspend fun fetch(): Result<List<BlogPost>> = remoteDataSource.fetch()

    override suspend fun readCache(): Result<List<BlogPost>> = localDataSource.read()

    override suspend fun writeCache(posts: List<BlogPost>): Result<Unit> =
        localDataSource.save(posts)
}
