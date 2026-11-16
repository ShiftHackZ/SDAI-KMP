package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.datasource.EasyDiffusionRemoteDataSource
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionHttpFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProtocolFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask
import com.shifthackz.aisdv1.network.api.easydiffusion.EasyDiffusionApi
import com.shifthackz.aisdv1.network.api.easydiffusion.EasyDiffusionHttpException
import com.shifthackz.aisdv1.network.api.easydiffusion.EasyDiffusionProtocolException
import com.shifthackz.aisdv1.network.request.EasyDiffusionRequest as RenderRequest

/**
 * Translates typed protocol data and safe statuses into domain entities without suppressing failures.
 * @param api Anonymous v3 transport with bounded bodies and incremental JSON framing.
 * @author Dmitriy Moroz
 */
internal class KtorEasyDiffusionRemoteDataSource(private val api: EasyDiffusionApi) :
    EasyDiffusionRemoteDataSource {
    /**
     * Returns server readiness without deciding which lifecycle states a caller should accept.
     * @param url Captured anonymous server prefix.
     * @param sessionId Operation session to renew, or null for setup probes.
     * @author Dmitriy Moroz
     */
    override suspend fun ping(url: String, sessionId: String?): String =
        transport { api.ping(url, sessionId).status }

    /**
     * Maps only checkpoint catalog entries, preserving full folder identifiers and propagating fetch errors.
     * @param url Captured server prefix whose catalog is being requested.
     * @author Dmitriy Moroz
     */
    override suspend fun models(url: String): List<EasyDiffusionModel> =
        transport { api.models(url) }.models
            .filter { "stable-diffusion" in it.tags && it.model.isNotBlank() }
            .map { EasyDiffusionModel(it.model, it.name.ifBlank { it.model }) }
            .distinctBy { it.id }

    /**
     * Maps one immutable domain request and returns its accepted endpoint/session/task ownership.
     * @param request Supported image inputs and connection identity captured before dispatch.
     * @author Dmitriy Moroz
     */
    override suspend fun submit(request: EasyDiffusionRequest): EasyDiffusionTask = transport {
        val body = RenderRequest(
            prompt = request.prompt,
            negativePrompt = request.negativePrompt,
            width = request.width,
            height = request.height,
            seed = request.seed,
            steps = request.steps,
            cfgScale = request.cfgScale,
            model = request.model,
            sessionId = request.sessionId,
            initImage = request.inputImage,
            strength = request.strength,
        )
        EasyDiffusionTask(request.url, request.sessionId, api.render(request.url, body).task)
    }

    /**
     * Maps progress and exactly one successful output without treating previews as final images.
     * @param task Captured endpoint and positive task identity used for this connection.
     * @param onFrame Receives domain progress or terminal data; callback failures propagate unchanged.
     * @author Dmitriy Moroz
     */
    override suspend fun readStream(
        task: EasyDiffusionTask,
        onFrame: suspend (EasyDiffusionFrame) -> Unit
    ) = transport {
        api.stream(task.url, task.id) { frame ->
            val image = if (frame.status == "succeeded") {
                check(frame.output.size == 1) { "Easy Diffusion returned an invalid image count" }
                frame.output.single()
            } else null
            onFrame(
                EasyDiffusionFrame(
                    frame.status,
                    frame.step,
                    frame.totalSteps,
                    image?.data,
                    image?.seed,
                    frame.detail,
                    frame.taskData?.model?.joinToString(", ")
                )
            )
        }
    }

    /**
     * Requests a task-specific stop and preserves transport/HTTP failures for domain cleanup policy.
     * @param task Accepted ownership whose endpoint and task identity must remain unchanged.
     * @author Dmitriy Moroz
     */
    override suspend fun stop(task: EasyDiffusionTask) = transport { api.stop(task.url, task.id) }

    /** Converts safe status types without hiding any transport, parsing, cancellation or storage failure.
     * @param block Strict remote call whose result should be propagated.
     * @author Dmitriy Moroz */
    private suspend fun <T> transport(block: suspend () -> T): T = try {
        block()
    } catch (failure: EasyDiffusionHttpException) {
        throw EasyDiffusionHttpFailure(failure.statusCode)
    } catch (failure: EasyDiffusionProtocolException) {
        throw EasyDiffusionProtocolFailure(failure)
    }
}
