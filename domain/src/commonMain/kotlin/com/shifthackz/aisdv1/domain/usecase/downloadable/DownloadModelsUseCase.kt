package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import kotlinx.coroutines.flow.Flow

/**
 * Downloads one selected archive or missing catalog models in order, stopping at the first failure.
 * @author Dmitriy Moroz
 */
interface DownloadModelsUseCase {
    /**
     * Rechecks installed files before downloading and excludes custom entries without sources.
     * @param request Provider and optional single-model mirror confirmed by the caller.
     * @author Dmitriy Moroz
     */
    operator fun invoke(request: ModelDownloadRequest): Flow<ModelDownloadBatch>
}
