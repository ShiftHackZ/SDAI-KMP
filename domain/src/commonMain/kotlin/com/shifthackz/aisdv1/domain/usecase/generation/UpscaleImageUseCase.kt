package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration

/**
 * Applies the captured upscaler to an unsaved image while retaining its generation provenance.
 *
 * @author Dmitriy Moroz
 */
interface UpscaleImageUseCase {
    /**
     * Resolves a downloaded upscaler and returns its enlarged output without gallery side effects.
     *
     * @param result Original image and metadata; its identity must be unsaved.
     * @param configuration Immutable dedicated upscaler selection.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        result: AiGenerationResult,
        configuration: UpscalerConfiguration
    ): AiGenerationResult
}
