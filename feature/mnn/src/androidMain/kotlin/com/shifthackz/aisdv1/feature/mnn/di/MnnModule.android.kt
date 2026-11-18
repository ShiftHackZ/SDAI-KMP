package com.shifthackz.aisdv1.feature.mnn.di

import android.content.Context
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.feature.mnn.MnnInferenceEngine
import com.shifthackz.aisdv1.feature.mnn.MnnCacheOwner
import com.shifthackz.aisdv1.feature.mnn.MnnCacheOwnerImpl
import com.shifthackz.aisdv1.feature.mnn.MnnCacheCleaner
import com.shifthackz.aisdv1.feature.mnn.MnnCacheCleanerImpl
import com.shifthackz.aisdv1.feature.mnn.MnnRuntime
import com.shifthackz.aisdv1.feature.mnn.MnnNativeBridgeImpl
import com.shifthackz.aisdv1.feature.mnn.MnnModelLeaseOwner
import com.shifthackz.aisdv1.feature.mnn.MnnModelLeaseOwnerImpl
import org.koin.dsl.module
import java.io.File

/** Keeps Android host composition free of child native dependencies.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformMnnHostModule() = module {}

/**
 * Creates the child-only bindings for one fresh MNN attempt.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformMnnModule() = module {
    factory<MnnCacheCleaner> { MnnCacheCleanerImpl() }
    factory<MnnCacheOwner> { MnnCacheOwnerImpl(get(), get()) }
    factory<MnnModelLeaseOwner> { MnnModelLeaseOwnerImpl(File(get<Context>().filesDir, "model")) }
    factory<MnnRuntime> { MnnNativeBridgeImpl(get(), get()) }
    factory<NativeInferenceEngine> { MnnInferenceEngine(get()) }
}
