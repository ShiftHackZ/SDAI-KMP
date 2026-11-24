package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.FaqCategory

/**
 * Provides independent access to the published FAQ and its persistent local snapshot.
 *
 * @author Dmitriy Moroz
 */
interface FaqRepository {
    /**
     * Downloads and validates the FAQ. HTTP, decoding, and content failures propagate.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetch(): List<FaqCategory>

    /**
     * Reads the previously saved FAQ in publication order. Returns an empty list before the first save.
     *
     * @author Dmitriy Moroz
     */
    suspend fun readCache(): List<FaqCategory>

    /**
     * Replaces the saved FAQ in one transaction so readers see one complete revision.
     *
     * @param categories Validated categories and questions from the publisher.
     * @author Dmitriy Moroz
     */
    suspend fun writeCache(categories: List<FaqCategory>)
}
