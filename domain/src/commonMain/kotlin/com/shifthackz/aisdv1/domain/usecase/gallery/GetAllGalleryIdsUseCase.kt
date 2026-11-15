package com.shifthackz.aisdv1.domain.usecase.gallery

/**
 * Loads ordered gallery identities without retaining original image payloads.
 *
 * @author Dmitriy Moroz
 */
interface GetAllGalleryIdsUseCase {
    /**
     * Returns all stored identities in repository order.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): List<Long>
}
