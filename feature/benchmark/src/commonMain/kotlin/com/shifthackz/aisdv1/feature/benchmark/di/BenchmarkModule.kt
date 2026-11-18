package com.shifthackz.aisdv1.feature.benchmark.di

import com.shifthackz.aisdv1.domain.gateway.BenchmarkRunner
import com.shifthackz.aisdv1.feature.benchmark.BenchmarkManagerImpl
import com.shifthackz.aisdv1.domain.gateway.BenchmarkRepository
import com.shifthackz.aisdv1.feature.benchmark.BenchmarkScoreEngine
import com.shifthackz.aisdv1.feature.benchmark.RoomBenchmarkRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Exposes benchmark feature dependencies.
 *
 * @author Dmitriy Moroz
 */
val benchmarkModule: Module = module {
    includes(platformBenchmarkModule())
    single<BenchmarkRepository> { RoomBenchmarkRepository(dao = get()) }
    single { BenchmarkScoreEngine(timeProvider = get()) }
    single<BenchmarkRunner> {
        BenchmarkManagerImpl(
            deviceProbe = get(),
            scoreEngine = get(),
            repository = get(),
            preferenceManager = get(),
        )
    }

}

/**
 * Creates platform-specific benchmark bindings.
 *
 * @return platform Koin module.
 * @author Dmitriy Moroz
 */
internal expect fun platformBenchmarkModule(): Module
