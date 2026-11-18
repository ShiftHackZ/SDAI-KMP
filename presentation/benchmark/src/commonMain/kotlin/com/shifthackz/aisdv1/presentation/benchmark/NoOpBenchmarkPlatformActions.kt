package com.shifthackz.aisdv1.presentation.benchmark

/**
 * No-op fallback for previews and tests.
 *
 * @author Dmitriy Moroz
 */
internal object NoOpBenchmarkPlatformActions : BenchmarkPlatformActions {
    override suspend fun shareText(text: String): BenchmarkActionResult =
        BenchmarkActionResult.Unsupported
}
