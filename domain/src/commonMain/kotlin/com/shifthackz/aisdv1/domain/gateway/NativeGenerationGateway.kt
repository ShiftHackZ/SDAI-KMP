package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import kotlinx.coroutines.flow.Flow

/**
 * Owns one Android inference attempt and its connection, independently of selected preferences.
 * Implementations return encoded images only after the child committed a complete output file.
 *
 * @author Dmitriy Moroz
 */
interface NativeGenerationGateway {
    /**
     * Requests resource counters from the currently owned child without starting a process.
     * Returns null before submission or after teardown; transport/read failures propagate.
     * Cancelling the read must not cancel inference or the connection's heartbeat.
     *
     * @author Dmitriy Moroz
     */
    suspend fun readProcessPerformance(): ProcessPerformanceReading?

    /**
     * Submits one immutable attempt; concurrent submissions fail without interrupting its owner.
     *
     * @param request Captured runtime and model inputs.
     * @return PNG image encoded as Base64 for the existing repository persistence contract.
     * @author Dmitriy Moroz
     */
    suspend fun generate(request: NativeGenerationRequest): String

    /**
     * Cancels the currently owned local attempt, irrespective of current provider selection.
     *
     * @param ownerId Optional work UUID; stale worker cancellation cannot cancel another attempt.
     * @return Whether a local attempt owned the cancellation.
     * @author Dmitriy Moroz
     */
    suspend fun cancelActive(ownerId: String? = null): Boolean

    /**
     * Observes progress for an engine without constructing or loading that runtime in the host.
     *
     * @param engine Engine whose progress is requested.
     * @author Dmitriy Moroz
     */
    fun observeStatus(engine: NativeGenerationEngine): Flow<LocalDiffusionStatus>

    /**
     * Observes model, tokenizer, inference and image-write stages without loading a runtime.
     *
     * @param engine Runtime whose lifecycle is requested.
     * @author Dmitriy Moroz
     */
    fun observeStage(engine: NativeGenerationEngine): Flow<NativeGenerationStage>
}
