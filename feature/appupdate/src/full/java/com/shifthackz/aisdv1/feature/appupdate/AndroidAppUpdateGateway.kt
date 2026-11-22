package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.AppUpdateGateway
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.scope.Scope

/**
 * Creates the gateway owned by this distribution; FOSS never contacts update services.
 *
 * @author Dmitriy Moroz
 */
internal fun Scope.createAndroidAppUpdateGateway(): AppUpdateGateway =
    ReleaseAppUpdateGateway(HttpClient(OkHttp) { configureAppUpdateClient() }, get(), false)
