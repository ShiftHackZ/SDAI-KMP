package com.shifthackz.aisdv1.feature.openaioauth.api

import com.shifthackz.aisdv1.feature.openaioauth.model.ImageRequest
import com.shifthackz.aisdv1.feature.openaioauth.model.StoredSession
import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies OAuth configuration and inference traffic uses the application counter bridge.
 * @author Dmitriy Moroz */
class OAuthTrafficTest {
    private val records = mutableListOf<Pair<NetworkUsageCategory, Long>>()

    @After
    fun cleanUp() { NetworkUsageCounter.recorder = null }

    @Test
    fun `image request and response count exact inference bytes`() = runBlocking {
        val response = """{"data":[{"b64_json":"AQID"}]}"""
        NetworkUsageCounter.recorder = { category, bytes -> records += category to bytes }
        HttpClient(MockEngine {
            respond(response, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }).use { client ->
            val session = StoredSession("unused-test-token", "unused-refresh", "test-account", "test-revision", Long.MAX_VALUE)
            val result = ImageApi(client, GenerationTimeoutProvider.Default).generate(session, "portrait", 512, 512)
            assertEquals("AQID", result)
            assertEquals(listOf(
                NetworkUsageCategory.INFERENCE to defaultNetworkJson.encodeToString(ImageRequest("portrait", "512x512")).encodeToByteArray().size.toLong(),
                NetworkUsageCategory.INFERENCE to response.encodeToByteArray().size.toLong(),
            ), records)
        }
    }

    @Test
    fun `failed device login still counts configuration request and response`() = runBlocking {
        val response = """{"detail":"unavailable"}"""
        NetworkUsageCounter.recorder = { category, bytes -> records += category to bytes }
        HttpClient(MockEngine {
            respond(response, status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }).use { client ->
            val outcome = runCatching { OAuthApi(client).requestCode() }
            assertEquals(true, outcome.isFailure)
            assertEquals(2, records.size)
            assertEquals(NetworkUsageCategory.CONFIGS, records.first().first)
            assertEquals(NetworkUsageCategory.CONFIGS to response.encodeToByteArray().size.toLong(), records.last())
        }
    }
}
