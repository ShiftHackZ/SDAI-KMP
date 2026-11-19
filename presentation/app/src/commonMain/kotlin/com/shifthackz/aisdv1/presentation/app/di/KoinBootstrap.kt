package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.feature.mnn.di.mnnHostRuntimeModule
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.data.di.coreDataModule
import com.shifthackz.aisdv1.data.di.dataModule
import com.shifthackz.aisdv1.demo.di.demoModule
import com.shifthackz.aisdv1.domain.di.coreDomainModule
import com.shifthackz.aisdv1.domain.di.benchmarkUseCaseModule
import com.shifthackz.aisdv1.feature.auth.di.authModule
import com.shifthackz.aisdv1.feature.backup.di.backupModule
import com.shifthackz.aisdv1.feature.benchmark.di.benchmarkModule
import com.shifthackz.aisdv1.feature.bonsai.di.bonsaiModule
import com.shifthackz.aisdv1.feature.coreml.di.coreMlModule
import com.shifthackz.aisdv1.network.di.coreNetworkModule
import com.shifthackz.aisdv1.network.di.networkModule
import com.shifthackz.aisdv1.work.di.backgroundWorkModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Shared implementation registrations; platform bindings are installed before resolution.
 *
 * @author Dmitriy Moroz
 */
internal val commonApplicationModules = listOf(
    coreNetworkModule,
    networkModule,
    validatorsModule,
    coreDomainModule,
    benchmarkUseCaseModule,
    coreDataModule,
    *dataModule,
    demoModule,
    authModule,
    backupModule,
    benchmarkModule,
    bonsaiModule,
    coreMlModule,
    mnnHostRuntimeModule,
    backgroundWorkModule,
    corePresentationModule,
)

/**
 * Initializes the iOS graph before runtime registration and Compose rendering.
 *
 * @return Existing or newly started application graph.
 *
 * @author Dmitriy Moroz
 */
fun initKoin(): Koin = (KoinPlatform.getKoinOrNull() ?: startKoin {
    modules(commonApplicationModules)
    modules(platformPresentationModule)
}.koin).also(::initializeApplicationLanguage)

/**
 * Applies the persisted locale before the first UI composition reads localized strings.
 *
 * @param koin Initialized host graph that owns the current preference store.
 *
 * @author Dmitriy Moroz
 */
internal fun initializeApplicationLanguage(koin: Koin) {
    val preferenceManager = koin.get<PreferenceManager>()
    Localization.setLanguageCode(preferenceManager.languageCode.takeIf(String::isNotBlank))
}
