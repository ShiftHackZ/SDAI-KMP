package com.shifthackz.aisdv1.domain.usecase.benchmark


import com.shifthackz.aisdv1.domain.gateway.LocalGenerationBenchmarkGate

/**
 * Records that the initial benchmark question has been answered.
 *
 * @param gate Persists the initial-question answer independently of running a benchmark.
 *
 * @author Dmitriy Moroz
 */
internal class RecordBenchmarkPromptAnswerUseCaseImpl(private val gate: LocalGenerationBenchmarkGate) :
    RecordBenchmarkPromptAnswerUseCase {
    override operator fun invoke(): Unit = gate.markFirstBenchmarkPromptAnswered()
}
