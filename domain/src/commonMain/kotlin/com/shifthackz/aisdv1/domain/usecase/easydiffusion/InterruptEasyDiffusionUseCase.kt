package com.shifthackz.aisdv1.domain.usecase.easydiffusion


/** Cancels the owning generation coroutine; its cleanup stops only the accepted task.
 * @author Dmitriy Moroz */
interface InterruptEasyDiffusionUseCase {
    /** Marks matching work cancelled before cancelling its coroutine; bounded cleanup stops only its captured positive task ID.
     * @param ownerId Exact background work UUID to cancel; foreground requests cancel their captured coroutine directly.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(ownerId: String)
}
