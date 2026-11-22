package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import kotlinx.coroutines.flow.Flow

/**
 * Reads local upload history regardless of the current Civitai authorization state.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiUploadHistoryRepository {
    /**
     * Observes ordered receipt identities for lazy-list positions.
     *
     * @author Dmitriy Moroz
     */
    fun observeIds(): Flow<List<Long>>

    /**
     * Observes only the receipts around the current viewport.
     *
     * @param limit Maximum retained receipt rows.
     * @param offset First global receipt index.
     * @author Dmitriy Moroz
     */
    fun observePage(limit: Int, offset: Int): Flow<List<CivitaiUpload>>
}
