package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository

/** Reads only the configured instance catalog after an explicit refresh.
 * @param repository Strict endpoint-scoped catalog operations.
 * @author Dmitriy Moroz */
internal class GetEasyDiffusionModelsUseCaseImpl(
    private val repository: EasyDiffusionRepository
) : GetEasyDiffusionModelsUseCase {
    /** Returns checkpoints only from the captured endpoint cache without consulting live settings.
     * @param url Captured instance root or proxy prefix.
     * @author Dmitriy Moroz */
    override suspend fun invoke(url: String) = repository.getModels(url)
}
