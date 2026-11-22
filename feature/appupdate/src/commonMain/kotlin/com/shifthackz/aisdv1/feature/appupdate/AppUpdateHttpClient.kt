package com.shifthackz.aisdv1.feature.appupdate

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout

/**
 * Applies short discovery timeouts independently of long-running generation requests.
 *
 * @author Dmitriy Moroz
 */
internal fun HttpClientConfig<*>.configureAppUpdateClient() {
    expectSuccess = true
    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        requestTimeoutMillis = 8_000
        socketTimeoutMillis = 8_000
    }
}
