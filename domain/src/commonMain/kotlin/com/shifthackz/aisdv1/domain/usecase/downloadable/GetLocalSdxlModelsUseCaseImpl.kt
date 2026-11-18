package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Keeps the existing Sdxl model-list operation while shared catalog recovery owns offline policy.
 *
 * @param refreshCatalog Refreshes one provider and reads usable cached model states.
 *
 * @author Dmitriy Moroz
 */
internal class GetLocalSdxlModelsUseCaseImpl(private val refreshCatalog: RefreshLocalModelCatalogUseCase) :
    GetLocalSdxlModelsUseCase {
    /**
     * Returns current Sdxl choices after an independent refresh; cancellation and cache failures propagate.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() =
        refreshCatalog(LocalAiModel.Type.Sdxl).let { result -> result.models }
}
