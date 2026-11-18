package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest

/** Decides whether local generation requires a benchmark or recommendation confirmation.
 * @author Dmitriy Moroz
 */
interface EvaluateGenerationBenchmarkUseCase {
    /** Assesses the current request.
     * @param request Immutable generation settings.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(request: LocalGenerationRequest): LocalGenerationGateResult
}
