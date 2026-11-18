package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Keeps the existing MediaPipe model-list operation while shared catalog recovery owns offline policy.
 *
 * @param refreshCatalog Refreshes one provider and reads usable cached model states.
 *
 * @author Dmitriy Moroz
 */
internal class GetLocalMediaPipeModelsUseCaseImpl(private val refreshCatalog: RefreshLocalModelCatalogUseCase) :
    GetLocalMediaPipeModelsUseCase {
    /**
     * Returns current MediaPipe choices after an independent refresh; cancellation and cache failures propagate.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() =
        refreshCatalog(LocalAiModel.Type.MediaPipe).let { result -> result.models }
}
