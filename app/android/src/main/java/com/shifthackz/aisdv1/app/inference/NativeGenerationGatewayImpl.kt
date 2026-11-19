package com.shifthackz.aisdv1.app.inference

import android.util.Base64
import com.shifthackz.aisdv1.app.diagnostics.GenerationAttemptJournal
import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import com.shifthackz.aisdv1.domain.entity.NativeGenerationOwner
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext

/**
 * Host owner of one native job; gallery writes remain in the existing parent repositories.
 *
 * @param connections Factory for isolated per-attempt bindings.
 * @param files Atomic private exchange storage.
 * @param journal Durable no-replay worker attempt history.
 * @param exits Best-effort exit diagnostics.
 * @param diagnosticScope Host-owned IO scope for bounded stage journal updates.
 *
 * @author Dmitriy Moroz
 */
internal class NativeGenerationGatewayImpl(
    private val connections: InferenceConnectionFactory,
    private val files: GenerationFileStore,
    private val journal: GenerationAttemptJournal,
    private val exits: ProcessExitDiagnosticsGateway,
    private val diagnosticScope: CoroutineScope,
) : NativeGenerationGateway {
    private val admission = Mutex()
    private val active = AtomicReference<OwnedInferenceConnection?>()

    /**
     * Requests counters through the owned connection without starting or binding a service.
     * @author Dmitriy Moroz
     */
    override suspend fun readProcessPerformance(): ProcessPerformanceReading? =
        active.get()?.connection?.readPerformance()

    private val initialized = CompletableDeferred<Unit>()
    private val progress = NativeGenerationEngine.entries.associateWith {
        MutableStateFlow(
            LocalDiffusionStatus(
                0,
                0
            )
        )
    }
    private val stages =
        NativeGenerationEngine.entries.associateWith { MutableStateFlow(NativeGenerationStage.PREPARING) }

    @Volatile
    private var isQuarantined = false

    /**
     * Initializes IO reconciliation on demand before host admission, never in child startup.
     *
     * @author Dmitriy Moroz
     */
    suspend fun initialize() {
        if (initialized.isCompleted) return initialized.await()
        admission.lock()
        try {
            if (!initialized.isCompleted) {
                withContext(Dispatchers.IO) { journal.reconcile(); files.clearAbandoned() }
                initialized.complete(Unit)
            }
        } finally {
            admission.unlock()
        }
    }

    /**
     * Admits one native attempt, validates its completed output and returns encoded image data.
     * Concurrent admission, child failure and cancellation propagate after owned cleanup; requests are not replayed automatically.
     *
     * @param request Captured native generation inputs.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun generate(request: NativeGenerationRequest): String {
        initialize()
        if (!admission.tryLock()) throw NativeGenerationFailure(
            NativeGenerationFailure.Category.BUSY,
            request.engine,
            request.payload.sdxlBackend.takeIf { request.engine == NativeGenerationEngine.SDCPP },
            request.payload.bonsaiBackend.takeIf { request.engine == NativeGenerationEngine.BONSAI },
            request.payload.mnnBackend.takeIf { request.engine == NativeGenerationEngine.MNN })
        val job = UUID.randomUUID().toString()
        val start = System.currentTimeMillis()
        val owner = currentCoroutineContext()[NativeGenerationOwner]?.nextKey()
        var connection: InferenceConnection? = null
        var outcome = "engine"
        var hasBegun = false
        var hasCommittedTerminal = false
        val stage = AtomicReference(NativeGenerationStage.PREPARING)
        val output = try {
            progress.getValue(request.engine).value = LocalDiffusionStatus(0, 0)
            stages.getValue(request.engine).value = NativeGenerationStage.PREPARING
            if (isQuarantined) throw NativeGenerationFailure(NativeGenerationFailure.Category.UNRESPONSIVE)
            withContext(Dispatchers.IO) {
                journal.begin(job, request.engine, owner); hasBegun = true; files.writeRequest(
                job,
                request
            )
            }
            val owned = connections.create()
            connection = owned
            active.set(OwnedInferenceConnection(owned, owner?.substringBeforeLast(':')))
            owned.run(
                job,
                request.engine,
                { progress.getValue(request.engine).value = it },
                { next ->
                    stage.set(next)
                    stages.getValue(request.engine).value = next
                    diagnosticScope.launch { runCatching { journal.stage(job, next) } }
                }) { pid, token ->
                withContext(Dispatchers.IO) { journal.connected(job, pid, token) }
            }
            val output = withContext(Dispatchers.IO) {
                Base64.encodeToString(
                    files.readResult(job),
                    Base64.NO_WRAP
                )
            }
            outcome = "success"
            output
        } catch (failure: CancellationException) {
            outcome = "cancelled"
            withContext(NonCancellable) { connection?.cancel() }
            throw failure
        } catch (failure: NativeGenerationFailure) {
            outcome = failure.category.name.lowercase()
            throw NativeGenerationFailure(
                failure.category,
                request.engine,
                request.payload.sdxlBackend.takeIf { request.engine == NativeGenerationEngine.SDCPP },
                request.payload.bonsaiBackend.takeIf { request.engine == NativeGenerationEngine.BONSAI },
                request.payload.mnnBackend.takeIf { request.engine == NativeGenerationEngine.MNN })
        } catch (_: Exception) {
            throw NativeGenerationFailure(
                NativeGenerationFailure.Category.INVALID_RESULT,
                request.engine,
                request.payload.sdxlBackend.takeIf { request.engine == NativeGenerationEngine.SDCPP },
                request.payload.bonsaiBackend.takeIf { request.engine == NativeGenerationEngine.BONSAI },
                request.payload.mnnBackend.takeIf { request.engine == NativeGenerationEngine.MNN })
        } finally {
            hasCommittedTerminal = !hasBegun
            try {
                withContext(NonCancellable) {
                    val hasClosed = runCatching { connection?.close() ?: true }.getOrDefault(false)
                    if (!hasClosed) isQuarantined = true
                    active.get()?.takeIf { it.connection === connection }
                        ?.let { active.compareAndSet(it, null) }
                    progress.getValue(request.engine).value = LocalDiffusionStatus(0, 0)
                    val exit = if (outcome == "process_died") runCatching {
                        exits.readInferenceExits(
                            start,
                            connection?.processId
                        ).maxByOrNull { it.timestamp }
                    }.getOrNull() else null
                    withContext(Dispatchers.IO) {
                        if (hasBegun) {
                            hasCommittedTerminal = runCatching {
                                journal.finish(
                                    job,
                                    outcome,
                                    stage.get(),
                                    exit
                                )
                            }.isSuccess
                            if (!hasCommittedTerminal) isQuarantined = true
                        }
                        if (hasClosed) runCatching { files.delete(job) }
                    }
                }
            } finally {
                admission.unlock()
            }
        }
        if (!hasCommittedTerminal) throw NativeGenerationFailure(
            NativeGenerationFailure.Category.INVALID_RESULT, request.engine,
            request.payload.sdxlBackend.takeIf { request.engine == NativeGenerationEngine.SDCPP },
            request.payload.bonsaiBackend.takeIf { request.engine == NativeGenerationEngine.BONSAI },
            request.payload.mnnBackend.takeIf { request.engine == NativeGenerationEngine.MNN },
        )
        return output
    }

    /**
     * Cancellation follows the owned binding, not mutable provider preferences.
     *
     * @param ownerId Optional worker UUID; replaced workers cannot cancel a newer request.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun cancelActive(ownerId: String?): Boolean {
        val attempt = active.get() ?: return false
        if (ownerId != null && attempt.ownerId != ownerId) return false
        withContext(NonCancellable) { attempt.connection.cancel() }
        return true
    }

    /**
     * Observes the selected engine's latest sanitized progress, including its initial value.
     *
     * @param engine Runtime whose latest progress is requested.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus(engine: NativeGenerationEngine): Flow<LocalDiffusionStatus> =
        progress.getValue(engine).asStateFlow()

    /**
     * Observes the selected engine's latest sanitized lifecycle stage, including its initial value.
     *
     * @param engine Runtime whose lifecycle stage is requested.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStage(engine: NativeGenerationEngine): Flow<NativeGenerationStage> =
        stages.getValue(engine).asStateFlow()
}
