package com.shifthackz.aisdv1.feature.appupdate.di

import com.shifthackz.aisdv1.feature.appupdate.ReleaseAppUpdateGateway
import com.shifthackz.aisdv1.feature.appupdate.configureAppUpdateClient
import com.shifthackz.aisdv1.feature.appupdate.domain.AppUpdateGateway
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Uses public App Store discovery; installation remains owned by the App Store.
 *
 * @author Dmitriy Moroz
 */
internal actual fun platformAppUpdateModule(): Module = module {
    single<AppUpdateGateway> {
        ReleaseAppUpdateGateway(HttpClient(Darwin) { configureAppUpdateClient() }, get(), true)
    }
}
