package com.shifthackz.aisdv1.network.api.easydiffusion

import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.NetworkUsageCounter
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.request.EasyDiffusionRequest
import com.shifthackz.aisdv1.network.response.EasyDiffusionFrame
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Runs the actual Ktor adapter and DTO serialization against an isolated HTTP engine.
 * @author Dmitriy Moroz
 */
class KtorEasyDiffusionApiTest {
    private val clients = mutableListOf<HttpClient>()
    private val requests = mutableListOf<HttpRequestData>()
    private val bytes = mutableListOf<Pair<NetworkUsageCategory, Long>>()
    private val root = "https://server.example/proxy"
    private val request = EasyDiffusionRequest(
        "lake",
        "blur",
        512,
        512,
        4294967295L,
        20,
        7f,
        "nested/model",
        "session"
    )

    @After
    fun closeClients() {
        clients.forEach(HttpClient::close)
        NetworkUsageCounter.recorder = null
    }

    @Test
    fun `configuration and operation pings preserve prefix session and traffic category`() =
        runBlocking {
            NetworkUsageCounter.recorder = { category, count -> bytes += category to count }
            val body = """{"status":"Rendering"}"""
            val api = api(body)
            assertEquals("Rendering", api.ping(root, null).status)
            api.ping(root, "session")
            assertEquals("/proxy/ping", requests.first().url.encodedPath)
            assertNull(requests.first().url.parameters["session_id"])
            assertEquals("session", requests.last().url.parameters["session_id"])
            assertEquals(
                listOf(NetworkUsageCategory.CONFIGS, NetworkUsageCategory.INFERENCE),
                bytes.map { it.first })
            assertEquals(body.encodeToByteArray().size.toLong() * 2, bytes.sumOf { it.second })
            assertTrue(requests.all { it.headers["Authorization"] == null })
        }

    @Test
    fun `render serializes supported controls and explicit safe defaults exactly once`() =
        runBlocking {
            val api = api("""{"task":42,"stream":"/image/stream/42"}""")
            assertEquals(42L, api.render(root, request).task)
            val sent = requests.single()
            assertEquals("/proxy/render", sent.url.encodedPath)
            val json =
                defaultNetworkJson.parseToJsonElement((sent.body as TextContent).text).jsonObject
            mapOf(
                "seed" to "4294967295", "sampler_name" to "ddim", "output_format" to "png",
                "num_outputs" to "1", "stream_image_progress" to "false",
                "use_stable_diffusion_model" to "nested/model", "session_id" to "session"
            ).forEach { (key, value) ->
                assertEquals(value, json.getValue(key).jsonPrimitive.content)
            }
            mapOf(
                "seed" to JsonPrimitive(4294967295L),
                "num_outputs" to JsonPrimitive(1),
                "stream_image_progress" to JsonPrimitive(false),
                "show_only_filtered_image" to JsonPrimitive(true),
                "width" to JsonPrimitive(512),
                "height" to JsonPrimitive(512),
                "num_inference_steps" to JsonPrimitive(20),
                "guidance_scale" to JsonPrimitive(7f),
                "prompt" to JsonPrimitive("lake"),
                "negative_prompt" to JsonPrimitive("blur"),
            ).forEach { (key, value) -> assertEquals(value, json.getValue(key)) }
            assertEquals("[]", json.getValue("filters").toString())
            assertNull(json["save_to_disk_path"])
        }

    @Test
    fun `strict provider decoder preserves string and array checkpoint metadata`() = runBlocking {
        listOf(
            "\"folder/model\"" to listOf("folder/model"),
            "[\"folder/first\",\"folder/second\"]" to listOf("folder/first", "folder/second")
        ).forEach { (metadata, expected) ->
            val frames = mutableListOf<EasyDiffusionFrame>()
            val body = """{"task_data":{"use_stable_diffusion_model":$metadata}}"""
            api(body).stream(root, 42) { frames += it }
            assertEquals(expected, frames.single().taskData?.model)
        }
    }

    @Test
    fun `strict provider decoder rejects unquoted fields and numeric checkpoint metadata`() =
        runBlocking {
            listOf(
                """{step:1}""",
                """{"task_data":{"use_stable_diffusion_model":42}}"""
            ).forEach { body ->
                assertTrue(runCatching { api(body).stream(root, 42) {} }.isFailure)
            }
        }

    @Test
    fun `invalid or foreign submission identities are rejected`() = runBlocking {
        listOf(
            """{"task":0,"stream":"/image/stream/0"}""",
            """{"task":42,"stream":"https://other.example/image/stream/42"}""",
            """{"task":42,"stream":"/image/stream/43"}"""
        ).forEach { body ->
            assertTrue(runCatching {
                api(body).render(
                    root,
                    request
                )
            }.exceptionOrNull() is IllegalArgumentException)
        }
    }

    @Test
    fun `stream handles adjacent frames larger than a read buffer and counts UTF8 bytes once`() =
        runBlocking {
            NetworkUsageCounter.recorder = { category, count -> bytes += category to count }
            val body =
                """{"step":1,"total_steps":20,"unknown":"${"ліс".repeat(4000)}"}{"status":"succeeded","output":[]}"""
            val frames = mutableListOf<EasyDiffusionFrame>()
            api(body).stream(root, 42) { frames += it }
            assertEquals(listOf(null, "succeeded"), frames.map { it.status })
            assertEquals(1, frames.first().step)
            assertEquals("/proxy/image/stream/42", requests.single().url.encodedPath)
            assertEquals(body.encodeToByteArray().size.toLong(), bytes.sumOf { it.second })
            assertTrue(bytes.all { it.first == NetworkUsageCategory.INFERENCE })
        }

    @Test
    fun `malformed complete JSON and truncated stream never become success`() = runBlocking {
        assertTrue(runCatching {
            api("""{"step":oops}""").stream(
                root,
                42
            ) {}
        }.exceptionOrNull() is EasyDiffusionProtocolException)
        assertTrue(runCatching {
            api("""{"step":1""").stream(
                root,
                42
            ) {}
        }.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `busy and expired streams retain HTTP statuses without automatic retries`() = runBlocking {
        listOf(
            HttpStatusCode.TooEarly,
            HttpStatusCode.NotFound,
            HttpStatusCode.Gone,
            HttpStatusCode.ServiceUnavailable
        ).forEach { status ->
            val before = requests.size
            val failure =
                runCatching { api("untrusted body", status).stream(root, 42) {} }.exceptionOrNull()
            assertEquals(status.value, (failure as EasyDiffusionHttpException).statusCode)
            assertEquals(before + 1, requests.size)
        }
    }

    @Test
    fun `frame callback failure propagates without reading another connection`() = runBlocking {
        val failure = IllegalStateException("caller stopped")
        val actual = runCatching {
            api("""{"step":1}{"step":2}""").stream(
                root,
                42
            ) { throw failure }
        }.exceptionOrNull()
        assertSame(failure, actual)
        assertEquals(1, requests.size)
    }

    @Test
    fun `stop uses only a positive captured task and requires OK acknowledgement`() = runBlocking {
        api("""["OK"]""").stop(root, 42)
        assertEquals("/proxy/image/stop", requests.single().url.encodedPath)
        assertEquals("42", requests.single().url.parameters["task"])
        assertTrue(runCatching {
            api("""["NO"]""").stop(
                root,
                42
            )
        }.exceptionOrNull() is IllegalStateException)
        val count = requests.size
        assertTrue(runCatching {
            api("[]").stop(
                root,
                0
            )
        }.exceptionOrNull() is IllegalArgumentException)
        assertEquals(count, requests.size)
    }

    @Test
    fun `catalog and readiness decoding failures propagate safely`() = runBlocking {
        assertTrue(runCatching { api("<html>").models(root) }.exceptionOrNull() is EasyDiffusionProtocolException)
        assertEquals(emptyList<Any>(), api("""{"models":[]}""").models(root).models)
        assertEquals("/proxy/get/models", requests.last().url.encodedPath)
        val failure = runCatching {
            api("busy", HttpStatusCode.ServiceUnavailable).ping(
                root,
                null
            )
        }.exceptionOrNull()
        assertEquals(503, (failure as EasyDiffusionHttpException).statusCode)
    }

    /** Builds only the transport boundary; request serialization and stream processing remain production code.
     * @param body Controlled HTTP response body.
     * @param status Controlled HTTP status.
     * @author Dmitriy Moroz
     */
    private fun api(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK
    ): KtorEasyDiffusionApi {
        val client = HttpClient(MockEngine { requestData ->
            requests += requestData
            respond(body, status)
        })
        clients += client
        return KtorEasyDiffusionApi(
            client,
            GenerationTimeoutProvider { 120000 },
            Json(defaultNetworkJson) { isLenient = false }
        )
    }
}
