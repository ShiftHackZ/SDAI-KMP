package com.shifthackz.aisdv1.network.di

import com.shifthackz.aisdv1.network.api.civitai.CivitaiApi
import com.shifthackz.aisdv1.network.api.civitai.KtorCivitaiApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import org.koin.dsl.module

/**
 * Supplies isolated Civitai HTTP operations with no provider credentials, body logger or retries.
 *
 * @param clientId Registered public mobile client ID supplied by build configuration.
 * @param redirectUri Exact registered native callback URI.
 * @author Dmitriy Moroz
 */
fun civitaiNetworkModule(clientId: String, redirectUri: String) = module {
    single<CivitaiApi> {
        KtorCivitaiApi(HttpClient {
            followRedirects = false
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 20_000
                socketTimeoutMillis =
                    120_000
            }
        }, clientId, redirectUri)
    }
}
