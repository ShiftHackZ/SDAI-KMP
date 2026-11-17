package com.shifthackz.aisdv1.feature.mnn.di

import com.shifthackz.aisdv1.feature.mnn.MnnRuntime
import com.shifthackz.aisdv1.feature.mnn.NoOpMnnRuntime
import org.koin.dsl.module

/**
 * Binds the unsupported execution boundary without loading or linking Android native code.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformMnnModule() = module {
    factory<MnnRuntime> { NoOpMnnRuntime() }
}
