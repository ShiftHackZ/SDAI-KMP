package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Routes a known source to its repository's interruption operation.
 *
 * @author Dmitriy Moroz
 */
interface InterruptSourceGenerationUseCase {
    /**
     * Requests interruption where the source supports it; unsupported sources are a no-op.
     *
     * @param source Explicit source to interrupt, independent of current preferences.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(source: ServerSource)
}
