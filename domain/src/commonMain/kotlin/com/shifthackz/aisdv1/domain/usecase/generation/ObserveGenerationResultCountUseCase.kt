package com.shifthackz.aisdv1.domain.usecase.generation

import kotlinx.coroutines.flow.Flow

/**
 * Observes the total number of stored generation results.
 *
 * @author Dmitriy Moroz
 */
interface ObserveGenerationResultCountUseCase {
    /**
     * Emits the current count and subsequent storage changes.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Flow<Int>
}
