package com.shifthackz.aisdv1.domain.di

import com.shifthackz.aisdv1.domain.gateway.LocalGenerationBenchmarkGate
import com.shifthackz.aisdv1.domain.policy.benchmark.LocalGenerationBenchmarkGateImpl
import com.shifthackz.aisdv1.domain.usecase.benchmark.EvaluateGenerationBenchmarkUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.EvaluateGenerationBenchmarkUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.benchmark.InspectBenchmarkDeviceUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.InspectBenchmarkDeviceUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.benchmark.ObserveLatestBenchmarkUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.ObserveLatestBenchmarkUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.benchmark.RecordBenchmarkPromptAnswerUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.RecordBenchmarkPromptAnswerUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.benchmark.RunBenchmarkUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.RunBenchmarkUseCaseImpl
import com.shifthackz.aisdv1.domain.usecase.benchmark.SuppressBenchmarkWarningsUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.SuppressBenchmarkWarningsUseCaseImpl
import org.koin.dsl.module

/**
 * Binds benchmark business operations independently of runtime/storage implementations.
 *
 * @author Dmitriy Moroz
 */
val benchmarkUseCaseModule = module {
    single<LocalGenerationBenchmarkGate> { LocalGenerationBenchmarkGateImpl(get(), get()) }
    factory<ObserveLatestBenchmarkUseCase> { ObserveLatestBenchmarkUseCaseImpl(get()) }
    factory<InspectBenchmarkDeviceUseCase> { InspectBenchmarkDeviceUseCaseImpl(get()) }
    factory<RunBenchmarkUseCase> { RunBenchmarkUseCaseImpl(get()) }
    factory<EvaluateGenerationBenchmarkUseCase> { EvaluateGenerationBenchmarkUseCaseImpl(get()) }
    factory<RecordBenchmarkPromptAnswerUseCase> { RecordBenchmarkPromptAnswerUseCaseImpl(get()) }
    factory<SuppressBenchmarkWarningsUseCase> { SuppressBenchmarkWarningsUseCaseImpl(get()) }
}
