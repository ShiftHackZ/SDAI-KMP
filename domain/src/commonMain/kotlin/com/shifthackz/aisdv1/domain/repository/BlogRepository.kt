package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.BlogPost

/**
 * Provides independent blog network and snapshot operations with explicit failures.
 * Cancellation propagates from all operations.
 *
 * @author Dmitriy Moroz
 */
interface BlogRepository {
    /**
     * Downloads the complete feed and returns transport or decoding failures in the result.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetch(): Result<List<BlogPost>>

    /**
     * Reads the last saved revision or an empty list if none exists. Storage errors return failures.
     *
     * @author Dmitriy Moroz
     */
    suspend fun readCache(): Result<List<BlogPost>>

    /**
     * Stores a complete revision in one transaction and returns a failure if the write fails.
     *
     * @param posts Articles to save in publication order.
     * @author Dmitriy Moroz
     */
    suspend fun writeCache(posts: List<BlogPost>): Result<Unit>
}
