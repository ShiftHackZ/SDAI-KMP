package com.shifthackz.aisdv1.domain.datasource

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask

/**
 * Remote v3 protocol boundary. HTTP, parsing and cancellation failures propagate without retry.
 * @author Dmitriy Moroz
 */
interface EasyDiffusionRemoteDataSource {
    /** Reads readiness without mutating selected settings.
     * @param url Captured HTTP(S) instance prefix.
     * @param sessionId Optional unique session whose task TTL is renewed.
     * @author Dmitriy Moroz */
    suspend fun ping(url: String, sessionId: String?): String

    /** Returns only stable-diffusion checkpoints while retaining exact identifiers.
     * @param url Candidate or selected instance prefix.
     * @author Dmitriy Moroz */
    suspend fun models(url: String): List<EasyDiffusionModel>

    /** Sends exactly one render POST, including no automatic submission retry.
     * @param request Immutable supported controls and operation identity.
     * @author Dmitriy Moroz */
    suspend fun submit(request: EasyDiffusionRequest): EasyDiffusionTask

    /** Consumes a single stream connection; empty EOF and HTTP425 stay visible to domain policy.
     * @param task Captured endpoint/session/task identity.
     * @param onFrame Receives complete progress or terminal objects.
     * @author Dmitriy Moroz */
    suspend fun readStream(task: EasyDiffusionTask, onFrame: suspend (EasyDiffusionFrame) -> Unit)

    /** Sends only task-specific stop and propagates every HTTP failure.
     * @param task Captured positive task ID and owning endpoint.
     * @author Dmitriy Moroz */
    suspend fun stop(task: EasyDiffusionTask)
}
