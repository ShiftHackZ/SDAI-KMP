package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Observes overall MNN percentage without interpreting it as denoising steps.
 *
 * @author Dmitriy Moroz
 */
interface ObserveMnnProcessStatusUseCase {
    /**
     * Returns the active runtime's status; inactive owners emit no late progress.
     *
     * @return Overall percentage with total 100.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Flow<LocalDiffusionStatus>
}
