package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Owns optional progress observation for one remote generation lifetime.
 *
 * @author Dmitriy Moroz
 */
interface TrackRemoteGenerationUseCase {
    /**
     * Runs generation once and removes its progress on success, failure or cancellation.
     *
     * @param source Captured remote provider.
     * @param block Normal generation and persistence operation; never replayed.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        source: ServerSource,
        block: suspend () -> List<AiGenerationResult>
    ): List<AiGenerationResult>
}
