package com.shifthackz.aisdv1.network.client

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.ByteReadChannel

/**
 * Holds only an already bounded response and preserves normal Ktor typed decoding/error access.
 * Uses the same detached-body/context convention as Ktor 3.5 SavedHttpCall: response cleanup
 * completes the network job; later body decoding reads only a fresh in-memory channel.
 *
 * @param client Original configured provider client.
 * @param origin Streamed response metadata.
 * @param bytes Bounded payload detached before streaming cleanup.
 * @author Dmitriy Moroz
 */
@OptIn(InternalAPI::class)
internal class BoundedMemoryCall(client: HttpClient, origin: HttpResponse, bytes: ByteArray) :
    HttpClientCall(client) {
    override val allowDoubleReceive = true

    init {
        request = origin.call.request
        response = object : HttpResponse() {
            override val call: HttpClientCall get() = this@BoundedMemoryCall
            override val status = origin.status
            override val version = origin.version
            override val requestTime = origin.requestTime
            override val responseTime = origin.responseTime
            override val headers = origin.headers
            override val coroutineContext = origin.coroutineContext
            override val rawContent: ByteReadChannel get() = ByteReadChannel(bytes)
        }
    }
}
