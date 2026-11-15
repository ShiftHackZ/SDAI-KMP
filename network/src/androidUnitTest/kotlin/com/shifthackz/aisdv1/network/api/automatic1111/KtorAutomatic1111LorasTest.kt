package com.shifthackz.aisdv1.network.api.automatic1111

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Verifies large A1111 LoRA responses through the production HTTP decoder.
 *
 * @author Dmitriy Moroz
 */
class KtorAutomatic1111LorasTest {

    @Test
    fun `2500 loras with metadata decode without losing names aliases or paths`() = runBlocking {
        val response = (0 until 2_500).joinToString(prefix = "[", postfix = "]") { index ->
            """{"name":"lora_$index","alias":"alias_$index","path":"models/Lora/$index.safetensors","metadata":{"ss_tag_frequency":{"set":{"portrait":100}},"ss_output_name":"lora_$index"}}"""
        }
        val engine = MockEngine { request ->
            assertEquals("/sdapi/v1/loras", request.url.encodedPath)
            respond(response, headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        HttpClient(engine).use { client ->
            val actual = KtorAutomatic1111MetadataApi(client).fetchLoras("http://localhost:7860", null)

            assertEquals(2_500, actual.size)
            actual.forEachIndexed { index, lora ->
                assertEquals("lora_$index", lora.name)
                assertEquals("alias_$index", lora.alias)
                assertEquals("models/Lora/$index.safetensors", lora.path)
            }
        }
    }
}
