package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import kotlinx.coroutines.flow.Flow

/**
 * Supplies opt-in resource history for a visible generation progress dialog.
 * @author Dmitriy Moroz
 */
interface ObserveGenerationPerformanceUseCase {
    /**
     * Starts an independent one-second sampler on collection, retaining at most 60 samples.
     * Emits an empty history when disabled and stops sampling on cancellation. The first CPU
     * point is unavailable until two readings establish an interval.
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Flow<List<PerformanceSample>>
}
