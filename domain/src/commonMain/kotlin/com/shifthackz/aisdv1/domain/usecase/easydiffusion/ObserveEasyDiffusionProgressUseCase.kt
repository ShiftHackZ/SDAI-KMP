package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import kotlinx.coroutines.flow.Flow

/** Observes queued and sampling progress independently from generation and interruption.
 * @author Dmitriy Moroz */
interface ObserveEasyDiffusionProgressUseCase {
    /** Returns the shared hot state flow. Idle progress is emitted before work and after the owning operation finishes; collectors do not start generation.
     * @author Dmitriy Moroz */
    operator fun invoke(): Flow<EasyDiffusionProgress>
}
