package com.shifthackz.aisdv1.work

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.localization.formatter.DurationFormatter
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionWorkOwner
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase
import kotlinx.coroutines.withContext
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveBonsaiProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * Runs one captured iOS background request and suppresses results/status after its job is replaced.
 * @param backgroundWorkObserver Publishes lifecycle and progress to shared application UI.
 * @param textToImageUseCase Dispatches captured text inputs through the selected provider.
 * @param imageToImageUseCase Dispatches captured image inputs through the selected provider.
 * @param interruptSourceGenerationUseCase Interrupts only the captured legacy provider inside its retained cleanup barrier.
 * @param observeHordeProcessStatusUseCase Observes AI Horde queue and wait estimates.
 * @param observeLocalDiffusionProcessStatusUseCase Observes ONNX sampling steps.
 * @param observeCoreMlProcessStatusUseCase Observes Core ML sampling steps.
 * @param observeBonsaiProcessStatusUseCase Observes Bonsai sampling steps.
 * @param observeEasyDiffusionProgressUseCase Observes remote progress tagged with the exact background job UUID.
 * @param preferenceManager Supplies source defaults only for legacy payloads without captured source identity.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalUuidApi::class)
internal class BackgroundTaskManagerImpl(
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val textToImageUseCase: TextToImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
    private val interruptSourceGenerationUseCase: InterruptSourceGenerationUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    private val observeCoreMlProcessStatusUseCase: ObserveCoreMlProcessStatusUseCase,
    private val observeBonsaiProcessStatusUseCase: ObserveBonsaiProcessStatusUseCase,
    private val preferenceManager: PreferenceManager,
    private val observeEasyDiffusionProgressUseCase: ObserveEasyDiffusionProgressUseCase,
) : BackgroundTaskManager {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val statusScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var activeJob: Job? = null

    /** Independent join barrier retaining cancelled work even when its replacement is also cancelled.
     * @author Dmitriy Moroz */
    private var cleanupJob: Job? = null
    private var activeSource: ServerSource? = null
    private var activeSelectionRevision: Long? = null

    /** Per-job dispatch marker preventing duplicate legacy stop calls for work cancelled while awaiting cleanup.
     * @author Dmitriy Moroz */
    private var activeDispatchState: MutableStateFlow<Boolean>? = null
    private var lastTextToImagePayload: TextToImagePayload? = null
    private var lastImageToImagePayload: ImageToImagePayload? = null

    /**
     * Captures and schedules text generation, replacing older work after its cleanup.
     * @param payload Immutable form inputs and provider revision captured at submission.
     *
     * @author Dmitriy Moroz
     */
    override fun scheduleTextToImageTask(payload: TextToImagePayload) {
        lastTextToImagePayload = payload
        runGenerationTask(
            payload.sourceSnapshot ?: preferenceManager.source,
            payload.providerSelectionRevision ?: preferenceManager.providerSelectionRevision,
        ) {
            textToImageUseCase(payload)
        }
    }

    /**
     * Captures and schedules image generation, retaining the original request image and provider.
     * @param payload Immutable image inputs and provider revision captured at submission.
     *
     * @author Dmitriy Moroz
     */
    override fun scheduleImageToImageTask(payload: ImageToImagePayload) {
        lastImageToImagePayload = payload
        runGenerationTask(
            payload.sourceSnapshot ?: preferenceManager.source,
            payload.providerSelectionRevision ?: preferenceManager.providerSelectionRevision,
        ) {
            imageToImageUseCase(payload)
        }
    }

    /**
     * Resubmits the last captured text request, or returns failure when none has been scheduled.
     *
     * @author Dmitriy Moroz
     */
    override fun retryLastTextToImageTask(): Result<Unit> {
        val payload = lastTextToImagePayload
            ?: return Result.failure(Throwable("Payload is null."))
        scheduleTextToImageTask(payload)
        return Result.success(Unit)
    }

    /**
     * Resubmits the last captured image request, or returns failure when none has been scheduled.
     *
     * @author Dmitriy Moroz
     */
    override fun retryLastImageToImageTask(): Result<Unit> {
        val payload = lastImageToImagePayload
            ?: return Result.failure(Throwable("Payload is null."))
        scheduleImageToImageTask(payload)
        return Result.success(Unit)
    }

    /**
     * Cancels the captured job and subscriptions; Easy Diffusion cleanup is performed by that job rather than a wildcard interrupt.
     *
     * @author Dmitriy Moroz
     */
    override fun cancelAll(): Result<Unit> = runCatching {
        val cancelledJob = activeJob
        val cancelledSource = activeSource
        val cancelledRevision = activeSelectionRevision
        cancelledJob?.cancel()
        val hasDispatched = activeDispatchState?.value == true
        retainCleanup(cancelledJob, cancelledSource.takeIf { hasDispatched }, cancelledRevision)
        activeJob = null
        activeSource = null
        activeSelectionRevision = null
        activeDispatchState = null
        clearStatusSubscriptions()
        backgroundWorkObserver.postCancelSignal()
    }

    /** Replaces the previous captured job, waiting for task-scoped cleanup before starting another render.
     * @param source Provider captured by this work request, never reread for progress dispatch.
     * @param selectionRevision Provider revision captured by this work request, including legacy-interruption ownership.
     * @param block Generation operation producing application results through normal persistence.
     * @author Dmitriy Moroz */
    private fun runGenerationTask(
        source: ServerSource,
        selectionRevision: Long,
        block: suspend () -> List<AiGenerationResult>
    ) {
        val previousJob = activeJob
        previousJob?.cancel()
        val previousCleanup = retainCleanup(
            previousJob,
            activeSource.takeIf { activeDispatchState?.value == true },
            activeSelectionRevision,
        )
        clearStatusSubscriptions()
        backgroundWorkObserver.dismissResult()
        val ownerId = Uuid.random().toString()
        val dispatchState = MutableStateFlow(false)
        lateinit var generationJob: Job
        generationJob = coroutineScope.launch(start = CoroutineStart.LAZY) {
            val backgroundExecution = IosBackgroundExecution(
                name = "SDAI Generation", activeJob = { generationJob },
            )
            try {
                previousCleanup?.join()
                backgroundExecution.begin()
                listenSourceStatus(source, generationJob, ownerId)
                postRunningMessage()
                val result = withContext(EasyDiffusionWorkOwner(ownerId)) {
                    dispatchState.value = true
                    coroutineContext.ensureActive()
                    block()
                }
                if (isActiveJob(generationJob)) backgroundWorkObserver.postSuccessSignal(result)
            } catch (failure: CancellationException) {
                if (isActiveJob(generationJob)) backgroundWorkObserver.postCancelSignal()
            } catch (failure: Throwable) {
                if (isActiveJob(generationJob)) backgroundWorkObserver.postFailedSignal(failure)
            } finally {
                if (isActiveJob(generationJob)) {
                    clearStatusSubscriptions()
                    activeJob = null
                    activeSource = null
                    activeSelectionRevision = null
                    activeDispatchState = null
                }
                backgroundExecution.end()
            }
        }
        activeSource = source
        activeSelectionRevision = selectionRevision
        activeDispatchState = dispatchState
        activeJob = generationJob
        generationJob.start()
    }

    /** Retains a join barrier outside cancelled generation jobs so cancellation chains cannot skip earlier cleanup.
     * @param job Recently cancelled generation whose finally block may still own a remote task.
     * @param source Captured provider requiring legacy interruption; null retains coroutine cleanup only.
     * @param selectionRevision Captured revision that must still own the provider before legacy interruption.
     * @return Barrier joining every outstanding predecessor, or null when there is no retained cleanup.
     * @author Dmitriy Moroz */
    private fun retainCleanup(
        job: Job?,
        source: ServerSource? = null,
        selectionRevision: Long? = null,
    ): Job? {
        val previousCleanup = cleanupJob?.takeUnless { it.isCompleted }
        if (job == null && source == null) return previousCleanup.also { cleanupJob = it }
        return coroutineScope.launch {
            previousCleanup?.join()
            try {
                if (source != null && source != ServerSource.EASY_DIFFUSION &&
                    preferenceManager.source == source &&
                    preferenceManager.providerSelectionRevision == selectionRevision
                ) {
                    runCatching { interruptSourceGenerationUseCase(source) }
                }
            } finally {
                job?.join()
            }
        }.also { cleanupJob = it }
    }

    /** Checks object identity rather than the current provider preference before publishing.
     * @param job Captured generation coroutine associated with a callback.
     * @author Dmitriy Moroz */
    private fun isActiveJob(job: Job): Boolean = activeJob === job

    /** Attaches status only for the provider captured by the active job.
     * @param source Immutable provider selection for this request.
     * @param job Job identity used to reject delayed status callbacks.
     * @param ownerId Exact UUID carried by Easy Diffusion progress.
     * @author Dmitriy Moroz */
    private fun listenSourceStatus(source: ServerSource, job: Job, ownerId: String) {
        when (source) {
            ServerSource.EASY_DIFFUSION -> listenEasyDiffusionStatus(job, ownerId)
            ServerSource.HORDE -> listenHordeStatus(job)
            ServerSource.LOCAL_MICROSOFT_ONNX -> listenLocalDiffusionStatus(job)
            ServerSource.LOCAL_APPLE_CORE_ML -> listenCoreMlStatus(job)
            ServerSource.LOCAL_APPLE_BONSAI -> listenBonsaiStatus(job)
            else -> Unit
        }
    }

    /** Maps only the matching remote job's queued/sampling progress into iOS status.
     * @param job Captured generation coroutine; replacement invalidates every callback.
     * @param ownerId Exact UUID attached when the domain operation registered this background job.
     * @author Dmitriy Moroz */
    private fun listenEasyDiffusionStatus(job: Job, ownerId: String) {
        statusScope.launch {
            observeEasyDiffusionProgressUseCase().collect { status ->
                if (!isActiveJob(job) || !status.isRunning || status.ownerId != ownerId) return@collect
                if (status.totalSteps > 0) postStepMessage(status.step, status.totalSteps)
                else postRunningMessage()
            }
        }
    }

    /** Observes queue position and wait estimates only while this job remains active.
     * @param job Captured job whose status may update the observer.
     * @author Dmitriy Moroz */
    private fun listenHordeStatus(job: Job) {
        statusScope.launch {
            observeHordeProcessStatusUseCase()
                .catch { /* Status updates are best-effort on iOS background work. */ }
                .collect { status ->
                    if (!isActiveJob(job)) return@collect
                    val title = Localization.string("notification_running_title")
                    val subTitle = buildString {
                        appendLine(
                            Localization.string(
                                "communicating_status_queue",
                                "${status.queuePosition}",
                            ),
                        )
                        append(
                            Localization.string(
                                "communicating_wait_time",
                                DurationFormatter.formatDurationInSeconds(status.waitTimeSeconds),
                            ),
                        )
                    }
                    backgroundWorkObserver.postStatusMessage(title, subTitle)
                }
        }
    }

    /** Observes ONNX sampling steps only while this job remains active.
     * @param job Captured job whose status may update the observer.
     * @author Dmitriy Moroz */
    private fun listenLocalDiffusionStatus(job: Job) {
        statusScope.launch {
            observeLocalDiffusionProcessStatusUseCase()
                .catch { /* Status updates are best-effort on iOS background work. */ }
                .collect { status ->
                    if (!isActiveJob(job)) return@collect
                    postStepMessage(status.current, status.total)
                }
        }
    }

    /** Observes Core ML sampling steps only while this job remains active.
     * @param job Captured job whose status may update the observer.
     * @author Dmitriy Moroz */
    private fun listenCoreMlStatus(job: Job) {
        statusScope.launch {
            observeCoreMlProcessStatusUseCase()
                .catch { /* Status updates are best-effort on iOS background work. */ }
                .collect { status ->
                    if (!isActiveJob(job)) return@collect
                    postStepMessage(status.current, status.total)
                }
        }
    }

    /** Observes Bonsai sampling steps only while this job remains active.
     * @param job Captured job whose status may update the observer.
     * @author Dmitriy Moroz */
    private fun listenBonsaiStatus(job: Job) {
        statusScope.launch {
            observeBonsaiProcessStatusUseCase()
                .catch { /* Status updates are best-effort on iOS background work. */ }
                .collect { status ->
                    if (!isActiveJob(job)) return@collect
                    postStepMessage(status.current, status.total)
                }
        }
    }

    /** Publishes indeterminate queued/loading status for the captured active job.
     * @author Dmitriy Moroz */
    private fun postRunningMessage() {
        backgroundWorkObserver.postStatusMessage(
            title = Localization.string("notification_running_title"),
            subTitle = Localization.string("notification_running_sub_title"),
        )
    }

    /** Publishes determinate sampling progress using the shared localized status contract.
     * @param current Completed sampling step.
     * @param total Total requested sampling steps.
     * @author Dmitriy Moroz */
    private fun postStepMessage(current: Int, total: Int) {
        backgroundWorkObserver.postStatusMessage(
            title = Localization.string("notification_running_title"),
            subTitle = Localization.string(
                "communicating_status_steps",
                current.toString(),
                total.toString(),
            ),
        )
    }

    /** Cancels all old subscriptions before replacement or completion; the generation job owns remote cleanup.
     * @author Dmitriy Moroz */
    private fun clearStatusSubscriptions() {
        statusScope.coroutineContext.cancelChildren()
    }
}
