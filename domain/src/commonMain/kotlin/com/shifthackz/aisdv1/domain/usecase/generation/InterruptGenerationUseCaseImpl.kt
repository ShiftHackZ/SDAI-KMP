package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Routes explicit user cancellation to active native work before consulting the UI preference.
 *
 * @param preferenceManager Supplies the selected source when no native work is active.
 * @param nativeGenerationGateway Cancels the active native job on explicit user request.
 * @param interruptSourceGenerationUseCase Routes cancellation to the selected source repository.
 * @author Dmitriy Moroz
 */
internal class InterruptGenerationUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val nativeGenerationGateway: NativeGenerationGateway,
    private val interruptSourceGenerationUseCase: InterruptSourceGenerationUseCase,
) : InterruptGenerationUseCase {
    /**
     * Requests cancellation using the user-facing source selection.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() {
        if (nativeGenerationGateway.cancelActive()) return
        interruptSourceGenerationUseCase(preferenceManager.source)
    }
}
