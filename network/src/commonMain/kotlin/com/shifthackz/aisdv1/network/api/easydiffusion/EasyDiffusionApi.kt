package com.shifthackz.aisdv1.network.api.easydiffusion

import com.shifthackz.aisdv1.network.request.EasyDiffusionRequest
import com.shifthackz.aisdv1.network.response.EasyDiffusionFrame
import com.shifthackz.aisdv1.network.response.EasyDiffusionModels
import com.shifthackz.aisdv1.network.response.EasyDiffusionPing
import com.shifthackz.aisdv1.network.response.EasyDiffusionSubmission

/** Anonymous Easy Diffusion v3 protocol with strict failure propagation and no automatic retries.
 * @author Dmitriy Moroz */
interface EasyDiffusionApi {
    /** Checks readiness and optionally renews an operation's session TTL.
     * @param baseUrl HTTP(S) root or reverse-proxy prefix.
     * @param sessionId Optional unique generation session.
     * @author Dmitriy Moroz */
    suspend fun ping(baseUrl: String, sessionId: String?): EasyDiffusionPing

    /** Fetches the current catalog without guessing model paths.
     * @param baseUrl Candidate or configured prefix.
     * @author Dmitriy Moroz */
    suspend fun models(baseUrl: String): EasyDiffusionModels

    /** Submits exactly once; callers must treat a lost response as an unknown remote outcome.
     * @param baseUrl Captured instance prefix.
     * @param request Supported explicit render controls.
     * @author Dmitriy Moroz */
    suspend fun render(baseUrl: String, request: EasyDiffusionRequest): EasyDiffusionSubmission

    /** Frames concatenated JSON incrementally until EOF; EOF has no completion meaning.
     * @param baseUrl Captured instance prefix.
     * @param task Positive accepted task identity.
     * @param onFrame Receives each complete decoded object.
     * @author Dmitriy Moroz */
    suspend fun stream(baseUrl: String, task: Long, onFrame: suspend (EasyDiffusionFrame) -> Unit)

    /** Stops exactly one task, preserving error status including 404/409.
     * @param baseUrl Captured instance prefix.
     * @param task Positive accepted task identity; zero is rejected before HTTP.
     * @author Dmitriy Moroz */
    suspend fun stop(baseUrl: String, task: Long)
}
