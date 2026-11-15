package com.shifthackz.aisdv1.network.client

import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.http.takeFrom
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

/**
 * Executes a GET through a bounded streaming boundary before Ktor can save a whole response.
 *
 * @param block Existing typed provider request configuration.
 * @author Dmitriy Moroz
 */
suspend fun HttpClient.boundedGet(block: HttpRequestBuilder.() -> Unit): HttpResponse =
    boundedRequest(HttpMethod.Get, block)

/**
 * Executes an absolute image URL through the same bounded boundary.
 *
 * @param url Validated absolute image URL.
 * @param block Existing timeout/security configuration.
 * @author Dmitriy Moroz
 */
suspend fun HttpClient.boundedGet(url: Url, block: HttpRequestBuilder.() -> Unit): HttpResponse =
    boundedRequest(HttpMethod.Get) { this.url.takeFrom(url); block() }

/**
 * Executes a POST without changing its existing typed request/response contract.
 *
 * @param block Existing provider payload, authorization and timeout configuration.
 * @author Dmitriy Moroz
 */
suspend fun HttpClient.boundedPost(block: HttpRequestBuilder.() -> Unit): HttpResponse =
    boundedRequest(HttpMethod.Post, block)

@OptIn(InternalAPI::class)
private suspend fun HttpClient.boundedRequest(
    method: HttpMethod,
    block: HttpRequestBuilder.() -> Unit
): HttpResponse {
    var shouldValidate = true
    return prepareRequest {
        this.method = method
        block()
        shouldValidate = expectSuccess
        expectSuccess = false
    }.execute { response ->
        val isSuccess = response.status.value < 300
        val category = response.call.request.attributes.getOrNull(NetworkUsageCategoryAttribute)
        val limit = when {
            !isSuccess -> 1024 * 1024
            category == NetworkUsageCategory.CONFIGS -> 4 * 1024 * 1024
            else -> 64 * 1024 * 1024
        }
        if ((response.contentLength() ?: 0L) > limit) throw ResponseSizeLimitException()
        val bytes = response.rawContent.readRemaining(limit.toLong() + 1).readByteArray()
        if (bytes.size > limit) throw ResponseSizeLimitException()
        val saved = BoundedMemoryCall(this, response, bytes).response
        if (shouldValidate && !isSuccess) {
            val text = bytes.decodeToString()
            throw when (response.status.value) {
                in 300..399 -> RedirectResponseException(saved, text)
                in 400..499 -> ClientRequestException(saved, text)
                in 500..599 -> ServerResponseException(saved, text)
                else -> ResponseException(saved, text)
            }
        }
        saved
    }
}
