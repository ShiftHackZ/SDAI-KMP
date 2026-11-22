package com.shifthackz.aisdv1.domain.usecase.benchmark


/**
 * Records that the initial benchmark question has been answered.
 *
 * @author Dmitriy Moroz
 */
interface RecordBenchmarkPromptAnswerUseCase {
    /**
     * Persists the answered flag so future local generation does not repeat the initial question.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Unit
}
