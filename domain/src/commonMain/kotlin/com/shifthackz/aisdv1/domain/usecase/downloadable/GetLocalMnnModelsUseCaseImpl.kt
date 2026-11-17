package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.mnn.MnnCatalogResult

/**
 * Keeps the existing Mnn model-list operation while shared catalog recovery owns offline policy.
 *
 * @param refreshCatalog Refreshes one provider and reads usable cached model states.
 *
 * @author Dmitriy Moroz
 */
internal class GetLocalMnnModelsUseCaseImpl(private val refreshCatalog: RefreshLocalModelCatalogUseCase) :
    GetLocalMnnModelsUseCase {
    /**
     * Returns current Mnn choices after an independent refresh; cancellation and cache failures propagate.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() = refreshCatalog(LocalAiModel.Type.Mnn).let { result ->
        MnnCatalogResult(
            result.models,
            result.failure
        )
    }
}
