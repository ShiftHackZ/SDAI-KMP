package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.CivitaiUploadLocalDataSource
import com.shifthackz.aisdv1.domain.entity.CivitaiUpload
import com.shifthackz.aisdv1.domain.repository.CivitaiUploadHistoryRepository
import kotlinx.coroutines.flow.Flow

/**
 * Supplies account-independent history projections without suppressing storage failures.
 *
 * @param local Persistent receipt identities and bounded page reads.
 * @author Dmitriy Moroz
 */
internal class CivitaiUploadHistoryRepositoryImpl(
    private val local: CivitaiUploadLocalDataSource,
) : CivitaiUploadHistoryRepository {
    /**
     * Observes newest-first positions retained after logout or gallery deletion.
     *
     * @author Dmitriy Moroz
     */
    override fun observeIds(): Flow<List<Long>> = local.observeIds()

    /**
     * Observes only the metadata needed for the current history viewport.
     *
     * @param limit Maximum retained complete receipt rows.
     * @param offset First global receipt index.
     * @author Dmitriy Moroz
     */
    override fun observePage(limit: Int, offset: Int): Flow<List<CivitaiUpload>> =
        local.observePage(limit, offset)
}
