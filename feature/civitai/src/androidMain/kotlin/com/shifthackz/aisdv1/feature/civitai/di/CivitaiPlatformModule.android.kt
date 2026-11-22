package com.shifthackz.aisdv1.feature.civitai.di

import com.shifthackz.aisdv1.domain.gateway.CivitaiOAuthBrowser
import com.shifthackz.aisdv1.feature.civitai.AndroidCivitaiOAuthBrowser
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android framework entry and interface share one pending authorization owner.
 *
 * @author Dmitriy Moroz
 */
internal actual val civitaiPlatformModule = module {
    single { AndroidCivitaiOAuthBrowser(androidContext()) }
    single<CivitaiOAuthBrowser> { get<AndroidCivitaiOAuthBrowser>() }
}
