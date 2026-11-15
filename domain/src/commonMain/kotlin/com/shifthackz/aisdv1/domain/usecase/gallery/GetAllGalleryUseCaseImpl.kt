package com.shifthackz.aisdv1.domain.usecase.gallery

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Loads every complete gallery result, including original image payloads.
 *
 * @param repository Owns persisted generation results and their ordering.
 * @author Dmitriy Moroz
 */
internal class GetAllGalleryUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetAllGalleryUseCase {
    /**
     * Returns all stored results in repository order.
     *
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke() = repository.getAll()
}
