package com.shifthackz.aisdv1.presentation.benchmark.di

import com.shifthackz.aisdv1.core.navigation.router.BenchmarkRouter
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkViewModel
import org.koin.dsl.module


/**
 * Creates benchmark state holders using separate domain inspect/observe/run operations and a caller-owned router.
 *
 * @author Dmitriy Moroz
 */
val benchmarkViewModelModule = module {
    factory { (router: BenchmarkRouter) ->
        BenchmarkViewModel(
            diagnostics = get(),
            dispatchersProvider = get(),
            observeLatestBenchmarkUseCase = get(),
            inspectBenchmarkDeviceUseCase = get(),
            runBenchmarkUseCase = get(),
            router = router,
            platformActions = get(),
        )
    }
}
