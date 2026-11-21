package com.shifthackz.aisdv1.network.client

import io.ktor.client.plugins.websocket.WebSockets

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Selects the native HTTP engine for the current platform.
 *
 * @return Result produced by `platformHttpClientEngine`.
 * @author Dmitriy Moroz
 */
expect fun platformHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig>

/**
 * Creates the SDAI value produced by `createConfiguredHttpClient`.
 *
 * @param json json value consumed by the API.
 * @param installContentNegotiation install content negotiation value consumed by the API.
 * @param connectionTimeoutProvider Optional live connection limit for AI provider clients.
 * @param configure configure value consumed by the API.
 * @author Dmitriy Moroz
 */
fun createConfiguredHttpClient(
    json: Json = defaultNetworkJson,
    installContentNegotiation: Boolean = true,
    connectionTimeoutProvider: ConnectionTimeoutProvider? = null,
    configure: HttpClientConfig<HttpClientEngineConfig>.() -> Unit = {},
): HttpClient = HttpClient(platformHttpClientEngine()) {
    expectSuccess = true
    install(WebSockets) { maxFrameSize = 64L * 1024 * 1024 }

    if (installContentNegotiation) {
        install(ContentNegotiation) {
            json(json)
            json(json, contentType = ContentType.Application.OctetStream)
            json(json, contentType = ContentType.Text.Plain)
        }
    }

    install(HttpTimeout) {
        connectTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
        requestTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
        socketTimeoutMillis = DEFAULT_TIMEOUT_MILLIS
    }

    install(Logging) {
        level = LogLevel.INFO
    }

    connectionTimeoutProvider?.let { install(connectionTimeoutPlugin(it)) }

    configure()
}

/**
 * Lenient provider JSON codec that ignores unsupported response fields.
 *
 * @author Dmitriy Moroz
 */
val defaultNetworkJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}

/**
 * Default two-minute transport waiting limit in milliseconds.
 *
 * @author Dmitriy Moroz
 */
private const val DEFAULT_TIMEOUT_MILLIS = 120_000L
