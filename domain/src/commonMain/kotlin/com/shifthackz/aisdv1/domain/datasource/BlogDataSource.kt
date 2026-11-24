package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * Reads blog posts from the website or the device's saved snapshot.
 *
 * @author Dmitriy Moroz
 */
sealed interface BlogDataSource {
    /**
     * Supplies the current public feed.
     *
     * @author Dmitriy Moroz
     */
    interface Remote : BlogDataSource {
        /**
         * Returns the published feed or a failure from its download or decoding.
         * Cancellation propagates to the caller.
         *
         * @author Dmitriy Moroz
         */
        suspend fun fetch(): Result<List<BlogPost>>
    }

    /**
     * Retains full articles for offline reading.
     *
     * @author Dmitriy Moroz
     */
    interface Local : BlogDataSource {
        /**
         * Reads the saved articles in publication order, or an empty list before the first save.
         * Storage errors return failures. Cancellation propagates to the caller.
         *
         * @author Dmitriy Moroz
         */
        suspend fun read(): Result<List<BlogPost>>

        /**
         * Replaces the snapshot atomically and returns a failure if storage cannot save it.
         * Cancellation propagates to the caller.
         *
         * @param posts Complete feed in publication order.
         * @author Dmitriy Moroz
         */
        suspend fun save(posts: List<BlogPost>): Result<Unit>
    }
}
