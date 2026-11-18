package com.shifthackz.aisdv1.presentation.benchmark

/**
 * Creates fallback benchmark platform actions for Android previews.
 *
 * @return no-op platform actions.
 * @author Dmitriy Moroz
 */
actual fun createDefaultBenchmarkPlatformActions(): BenchmarkPlatformActions =
    NoOpBenchmarkPlatformActions
