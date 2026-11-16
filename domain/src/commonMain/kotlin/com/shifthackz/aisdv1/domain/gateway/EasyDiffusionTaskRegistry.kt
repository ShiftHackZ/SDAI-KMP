package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

/**
 * Process-local ownership of the single Easy Diffusion operation. Cancellation marks the operation
 * before cancelling its job so a submission response learned later can still be stopped safely.
 * @author Dmitriy Moroz
 */
interface EasyDiffusionTaskRegistry {
    /** Current queued/running progress, reset when the owning operation finishes.
     * @author Dmitriy Moroz */
    val progress: StateFlow<EasyDiffusionProgress>

    /** Claims exclusive ownership; concurrent starts fail rather than replacing another task.
     * @param sessionId Fresh operation identity.
     * @param job Coroutine to interrupt when the user requests stop.
     * @param ownerId Optional exact work UUID; foreground ownership is null.
     * @author Dmitriy Moroz */
    suspend fun begin(sessionId: String, job: Job, ownerId: String?)

    /** Requests cancellation of the current owner without stopping unrelated remote work.
     * @param ownerId Exact background work UUID for cancellation; a different active owner is left untouched.
     * @author Dmitriy Moroz */
    suspend fun cancel(ownerId: String)


    /** Publishes progress only for the current uncancelled owner.
     * @param sessionId Captured operation identity.
     * @param value Queued or sampling progress.
     * @author Dmitriy Moroz */
    suspend fun update(sessionId: String, value: EasyDiffusionProgress)

    /**
     * Serializes final publication with owner cancellation and replacement.
     * @param sessionId Operation identity allowed to publish a result.
     * @param block Normal gallery insertion while this operation still owns publication.
     * @return The published result; a cancelled or replaced owner fails before executing the block.
     * @author Dmitriy Moroz
     */
    suspend fun <T> withActive(sessionId: String, block: suspend () -> T): T

    /** Clears only the matching owner, allowing a subsequent operation.
     * @param sessionId Finishing operation identity.
     * @author Dmitriy Moroz */
    suspend fun finish(sessionId: String)
}
