package com.shifthackz.aisdv1.domain.usecase.benchmark


/**
 * Suppresses recommendation warnings until the next benchmark.
 *
 * @author Dmitriy Moroz
 */
interface SuppressBenchmarkWarningsUseCase {
    /**
     * Persists suppression of recommendation warnings; a completed benchmark resets that preference.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Unit
}
