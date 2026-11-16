package com.shifthackz.aisdv1.network.api.easydiffusion

import com.shifthackz.aisdv1.network.client.GenerationTimeoutProvider
import com.shifthackz.aisdv1.network.client.NetworkUsageCategory
import com.shifthackz.aisdv1.network.client.applyGenerationTimeout
import com.shifthackz.aisdv1.network.client.boundedGet
import com.shifthackz.aisdv1.network.client.boundedPost
import com.shifthackz.aisdv1.network.client.recordUsageBytes
import com.shifthackz.aisdv1.network.client.setTrackedJsonBody
import com.shifthackz.aisdv1.network.client.trackUsage
import com.shifthackz.aisdv1.network.client.trackedJsonBody
import com.shifthackz.aisdv1.network.request.EasyDiffusionRequest
import com.shifthackz.aisdv1.network.response.EasyDiffusionFrame
import com.shifthackz.aisdv1.network.response.EasyDiffusionModels
import com.shifthackz.aisdv1.network.response.EasyDiffusionPing
import com.shifthackz.aisdv1.network.response.EasyDiffusionSubmission
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Anonymous, bounded v3 transport. Stream bodies bypass whole-response buffering and count each
 * received byte once; generation deadlines and reopen policy belong to the domain caller.
 * @param client Independent provider client with current connection limits and no authentication.
 * @param generationTimeoutProvider Live request and socket limit for generation-related calls.
 * @param json Strict provider decoder that rejects malformed JSON while tolerating unknown response fields.
 * @author Dmitriy Moroz
 */
internal class KtorEasyDiffusionApi(
    private val client: HttpClient,
    private val generationTimeoutProvider: GenerationTimeoutProvider,
    private val json: Json,
) : EasyDiffusionApi {
    /**
     * Reads strict readiness JSON, retaining HTTP failures for the caller's reachability policy.
     * @param baseUrl Captured anonymous server root or reverse-proxy prefix.
     * @param sessionId Operation session to renew, or null during candidate setup.
     * @author Dmitriy Moroz
     */
    override suspend fun ping(baseUrl: String, sessionId: String?): EasyDiffusionPing {
        val category =
            if (sessionId == null) NetworkUsageCategory.CONFIGS else NetworkUsageCategory.INFERENCE
        val response = client.boundedGet {
            endpoint(baseUrl, "ping")
            sessionId?.let { url.parameters.append("session_id", it) }
            trackUsage(category)
        }
        response.requireSuccess(category)
        return response.decodeTrackedBody(category)
    }

    /**
     * Downloads and counts the current catalog without using stale data after a failed response.
     * @param baseUrl Captured anonymous server root or reverse-proxy prefix.
     * @author Dmitriy Moroz
     */
    override suspend fun models(baseUrl: String): EasyDiffusionModels {
        val response = client.boundedGet {
            endpoint(baseUrl, "get", "models")
            trackUsage(NetworkUsageCategory.CONFIGS)
        }
        response.requireSuccess(NetworkUsageCategory.CONFIGS)
        return response.decodeTrackedBody(NetworkUsageCategory.CONFIGS)
    }

    /**
     * Submits one task once and validates its positive identity and expected relative stream route.
     * @param baseUrl Captured anonymous server root or reverse-proxy prefix.
     * @param request One supported image request with explicit output and sampler settings.
     * @author Dmitriy Moroz
     */
    override suspend fun render(
        baseUrl: String,
        request: EasyDiffusionRequest
    ): EasyDiffusionSubmission {
        val response = client.boundedPost {
            endpoint(baseUrl, "render")
            applyGenerationTimeout(generationTimeoutProvider)
            setTrackedJsonBody(NetworkUsageCategory.INFERENCE, request)
        }
        response.requireSuccess(NetworkUsageCategory.INFERENCE)
        return response.decodeTrackedBody<EasyDiffusionSubmission>(NetworkUsageCategory.INFERENCE)
            .also {
                require(it.task > 0 && it.stream == "/image/stream/${it.task}") { "Invalid Easy Diffusion task identity" }
            }
    }

    /**
     * Delivers complete strict JSON objects from one bounded stream connection and counts consumed bytes.
     * HTTP errors, malformed objects and truncated EOF propagate; reconnect policy remains in domain.
     * @param baseUrl Captured anonymous server root or reverse-proxy prefix.
     * @param task Positive task identity returned by the accepted render request.
     * @param onFrame Receives each decoded progress or terminal object before the next object is read.
     * @author Dmitriy Moroz
     */
    override suspend fun stream(
        baseUrl: String,
        task: Long,
        onFrame: suspend (EasyDiffusionFrame) -> Unit
    ) {
        require(task > 0) { "A task-specific stream is required" }
        client.prepareGet {
            endpoint(baseUrl, "image", "stream", task.toString())
            applyGenerationTimeout(generationTimeoutProvider)
            trackUsage(NetworkUsageCategory.INFERENCE)
        }.execute { response ->
            response.requireSuccess(NetworkUsageCategory.INFERENCE)
            val framer = EasyDiffusionJsonFramer()
            val bytes = ByteArray(8192)
            val channel = response.bodyAsChannel()
            while (true) {
                val count = channel.readAvailable(bytes)
                if (count < 0) break
                if (count == 0) continue
                recordUsageBytes(NetworkUsageCategory.INFERENCE, count.toLong())
                for (index in 0 until count) {
                    framer.accept(bytes[index])?.let { frame ->
                        val decoded = try {
                            json.decodeFromString<EasyDiffusionFrame>(frame)
                        } catch (failure: SerializationException) {
                            throw EasyDiffusionProtocolException(failure)
                        }
                        onFrame(decoded)
                    }
                }
            }
            framer.finish()
        }
    }

    /**
     * Stops only the supplied positive task and validates the server's OK acknowledgement.
     * @param baseUrl Captured server prefix from the accepted task, never current replacement settings.
     * @param task Positive accepted identity; zero or omitted identities are forbidden.
     * @author Dmitriy Moroz
     */
    override suspend fun stop(baseUrl: String, task: Long) {
        require(task > 0) { "A task-specific stop is required" }
        val response = client.boundedGet {
            endpoint(baseUrl, "image", "stop")
            url.parameters.append("task", task.toString())
            trackUsage(NetworkUsageCategory.INFERENCE)
        }
        response.requireSuccess(NetworkUsageCategory.INFERENCE)
        check(
            "OK" in response.decodeTrackedBody<List<String>>(NetworkUsageCategory.INFERENCE)
        ) { "Invalid stop response" }
    }

    /**
     * Counts and decodes one bounded response, retaining malformed JSON as a typed safe failure.
     * @param category Traffic bucket receiving the actual consumed response bytes.
     * @author Dmitriy Moroz
     */
    private suspend inline fun <reified T> HttpResponse.decodeTrackedBody(category: NetworkUsageCategory): T =
        try {
            trackedJsonBody<T>(category, json)
        } catch (failure: SerializationException) {
            throw EasyDiffusionProtocolException(failure)
        }

    /** Builds only known relative routes under the captured prefix, ignoring remote redirect paths.
     * @param baseUrl Validated root or reverse-proxy prefix.
     * @param segments Known protocol route segments.
     * @author Dmitriy Moroz */
    private fun HttpRequestBuilder.endpoint(baseUrl: String, vararg segments: String) {
        expectSuccess = false
        url.takeFrom(baseUrl.trimEnd('/'))
        url.appendPathSegments(*segments)
    }

    /** Retains HTTP status and counts consumed error bytes without retaining arbitrary error bodies.
     * @param category Traffic bucket for this call.
     * @author Dmitriy Moroz */
    private suspend fun HttpResponse.requireSuccess(category: NetworkUsageCategory) {
        if (status.value !in 200..299) {
            // Buffered metadata calls contain at most 1 MiB. Direct stream failures use the same
            // cap; count every consumed chunk once, then let the enclosing call close the body.
            val channel = bodyAsChannel()
            val bytes = ByteArray(8192)
            var consumed = 0
            while (consumed < 1024 * 1024) {
                val count =
                    channel.readAvailable(bytes, 0, minOf(bytes.size, 1024 * 1024 - consumed))
                if (count < 0) break
                if (count == 0) continue
                consumed += count
                recordUsageBytes(category, count.toLong())
            }
            throw EasyDiffusionHttpException(status.value)
        }
    }
}
