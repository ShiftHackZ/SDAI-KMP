package com.shifthackz.aisdv1.network.api.openaicompatible

import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.request.OpenAiCompatibleRequest
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/** Verifies byte accounting at each compatible-provider transport boundary.
 * @author Dmitriy Moroz */
class OpenAiCompatibleTrafficTest {
    private val records = mutableListOf<Pair<NetworkUsageCategory, Long>>()

    @After
    fun cleanUp() { NetworkUsageCounter.recorder = null }

    @Test
    fun `discovery JSON generation and image downloads reach the shared counters`() = runBlocking {
        val discovery = """{"data":[]}"""
        val generated = """{"data":[{"url":"http://images/image.png"}]}"""
        val image = byteArrayOf(1, 2, 3, 4)
        val engine = MockEngine { request ->
            val headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            when (request.url.encodedPath) {
                "/v1/models" -> respond(discovery, headers = headers)
                "/v1/images/generations" -> respond(generated, headers = headers)
                else -> respond(image)
            }
        }
        NetworkUsageCounter.recorder = { category, bytes -> records += category to bytes }
        HttpClient(engine).use { client ->
            val api = KtorOpenAiCompatibleApi(client, client, GenerationTimeoutProvider.Default)
            api.probe("http://api/v1", "")
            val request = OpenAiCompatibleRequest("gpt-image-2", "portrait", "512x512")
            api.generate("http://api/v1", "", request)
            api.downloadImage("http://images/image.png")

            assertEquals(listOf(
                NetworkUsageCategory.CONFIGS to discovery.encodeToByteArray().size.toLong(),
                NetworkUsageCategory.INFERENCE to defaultNetworkJson.encodeToString(request).encodeToByteArray().size.toLong(),
                NetworkUsageCategory.INFERENCE to generated.encodeToByteArray().size.toLong(),
                NetworkUsageCategory.INFERENCE to image.size.toLong(),
            ), records)
        }
    }
}
