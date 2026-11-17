package com.shifthackz.aisdv1.feature.mnn.di

import com.shifthackz.aisdv1.domain.gateway.MnnArchiveExtractor
import com.shifthackz.aisdv1.domain.gateway.MnnBundleFileSystem
import com.shifthackz.aisdv1.feature.mnn.MnnAppleBridge
import org.koin.core.Koin
import org.koin.dsl.module

/**
 * Installs explicit host collaborators before UI dependencies are resolved.
 *
 * @param koin Application composition root returned by initialization.
 * @param runtime Serial native execution adapter.
 * @param fileSystem Regular-file and streamed digest adapter.
 * @param extractor Strict root-layout archive extraction adapter.
 *
 * @author Dmitriy Moroz
 */
fun installMnnHost(
    koin: Koin, runtime: MnnAppleBridge,
    fileSystem: MnnBundleFileSystem, extractor: MnnArchiveExtractor
) {
    koin.loadModules(listOf(module {
        single<MnnAppleBridge> { runtime }
        single<MnnBundleFileSystem> { fileSystem }
        single<MnnArchiveExtractor> { extractor }
    }))
}
