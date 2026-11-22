package com.shifthackz.aisdv1.feature.civitai.di

import com.shifthackz.aisdv1.domain.gateway.CivitaiOAuthBrowser
import com.shifthackz.aisdv1.feature.civitai.IosCivitaiOAuthBrowser
import com.shifthackz.aisdv1.feature.civitai.IosCivitaiOAuthPresentationContext
import org.koin.dsl.module

/**
 * Supplies a retained iOS authentication-session owner through its common contract.
 *
 * @author Dmitriy Moroz
 */
internal actual val civitaiPlatformModule = module {
    single<CivitaiOAuthBrowser> {
        IosCivitaiOAuthBrowser(IosCivitaiOAuthPresentationContext())
    }
}
