package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.gateway.EasyDiffusionTaskRegistry

/** Observes queued and sampling progress independently from generation and interruption.
 * @param registry Shared operation ownership and progress.
 * @author Dmitriy Moroz */
internal class ObserveEasyDiffusionProgressUseCaseImpl(private val registry: EasyDiffusionTaskRegistry) :
    ObserveEasyDiffusionProgressUseCase {

    /** Returns the shared hot progress stream; idle state is reset when its owning operation finishes.
     * @author Dmitriy Moroz */
    override fun invoke() = registry.progress
}
