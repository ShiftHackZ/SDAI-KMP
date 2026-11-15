package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.GenerationFailureContext

/**
 * Best-effort sanitized exit history, read off main and matched to a child process lifetime.
 *
 * @author Dmitriy Moroz
 */
interface ProcessExitDiagnosticsGateway {
    /**
     * Returns sanitized inference-process exits matched by PID and attempt start time.
     * Unsupported APIs, an absent handshake or unavailable system history produce an empty list.
     *
     * @param after Earliest timestamp of the attempt.
     * @param pid Validated child PID, or null when no handshake completed.
     * @return Matching records; empty means unavailable evidence rather than a diagnosis.
     * @author Dmitriy Moroz
     */
    suspend fun readInferenceExits(after: Long, pid: Int?): List<GenerationFailureContext>
}
