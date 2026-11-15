package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Observes lightweight result identities and flags for a fixed page.
 *
 * @param repository Owns persisted generation results and their ordering.
 * @author Dmitriy Moroz
 */
internal class ObserveGenerationResultPreviewPageUseCaseImpl(
    private val repository: GenerationResultRepository,
) : ObserveGenerationResultPreviewPageUseCase {
    /**
     * Emits the requested preview page when stored results change.
     *
     * @param limit Maximum number of rows in the page.
     * @param offset Number of ordered rows to skip.
     * @author Dmitriy Moroz
     */
    override operator fun invoke(limit: Int, offset: Int) =
        repository.observePagePreview(limit, offset)
}
