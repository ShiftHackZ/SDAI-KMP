package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.gateway.EasyDiffusionTaskRegistry

/** Cancels the owning generation coroutine; its cleanup stops only the accepted task.
 * @param registry Shared operation ownership and progress.
 * @author Dmitriy Moroz */
internal class InterruptEasyDiffusionUseCaseImpl(private val registry: EasyDiffusionTaskRegistry) :
    InterruptEasyDiffusionUseCase {
    /** Cancels only the exact background owner; its domain finally block stops the captured task.
     * @param ownerId Exact background job UUID.
     * @author Dmitriy Moroz */
    override suspend fun invoke(ownerId: String) = registry.cancel(ownerId)
}
