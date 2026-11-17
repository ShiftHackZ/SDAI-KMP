package com.shifthackz.aisdv1.feature.mnn.di

import com.shifthackz.aisdv1.feature.mnn.MnnAppleBridge
import com.shifthackz.aisdv1.feature.mnn.UnavailableMnnAppleBridge
import com.shifthackz.aisdv1.feature.mnn.MnnRuntime
import com.shifthackz.aisdv1.feature.mnn.MnnRuntimeImpl
import org.koin.dsl.module

/**
 * Uses the explicitly injected Apple host bridge.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformMnnHostModule() = platformMnnModule()

/**
 * Binds asynchronous in-process Apple execution without loading Android native code.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformMnnModule() = module {
    single<MnnAppleBridge> { UnavailableMnnAppleBridge() }
    single<MnnRuntime> { MnnRuntimeImpl(get()) }
}
