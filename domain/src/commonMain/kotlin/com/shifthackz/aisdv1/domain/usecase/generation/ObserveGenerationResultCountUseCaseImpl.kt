package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Observes the total number of stored generation results.
 *
 * @param repository Owns persisted generation results and their ordering.
 * @author Dmitriy Moroz
 */
internal class ObserveGenerationResultCountUseCaseImpl(
    private val repository: GenerationResultRepository,
) : ObserveGenerationResultCountUseCase {
    /**
     * Emits the current count and subsequent storage changes.
     *
     * @author Dmitriy Moroz
     */
    override operator fun invoke() = repository.observeCount()
}
