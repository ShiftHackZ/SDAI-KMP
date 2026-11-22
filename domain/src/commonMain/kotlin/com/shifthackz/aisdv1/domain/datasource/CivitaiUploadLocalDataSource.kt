package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import kotlinx.coroutines.flow.Flow

/**
 * Durable receipt storage independent of account credentials and full gallery image payloads.
 *
 * @author Dmitriy Moroz
 */
interface CivitaiUploadLocalDataSource {
    /**
     * Observes stable newest-first receipt identities after any required legacy conversion.
     *
     * @author Dmitriy Moroz
     */
    fun observeIds(): Flow<List<Long>>

    /**
     * Observes a bounded history page, including receipts from signed-out accounts.
     *
     * @param limit Maximum complete receipts in the requested window.
     * @param offset First global receipt index.
     * @author Dmitriy Moroz
     */
    fun observePage(limit: Int, offset: Int): Flow<List<CivitaiUpload>>

    /**
     * Finds an original-image receipt without loading unrelated history.
     *
     * @param imageId Persistent gallery identity.
     * @param imageCreatedAt Original gallery timestamp protecting ID reuse.
     * @param imageRevision Original image-content revision.
     * @param accountId Optional account restriction for phase replacement.
     * @author Dmitriy Moroz
     */
    suspend fun find(
        imageId: Long,
        imageCreatedAt: Long,
        imageRevision: String,
        accountId: Long? = null
    ): CivitaiUpload?

    /**
     * Commits a phase before returning its stable receipt identity; storage failures propagate.
     *
     * @param receipt Complete attempted or confirmed publication record.
     * @author Dmitriy Moroz
     */
    suspend fun save(receipt: CivitaiUpload): CivitaiUpload
}
