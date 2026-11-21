package com.shifthackz.aisdv1.network.api.swarmui

import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.defaultNetworkJson
import com.shifthackz.aisdv1.network.request.SwarmUiGenerationRequest
import io.ktor.client.plugins.websocket.WebSockets as ClientWebSockets
import io.ktor.http.HttpHeaders
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.WebSockets as ServerWebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send

/**
 * Exercises the production WebSocket transport against Ktor's in-memory server engine.
 *
 * @param messages Ordered upstream frames, including terminal and postprocessing messages.
 * @param shouldHoldOpen Whether the server stays active until caller cancellation or deadline.
 * @param timeoutMillis Generation deadline; zero disables it.
 * @author Dmitriy Moroz
 */
internal class SwarmUiProgressTransportFixture(
    private val messages: List<String>,
    private val shouldHoldOpen: Boolean = false,
    private val timeoutMillis: Long = 0,
) {
    var requestCount = 0
        private set
    var receivedRequest: SwarmUiGenerationRequest? = null
        private set
    var authorization: String? = null
        private set

    /**
     * Starts one isolated server and passes its production client to the scenario.
     *
     * @param block Scenario operation and observable assertions.
     * @author Dmitriy Moroz
     */
    fun run(block: suspend (KtorSwarmUiGenerationApi) -> Unit) = testApplication {
        application {
            install(ServerWebSockets)
            routing {
                webSocket("/proxy/API/GenerateText2ImageWS") {
                    requestCount++
                    authorization = call.request.headers[HttpHeaders.Authorization]
                    val frame = incoming.receive() as Frame.Text
                    receivedRequest =
                        defaultNetworkJson.decodeFromString<SwarmUiGenerationRequest>(frame.readText())
                    messages.forEach { send(it) }
                    if (shouldHoldOpen) incoming.receiveCatching()
                }
            }
        }
        val client = createClient { install(ClientWebSockets) }
        block(KtorSwarmUiGenerationApi(client, GenerationTimeoutProvider { timeoutMillis }))
    }

    companion object {
        const val BASE_URL = "http://localhost/proxy"
        val REQUEST = SwarmUiGenerationRequest(
            sessionId = "test-session", model = "sd15", initImage = null,
            initImageCreativity = null, images = 1, prompt = "a cat", negativePrompt = "",
            width = 512, height = 512, seed = "42", variationSeed = null,
            variationSeedStrength = null, cfgScale = 7f, steps = 20,
        )
        const val IMAGE = """{"image":"View/result.png","batch_index":"0"}"""
        const val COMPLETE = """{"socket_intention":"close"}"""
    }
}
