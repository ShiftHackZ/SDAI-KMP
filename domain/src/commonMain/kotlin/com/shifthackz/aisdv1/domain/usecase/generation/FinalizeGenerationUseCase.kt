package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Applies normal gallery and media export policy after image postprocessing.
 *
 * @author Dmitriy Moroz
 */
interface FinalizeGenerationUseCase {
    /**
     * Saves final pixels when autosave or background execution requires durable output.
     *
     * @param result Completed unsaved image retaining the originating provider identity guards.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(result: AiGenerationResult): AiGenerationResult
}
