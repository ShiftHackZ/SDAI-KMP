package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Interrupts a worker's captured source without consulting mutable UI preferences.
 *
 * @author Dmitriy Moroz
 */
interface InterruptWorkerGenerationUseCase {
    /**
     * Cancels Android native sources only when the worker owner matches, with no local fallback.
     * Routes other captured sources to their repository interruption operation.
     * Remote callers must establish that this worker still owns the active task before invoking.
     *
     * @param source Source captured by the work request.
     * @param ownerId Immutable worker identity, never a wildcard native owner.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(source: ServerSource, ownerId: String)
}
