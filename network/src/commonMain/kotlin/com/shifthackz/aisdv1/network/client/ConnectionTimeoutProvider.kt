package com.shifthackz.aisdv1.network.client

import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.timeout

/**
 * Reads the current provider connection limit without coupling HTTP clients to preferences.
 *
 * @author Dmitriy Moroz
 */
fun interface ConnectionTimeoutProvider {
    /**
     * Returns connection time in milliseconds; zero means no limit.
     *
     * @author Dmitriy Moroz
     */
    fun timeoutMillis(): Long

    companion object {
        /**
         * Original 120-second connection limit for clients outside the application graph.
         *
         * @author Dmitriy Moroz
         */
        val Default = ConnectionTimeoutProvider { 120_000L }
    }
}

/**
 * Applies the selected connection limit to setup and metadata requests before HttpTimeout runs.
 * Explicit request/socket limits, including generation overrides, remain independent.
 * Darwin enforces the request deadline while managing low-level connection timing itself.
 *
 * @param provider Live preference reader, evaluated for each new request.
 * @author Dmitriy Moroz
 */
internal fun connectionTimeoutPlugin(provider: ConnectionTimeoutProvider) =
    createClientPlugin("ProviderConnectionTimeout") {
        onRequest { request, _ ->
            val limit = provider.timeoutMillis().takeIf { it > 0L }
                ?: HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            val existing = request.getCapabilityOrNull(HttpTimeoutCapability)
            if (existing != null) {
                existing.connectTimeoutMillis = limit
                if (existing.requestTimeoutMillis == null) existing.requestTimeoutMillis = limit
                if (existing.socketTimeoutMillis == null) existing.socketTimeoutMillis = limit
            } else {
                request.timeout {
                    connectTimeoutMillis = limit
                    requestTimeoutMillis = limit
                    socketTimeoutMillis = limit
                }
            }
        }
    }
