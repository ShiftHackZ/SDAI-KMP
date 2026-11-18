package com.shifthackz.aisdv1.presentation.benchmark.di

import com.shifthackz.aisdv1.presentation.benchmark.AndroidBenchmarkPlatformActions
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkPlatformActions
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module


/**
 * Supplies an Android benchmark-sharing adapter factory bound to the native context and UI dispatcher.
 *
 * @author Dmitriy Moroz
 */
val benchmarkUiUtilsModule = module {
    factory {
        AndroidBenchmarkPlatformActions(
            androidContext(),
            get()
        )
    } bind BenchmarkPlatformActions::class
}
