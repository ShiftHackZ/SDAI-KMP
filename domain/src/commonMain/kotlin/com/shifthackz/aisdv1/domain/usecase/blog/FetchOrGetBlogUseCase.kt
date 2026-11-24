package com.shifthackz.aisdv1.domain.usecase.blog

import com.shifthackz.aisdv1.domain.entity.BlogContent

/**
 * Loads the blog online first and falls back to the saved revision.
 *
 * @author Dmitriy Moroz
 */
interface FetchOrGetBlogUseCase {
    /**
     * Loads the current feed, falling back to a saved copy on ordinary failures.
     * Returns a failure if neither source can supply content. Cancellation propagates.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): Result<BlogContent>
}
