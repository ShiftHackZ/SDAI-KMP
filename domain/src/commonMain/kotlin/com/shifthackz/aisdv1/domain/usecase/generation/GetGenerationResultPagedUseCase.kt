package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Loads one page of complete generation results, including original image payloads.
 *
 * @author Dmitriy Moroz
 */
interface GetGenerationResultPagedUseCase {
    /**
     * Returns the requested page in repository order.
     *
     * @param limit Maximum number of rows in the page.
     * @param offset Number of ordered rows to skip.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(limit: Int, offset: Int): List<AiGenerationResult>
}
