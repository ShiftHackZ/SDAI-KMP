package com.shifthackz.aisdv1.domain.usecase.gallery

import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Loads ordered gallery identities without retaining original image payloads.
 *
 * @param repository Owns persisted generation results and their ordering.
 * @author Dmitriy Moroz
 */
internal class GetAllGalleryIdsUseCaseImpl(
    private val repository: GenerationResultRepository,
) : GetAllGalleryIdsUseCase {
    /**
     * Returns all stored identities in repository order.
     *
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke() = repository.getAllIds()
}
