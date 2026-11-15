package com.shifthackz.aisdv1.domain.usecase.gallery

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Loads every complete gallery result, including original image payloads.
 *
 * @author Dmitriy Moroz
 */
interface GetAllGalleryUseCase {
    /**
     * Returns all stored results in repository order.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): List<AiGenerationResult>
}
