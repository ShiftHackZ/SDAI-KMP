package com.shifthackz.aisdv1.presentation.app.di

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.validation.di.validatorsModule
import com.shifthackz.aisdv1.network.di.civitaiNetworkModule
import com.shifthackz.aisdv1.feature.civitai.config.BuildConfig as CivitaiBuildConfig
import com.shifthackz.aisdv1.data.di.civitaiDataModule
import com.shifthackz.aisdv1.feature.civitai.di.civitaiModule
import com.shifthackz.aisdv1.presentation.civitai.di.civitaiPresentationModule
import com.shifthackz.aisdv1.data.di.coreDataModule
import com.shifthackz.aisdv1.data.di.dataModule
import com.shifthackz.aisdv1.demo.di.demoModule
import com.shifthackz.aisdv1.domain.di.benchmarkUseCaseModule
import com.shifthackz.aisdv1.domain.di.coreDomainModule
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.feature.appupdate.di.appUpdateModule
import com.shifthackz.aisdv1.feature.auth.di.authModule
import com.shifthackz.aisdv1.feature.backup.di.backupModule
import com.shifthackz.aisdv1.feature.benchmark.di.benchmarkModule
import com.shifthackz.aisdv1.feature.bonsai.di.bonsaiModule
import com.shifthackz.aisdv1.feature.coreml.di.coreMlModule
import com.shifthackz.aisdv1.feature.mnn.di.mnnHostRuntimeModule
import com.shifthackz.aisdv1.network.di.coreNetworkModule
import com.shifthackz.aisdv1.network.di.networkModule
import com.shifthackz.aisdv1.presentation.appupdate.di.appUpdatePresentationModule
import com.shifthackz.aisdv1.work.di.backgroundWorkModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Shared implementation registrations; platform bindings are installed before resolution.
 *
 * @author Dmitriy Moroz
 */
internal val commonApplicationModules = listOf(
    appUpdateModule,
    appUpdatePresentationModule,
    coreNetworkModule,
    networkModule,
    validatorsModule,
    coreDomainModule,
    benchmarkUseCaseModule,
    coreDataModule,
    civitaiNetworkModule(
        CivitaiBuildConfig.CIVITAI_CLIENT_ID,
        CivitaiBuildConfig.CIVITAI_REDIRECT_URI
    ),
    civitaiDataModule,
    civitaiModule,
    civitaiPresentationModule,
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
 * @author Dmitriy Moroz
 */
internal fun initializeApplicationLanguage(koin: Koin) {
    val preferenceManager = koin.get<PreferenceManager>()
    Localization.setLanguageCode(preferenceManager.languageCode.takeIf(String::isNotBlank))
}
