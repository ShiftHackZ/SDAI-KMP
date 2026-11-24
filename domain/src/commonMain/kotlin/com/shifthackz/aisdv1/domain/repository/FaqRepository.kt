package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.FaqCategory

/**
 * Provides independent access to the published FAQ and its persistent local snapshot.
 * Cancellation propagates from all operations.
 *
 * @author Dmitriy Moroz
 */
interface FaqRepository {
    /**
     * Downloads and validates the FAQ. HTTP, decoding, and content errors return failures.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetch(): Result<List<FaqCategory>>

    /**
     * Reads the saved FAQ in publication order or an empty list before the first save.
     * Storage errors return failures.
     *
     * @author Dmitriy Moroz
     */
    suspend fun readCache(): Result<List<FaqCategory>>

    /**
     * Replaces the saved FAQ in one transaction and returns a failure if the write fails.
     *
     * @param categories Validated categories and questions from the publisher.
     * @author Dmitriy Moroz
     */
    suspend fun writeCache(categories: List<FaqCategory>): Result<Unit>
}
