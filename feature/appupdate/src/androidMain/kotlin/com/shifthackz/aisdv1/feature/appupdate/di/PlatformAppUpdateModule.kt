package com.shifthackz.aisdv1.feature.appupdate.di

import com.shifthackz.aisdv1.feature.appupdate.createAndroidAppUpdateGateway
import com.shifthackz.aisdv1.domain.gateway.AppUpdateGateway
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Binds only the gateway compiled into this Android distribution.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformAppUpdateModule(): Module = module {
    single<AppUpdateGateway> { createAndroidAppUpdateGateway() }
}
