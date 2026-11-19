package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage

/**
 * One host binding and validated process incarnation for one generation attempt.
 *
 * @author Dmitriy Moroz
 */
internal interface InferenceConnection {
    /**
     * PID belonging to the validated Binder incarnation; never used without live-identity checks.
     *
     * @author Dmitriy Moroz
     */
    val processId: Int?

    /**
     * Reads child-owned counters on demand. Returns null before submission or after completion;
     * timeout, child death and malformed responses fail this read without cancelling inference.
     * @author Dmitriy Moroz
     */
    suspend fun readPerformance(): ProcessPerformanceReading?

    /**
     * Binds and validates one child incarnation, persists its identity, then awaits this job completion.
     * Delivers only matching job events; binding, protocol or child-exit failures propagate to the host.
     *
     * @param job Opaque job UUID with an already committed request file.
     * @param engine Expected runtime identifier in every job event.
     * @param onStage Sanitized lifecycle-stage delivery.
     * @param onProgress Nonblocking sanitized step delivery.
     * @param onConnected Persists PID/incarnation before native entry.
     * @author Dmitriy Moroz
     */
    suspend fun run(
        job: String,
        engine: NativeGenerationEngine,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit,
        onConnected: suspend (Int, String) -> Unit
    )

    /**
     * Cooperatively cancels this connection's owned job and awaits termination or failure.
     *
     * @author Dmitriy Moroz
     */
    suspend fun cancel()

    /**
     * Requests process exit and releases binding. False quarantines admission until app restart.
     *
     * @author Dmitriy Moroz
     */
    suspend fun close(): Boolean
}
