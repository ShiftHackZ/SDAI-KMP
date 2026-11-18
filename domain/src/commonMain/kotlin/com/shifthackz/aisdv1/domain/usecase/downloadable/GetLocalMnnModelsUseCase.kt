package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.mnn.MnnCatalogResult

/**
 * Refreshes the MNN setup catalog, retaining usable cached models when offline.
 *
 * @author Dmitriy Moroz
 */
interface GetLocalMnnModelsUseCase {
    /**
     * Returns current local model states; refresh failures remain explicit even when custom setup is available.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): MnnCatalogResult
}
