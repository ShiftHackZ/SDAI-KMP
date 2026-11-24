package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogPost
import com.shifthackz.aisdv1.domain.repository.BlogRepository

/**
 * Opens saved articles immediately and delegates online refresh and recovery to the feed loader.
 *
 * @param repository Reads the persistent article snapshot.
 * @param fetchOrGetBlog Refreshes the shared feed or recovers its last saved copy.
 * @author Dmitriy Moroz
 */
internal class FetchOrGetBlogPostUseCaseImpl(
    private val repository: BlogRepository,
    private val fetchOrGetBlog: FetchOrGetBlogUseCase,
) : FetchOrGetBlogPostUseCase {
    override suspend fun invoke(slug: String, shouldRefresh: Boolean): Result<BlogPost?> {
        if (!shouldRefresh) {
            val saved = repository.readCache().getOrNull()?.firstOrNull { it.slug == slug }
            if (saved != null) return Result.success(saved)
        }
        return fetchOrGetBlog().map { content -> content.posts.firstOrNull { it.slug == slug } }
    }
}
