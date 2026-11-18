package com.shifthackz.aisdv1.domain.usecase.benchmark

import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest
import com.shifthackz.aisdv1.domain.gateway.LocalGenerationBenchmarkGate

/**
 * Applies the persisted benchmark dialog policy to a captured local-generation request.
 *
 * @param gate Compares the captured request against saved recommendations and prompt preferences.
 *
 * @author Dmitriy Moroz
 */
internal class EvaluateGenerationBenchmarkUseCaseImpl(private val gate: LocalGenerationBenchmarkGate) :
    EvaluateGenerationBenchmarkUseCase {
    override suspend operator fun invoke(request: LocalGenerationRequest): LocalGenerationGateResult =
        gate.evaluate(request)
}
