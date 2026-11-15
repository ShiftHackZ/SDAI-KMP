package com.shifthackz.aisdv1.domain.usecase.generation

/**
 * Cancels generation requested by the user, using the active native owner before the selected source.
 *
 * @author Dmitriy Moroz
 */
interface InterruptGenerationUseCase {
    /**
     * Requests cancellation of active native work, or interrupts the currently selected source.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}
