package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogContent
import com.shifthackz.aisdv1.domain.repository.BlogRepository

/**
 * Refreshes the feed and keeps a complete snapshot for offline browsing.
 *
 * @param repository Access to current posts and the persistent snapshot.
 * @author Dmitriy Moroz
 */
internal class FetchOrGetBlogUseCaseImpl(
    private val repository: BlogRepository,
) : FetchOrGetBlogUseCase {

    override suspend fun invoke(): Result<BlogContent> {
        val posts = repository.fetch().getOrElse { failure ->
            val cached = repository.readCache().getOrNull().orEmpty()
            if (cached.isEmpty()) return Result.failure(failure)
            return Result.success(BlogContent(cached, isCached = true))
        }
        // The fetched articles remain available if saving the offline snapshot fails.
        repository.writeCache(posts)
        return Result.success(BlogContent(posts, isCached = false))
    }
}
