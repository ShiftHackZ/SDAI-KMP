package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResultPreview
import kotlinx.coroutines.flow.Flow

/**
 * Observes lightweight result identities and flags for a fixed page.
 *
 * @author Dmitriy Moroz
 */
interface ObserveGenerationResultPreviewPageUseCase {
    /**
     * Emits the requested preview page when stored results change.
     *
     * @param limit Maximum number of rows in the page.
     * @param offset Number of ordered rows to skip.
     * @author Dmitriy Moroz
     */
    operator fun invoke(limit: Int, offset: Int): Flow<List<AiGenerationResultPreview>>
}
