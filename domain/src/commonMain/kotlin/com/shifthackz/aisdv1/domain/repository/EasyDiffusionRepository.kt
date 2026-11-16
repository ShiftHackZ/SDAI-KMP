package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask

/**
 * Strict transport, model cache and result persistence operations for Easy Diffusion.
 * Callers own retry, cancellation and optional-error policy; failures are never converted to cache hits.
 * @author Dmitriy Moroz
 */
interface EasyDiffusionRepository {
    /** Checks server readiness without changing configuration.
     * @param url Candidate HTTP(S) root or prefix.
     * @param sessionId Optional operation session whose task TTL should be renewed.
     * @return Server status string, including busy/loading states.
     * @author Dmitriy Moroz */
    suspend fun ping(url: String, sessionId: String? = null): String

    /** Replaces only this endpoint's model cache after a successful fetch.
     * @param url Candidate or configured instance.
     * @author Dmitriy Moroz */
    suspend fun fetchModels(url: String)

    /** Reads an endpoint-scoped catalog; an unfetched catalog is empty.
     * @param url Instance whose successfully fetched catalog is requested.
     * @author Dmitriy Moroz */
    suspend fun getModels(url: String): List<EasyDiffusionModel>

    /** Submits once; an ambiguous failure must never be retried automatically.
     * @param request Captured controls and unique session identity.
     * @return Captured task identity, validated before streaming or stopping.
     * @author Dmitriy Moroz */
    suspend fun submit(request: EasyDiffusionRequest): EasyDiffusionTask

    /** Reads complete adjacent JSON frames until EOF, propagating HTTP and framing failures.
     * @param task Captured server/session/task identity.
     * @param onFrame Consumes each progress or terminal frame without retaining the stream history.
     * @author Dmitriy Moroz */
    suspend fun readStream(task: EasyDiffusionTask, onFrame: suspend (EasyDiffusionFrame) -> Unit)

    /** Stops only the supplied positive task ID; 404/409 remain explicit failures.
     * @param task Captured identity from the accepted render response.
     * @author Dmitriy Moroz */
    suspend fun stop(task: EasyDiffusionTask)

    /** Decodes the original image, validates dimensions and follows normal gallery/autosave behavior.
     * @param request Exact controls submitted for this single image.
     * @param frame Successful terminal output containing encoded bytes and actual seed.
     * @return Validated result with original encoded image bytes.
     * @author Dmitriy Moroz */
    suspend fun save(request: EasyDiffusionRequest, frame: EasyDiffusionFrame): AiGenerationResult
}
