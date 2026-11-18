package com.shifthackz.aisdv1.presentation.benchmark.di

import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkPlatformActions
import com.shifthackz.aisdv1.presentation.benchmark.createDefaultBenchmarkPlatformActions
import org.koin.dsl.module


/**
 * Supplies a singleton platform-default benchmark sharing adapter; native bindings may override it.
 *
 * @author Dmitriy Moroz
 */
val benchmarkCoreModule = module {
    single<BenchmarkPlatformActions> { createDefaultBenchmarkPlatformActions() }
}
