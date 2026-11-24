package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.FaqCategory

/**
 * Supplies FAQ categories in publication order from the website or a stored snapshot.
 *
 * @author Dmitriy Moroz
 */
sealed interface FaqDataSource {
    /**
     * Downloads the current FAQ through the public metadata service.
     *
     * @author Dmitriy Moroz
     */
    interface Remote : FaqDataSource {
        /**
         * Returns the published feed or a failure from its download or decoding.
         * Cancellation propagates to the caller.
         *
         * @author Dmitriy Moroz
         */
        suspend fun fetch(): Result<List<FaqCategory>>
    }

    /**
     * Retains published categories across app launches for offline reading.
     *
     * @author Dmitriy Moroz
     */
    interface Local : FaqDataSource {
        /**
         * Returns the saved snapshot in publication order, or an empty list before the first write.
         * Storage errors return failures. Cancellation propagates to the caller.
         *
         * @author Dmitriy Moroz
         */
        suspend fun read(): Result<List<FaqCategory>>

        /**
         * Replaces the snapshot atomically and returns a failure if storage cannot save it.
         * Cancellation propagates to the caller.
         *
         * @param categories Validated groups and questions in publication order.
         * @author Dmitriy Moroz
         */
        suspend fun save(categories: List<FaqCategory>): Result<Unit>
    }
}
