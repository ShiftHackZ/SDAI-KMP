package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Loads one page of complete generation results, including original image payloads.
 *
 * @param repository Owns persisted generation results and their ordering.
 * @author Dmitriy Moroz
 */
internal class GetGenerationResultPagedUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetGenerationResultPagedUseCase {
    /**
     * Returns the requested page in repository order.
     *
     * @param limit Maximum number of rows in the page.
     * @param offset Number of ordered rows to skip.
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke(limit: Int, offset: Int) =
        repository.getPage(limit, offset)
}
