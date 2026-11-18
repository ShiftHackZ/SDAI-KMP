package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Reads usable local choices before a potentially slow catalog request, without starting network work.
 *
 * @author Dmitriy Moroz
 */
interface GetCachedLocalModelCatalogUseCase {
    /**
     * Reads current completion/selection metadata; cache failures and cancellation propagate.
     *
     * @param type Runtime family whose cached choices should become visible immediately.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(type: LocalAiModel.Type): List<LocalAiModel>
}
