package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Observes a fixed page of complete results, including original image payloads.
 *
 * @param repository Owns persisted generation results and their ordering.
 * @author Dmitriy Moroz
 */
internal class ObserveGenerationResultPageUseCaseImpl(
    private val repository: GenerationResultRepository,
) : ObserveGenerationResultPageUseCase {
    /**
     * Emits the requested page when stored results change.
     *
     * @param limit Maximum number of rows in the page.
     * @param offset Number of ordered rows to skip.
     * @author Dmitriy Moroz
     */
    override operator fun invoke(limit: Int, offset: Int) = repository.observePage(limit, offset)
}
