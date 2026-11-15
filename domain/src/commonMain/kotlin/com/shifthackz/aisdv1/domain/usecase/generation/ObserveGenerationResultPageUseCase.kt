package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import kotlinx.coroutines.flow.Flow

/**
 * Observes a fixed page of complete results, including original image payloads.
 *
 * @author Dmitriy Moroz
 */
interface ObserveGenerationResultPageUseCase {
    /**
     * Emits the requested page when stored results change.
     *
     * @param limit Maximum number of rows in the page.
     * @param offset Number of ordered rows to skip.
     * @author Dmitriy Moroz
     */
    operator fun invoke(limit: Int, offset: Int): Flow<List<AiGenerationResult>>
}
