package com.shifthackz.aisdv1.presentation.benchmark

/**
 * Defines platform side effects used by the benchmark screen.
 *
 * @author Dmitriy Moroz
 */
interface BenchmarkPlatformActions {
    /**
     * Shares text with the native platform share sheet.
     *
     * @param text text payload to share.
     * @return result of the platform action.
     * @author Dmitriy Moroz
     */
    suspend fun shareText(text: String): BenchmarkActionResult
}
