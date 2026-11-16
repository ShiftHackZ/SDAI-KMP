package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProgress
import com.shifthackz.aisdv1.domain.gateway.EasyDiffusionTaskRegistry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes operation ownership across foreground screens and background workers.
 * @author Dmitriy Moroz
 */
internal class InMemoryEasyDiffusionTaskRegistry : EasyDiffusionTaskRegistry {
    private val mutex = Mutex()
    private val state = MutableStateFlow(EasyDiffusionProgress())
    private var sessionId: String? = null
    private var job: Job? = null
    private var ownerId: String? = null
    private var isCancelled = false
    override val progress = state.asStateFlow()

    /** Claims exclusive operation ownership and publishes initial queued progress; concurrent starts fail.
     * @param sessionId Non-null identity retained for this operation until matching completion.
     * @param job Captured generation coroutine cancelled only by its matching owner.
     * @param ownerId Exact background job UUID; a different active owner remains untouched.
     * @author Dmitriy Moroz */

    override suspend fun begin(sessionId: String, job: Job, ownerId: String?) = mutex.withLock {
        check(this.sessionId == null) { "Easy Diffusion generation is already running" }
        this.sessionId = sessionId
        this.job = job
        this.ownerId = ownerId
        isCancelled = false
        state.value = EasyDiffusionProgress(ownerId = ownerId, isRunning = true)
    }

    /** Marks only the exact matching background owner cancelled before cancelling its captured coroutine.
     * @param ownerId Exact background job UUID; a different active owner remains untouched.
     * @author Dmitriy Moroz */

    override suspend fun cancel(ownerId: String) {
        val owner = mutex.withLock {
            if (this.ownerId != ownerId) return@withLock null
            isCancelled = true
            job
        }
        owner?.cancel()
    }

    /** Replaces progress only while the session still owns the uncancelled operation.
     * @param sessionId Identity whose ownership must still match before progress is published.
     * @param value Current queue or sampling state; the registry attaches the captured background owner.
     * @author Dmitriy Moroz */


    override suspend fun update(sessionId: String, value: EasyDiffusionProgress) = mutex.withLock {
        if (this.sessionId == sessionId && !isCancelled) state.value = value.copy(ownerId = ownerId)
    }

    /** Runs final publication while excluding owner cancellation/replacement; stale sessions fail before executing the block.
     * @param sessionId Identity that must still own the uncancelled operation.
     * @param block Gallery publication performed while cancellation and replacement are excluded.
     * @author Dmitriy Moroz */

    override suspend fun <T> withActive(sessionId: String, block: suspend () -> T): T =
        mutex.withLock {
            check(this.sessionId == sessionId && !isCancelled) { "Easy Diffusion generation was replaced" }
            block()
        }

    /** Clears progress and ownership only when the finishing session still owns the registry.
     * @param sessionId Finishing identity; a different current owner is preserved.
     * @author Dmitriy Moroz */

    override suspend fun finish(sessionId: String) = mutex.withLock {
        if (this.sessionId == sessionId) {
            this.sessionId = null
            job = null
            ownerId = null
            state.value = EasyDiffusionProgress()
        }
    }
}
