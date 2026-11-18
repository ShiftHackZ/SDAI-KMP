package com.shifthackz.aisdv1.domain.usecase.benchmark


import com.shifthackz.aisdv1.domain.gateway.LocalGenerationBenchmarkGate

/**
 * Persists warning suppression until a subsequent benchmark completes.
 *
 * @param gate Writes the warning-suppression preference through the admission policy.
 *
 * @author Dmitriy Moroz
 */
internal class SuppressBenchmarkWarningsUseCaseImpl(private val gate: LocalGenerationBenchmarkGate) :
    SuppressBenchmarkWarningsUseCase {
    override operator fun invoke(): Unit = gate.suppressRecommendationWarnings()
}
