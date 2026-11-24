package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * Resolves one published article using the saved snapshot and the current feed.
 *
 * @author Dmitriy Moroz
 */
interface FetchOrGetBlogPostUseCase {
    /**
     * Finds the selected article, returning a successful null when the feed has no matching post.
     * The initial read prefers the saved article. An explicit retry refreshes the feed first.
     * Feed failures remain in the result. Cancellation propagates.
     *
     * @param slug Stable identifier of the requested article.
     * @param shouldRefresh Whether to request fresh content before using the snapshot.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(slug: String, shouldRefresh: Boolean = false): Result<BlogPost?>
}
