package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository

/** Refreshes the configured instance catalog; fetch failures remain visible.
 * @param repository Strict endpoint-scoped catalog operations.
 * @author Dmitriy Moroz */
internal class FetchEasyDiffusionModelsUseCaseImpl(
    private val repository: EasyDiffusionRepository
) : FetchEasyDiffusionModelsUseCase {
    /** Refreshes the captured endpoint cache and propagates transport/decoding failures.
     * @param url Captured instance root or proxy prefix.
     * @author Dmitriy Moroz */
    override suspend fun invoke(url: String) = repository.fetchModels(url)
}
