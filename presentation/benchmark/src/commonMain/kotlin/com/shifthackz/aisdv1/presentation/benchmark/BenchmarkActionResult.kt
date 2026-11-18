package com.shifthackz.aisdv1.presentation.benchmark

/**
 * Result returned by benchmark platform actions.
 *
 * @author Dmitriy Moroz
 */
sealed interface BenchmarkActionResult {
    /**
     * Indicates that the action was completed or handed to the platform.
     *
     * @author Dmitriy Moroz
     */
    data object Done : BenchmarkActionResult

    /**
     * Indicates that the current platform cannot perform the action.
     *
     * @author Dmitriy Moroz
     */
    data object Unsupported : BenchmarkActionResult

    /**
     * Indicates that the platform action failed.
     *
     * @param message user-facing failure message.
     * @author Dmitriy Moroz
     */
    data class Failed(val message: String) : BenchmarkActionResult
}
