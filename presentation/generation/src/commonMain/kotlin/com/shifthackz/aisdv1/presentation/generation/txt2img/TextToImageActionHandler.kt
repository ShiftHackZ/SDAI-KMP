package com.shifthackz.aisdv1.presentation.generation.txt2img

import kotlin.time.TimeSource

import com.shifthackz.aisdv1.presentation.components.image.ImageSaveResult
import com.shifthackz.aisdv1.presentation.components.image.ImageSaver
import com.shifthackz.aisdv1.presentation.components.image.ImageShareResult
import com.shifthackz.aisdv1.presentation.components.image.ImageSharer
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.isUpscalerInputSizeValid
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.openaioauth.OpenAiOAuthGateway
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthIdentity
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.TextToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.EvaluateGenerationBenchmarkUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.RecordBenchmarkPromptAnswerUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.SuppressBenchmarkWarningsUseCase
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest
import com.shifthackz.aisdv1.domain.entity.benchmark.isLocalGenerationSource
import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.core.ViewModelLauncher
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.core.navigation.router.TextToImageRouter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * Handles form actions, captured foreground/background generation, result persistence and platform export.
 *
 * @param openAiOAuthGateway Guards completed results against account replacement or logout.
 * @param providerSelectionGuard Serializes final UI publication with provider replacement.
 * @param dispatchersProvider Dispatches persistence and generation off the UI thread and publishes state immediately.
 * @param textToImageUseCase Generates results from immutable text inputs through the selected provider.
 * @param saveGenerationResultUseCase Persists a generated result when it has no durable gallery identity.
 * @param saveLastResultToCacheUseCase Keeps the latest generated image available to normal preview/share flows.
 * @param interruptSourceGenerationUseCase Interrupts only the captured legacy provider after its foreground job is cancelled.
 * @param preferenceManager Reads current persistence/export choices and the provider selection revision.
 * @param backgroundTaskManager Schedules captured generation payloads when background mode is enabled.
 * @param backgroundWorkObserver Prevents overlapping foreground/background requests and opens active work status.
 * @param wakeLockInterActor Keeps the device awake during foreground generation and releases it on completion.
 * @param platformServices Reports platform support and opens application result/export surfaces.
 * @param buildInfoProvider Controls result reporting availability for the current build flavor.
 * @param dimensionValidator Validates form dimensions before dispatching generation.
 * @param evaluateGenerationBenchmarkUseCase Lazily obtains recommendation admission policy.
 * @param recordBenchmarkPromptAnswerUseCase Lazily obtains the initial-question recorder.
 * @param suppressBenchmarkWarningsUseCase Lazily obtains the recommendation warning preference writer.
 * @param imageSaver Saves encoded output through the platform media workflow.
 * @param imageSharer Shares encoded output through the platform share workflow.
 * @param router Navigates to benchmark, generation preview and report screens.
 * @param currentState Reads the current form without retaining a stale state snapshot between actions.
 * @param emitState Publishes a complete validated state before attempting generation.
 * @param updateState Atomically transforms current presentation state after asynchronous actions.
 * @param launch Starts coroutines in the owning view model lifecycle.
 * @param onError Reports generation, persistence, picker and export failures to the application error sink.
 * @author Dmitriy Moroz
 */
internal class TextToImageActionHandler(
    private val openAiOAuthGateway: OpenAiOAuthGateway,
    private val providerSelectionGuard: ProviderSelectionGuard,
    private val dispatchersProvider: DispatchersProvider,
    private val textToImageUseCase: TextToImageUseCase,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
    private val saveLastResultToCacheUseCase: SaveLastResultToCacheUseCase,
    private val interruptSourceGenerationUseCase: InterruptSourceGenerationUseCase,
    private val preferenceManager: PreferenceManager,
    private val backgroundTaskManager: BackgroundTaskManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val wakeLockInterActor: WakeLockInterActor,
    private val platformServices: GenerationPlatformServices,
    private val buildInfoProvider: BuildInfoProvider,
    private val dimensionValidator: DimensionValidator,
    private val evaluateGenerationBenchmarkUseCase: () -> EvaluateGenerationBenchmarkUseCase,
    private val recordBenchmarkPromptAnswerUseCase: () -> RecordBenchmarkPromptAnswerUseCase,
    private val suppressBenchmarkWarningsUseCase: () -> SuppressBenchmarkWarningsUseCase,
    private val imageSaver: ImageSaver,
    private val imageSharer: ImageSharer,
    private val router: TextToImageRouter,
    private val currentState: () -> TextToImageState,
    private val emitState: (TextToImageState) -> Unit,
    private val updateState: ((TextToImageState) -> TextToImageState) -> Unit,
    private val launch: ViewModelLauncher,
    private val onError: (Throwable) -> Unit,
) {

    /**
     * Captured foreground coroutine; cancelling it triggers provider-specific cleanup before it can publish results.
     *
     * @author Dmitriy Moroz
     */
    private var generationJob: Job? = null

    /**
     * Per-operation dispatch marker; a cancelled replacement still awaiting cleanup owns no legacy task.
     *
     * @author Dmitriy Moroz
     */
    private var generationDispatchState: MutableStateFlow<Boolean>? = null

    /**
     * Provider identity captured from the launched request, retained independently of later form edits.
     *
     * @author Dmitriy Moroz
     */
    private var generationSource: ServerSource? = null

    /**
     * Selection revision captured with the launched request and used only for its cancellation ownership.
     *
     * @author Dmitriy Moroz
     */
    private var generationSelectionRevision: Long? = null

    /**
     * Independent join barrier retaining every cancelled predecessor until its provider cleanup finishes.
     *
     * @author Dmitriy Moroz
     */
    private var cleanupJob: Job? = null

    private var pendingGeneration: PendingGeneration? = null

    /**
     * Validates the current form, rejects overlapping work and starts or schedules the captured generation request.
     *
     * @author Dmitriy Moroz
     */
    fun generate() {
        if (currentState().generating) return
        val validatedState = currentState().validated(dimensionValidator)
        emitState(validatedState)
        if (!validatedState.canGenerate()) return

        if (!validatedState.mapToPayload().isUpscalerInputSizeValid) {
            updateState {
                it.copy(
                    screenModal = GenerationModal.Error(
                        Localization.string("error_upscaler_generation_size").asUiText()
                    )
                )
            }
            return
        }

        val payload = validatedState.mapToPayload()
        if (backgroundWorkObserver.hasActiveTasks()) {
            updateState {
                it.copy(screenModal = GenerationModal.Background.Running)
            }
            return
        }
        if (validatedState.mode.isLocalGenerationSource()) {
            evaluateBenchmarkGate(validatedState, payload)
            return
        }

        startGeneration(validatedState, payload)
    }

    /**
     * Records the benchmark prompt response and navigates to the local performance benchmark.
     *
     * @author Dmitriy Moroz
     */
    fun runBenchmarkFromPrompt() {
        recordBenchmarkPromptAnswerUseCase()()
        pendingGeneration = null
        updateState { it.copy(screenModal = GenerationModal.None) }
        router.navigateToBenchmark()
    }

    /**
     * Records the skipped benchmark and resumes the captured pending generation request.
     *
     * @author Dmitriy Moroz
     */
    fun skipBenchmarkPrompt() {
        recordBenchmarkPromptAnswerUseCase()()
        val pending =
            pendingGeneration ?: return updateState { it.copy(screenModal = GenerationModal.None) }
        pendingGeneration = null
        updateState { it.copy(screenModal = GenerationModal.None) }
        startGeneration(pending.state, pending.payload)
    }

    /**
     * Resumes the captured generation after a local performance warning.
     *
     * @param suppressFutureWarnings Whether future recommendation warnings should be disabled.
     * @author Dmitriy Moroz
     */
    fun continueAfterBenchmarkWarning(suppressFutureWarnings: Boolean) {
        if (suppressFutureWarnings) {
            suppressBenchmarkWarningsUseCase()()
        }
        val pending =
            pendingGeneration ?: return updateState { it.copy(screenModal = GenerationModal.None) }
        pendingGeneration = null
        updateState { it.copy(screenModal = GenerationModal.None) }
        startGeneration(pending.state, pending.payload)
    }

    /**
     * Discards the pending generation and closes the local performance recommendation.
     *
     * @author Dmitriy Moroz
     */
    fun dismissBenchmarkDialog() {
        pendingGeneration = null
        updateState { it.copy(screenModal = GenerationModal.None) }
    }

    /**
     * Cancels the captured coroutine; Easy Diffusion stops only its captured remote task during domain cleanup.
     *
     * @author Dmitriy Moroz
     */
    fun cancelGeneration() {
        val source = generationSource
        val revision = generationSelectionRevision
        generationJob?.cancel()
        val hasDispatched = generationDispatchState?.value == true
        retainCleanup(generationJob, source.takeIf { hasDispatched }, revision)
        generationJob = null
        generationDispatchState = null
        generationSource = null
        generationSelectionRevision = null
        updateState {
            it.copy(
                generating = false,
                screenModal = GenerationModal.None,
            )
        }
    }

    /**
     * Persists completed results and updates the result modal with durable gallery identities.
     *
     * @param results Completed images whose gallery identities should be retained or created.
     * @author Dmitriy Moroz
     */
    fun saveGenerationResults(results: List<AiGenerationResult>) {
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            runCatching { persistResultsIfNeeded(results) }
                .onSuccess {
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                screenModal = GenerationModal.None,
                                message = Localization.string("success").asUiText(),
                            )
                        }
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(screenModal = GenerationModal.Error(t.localizedMessageText())) }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Ensures the result is persisted and cached before opening its normal preview.
     *
     * @param result Completed image selected by the user.
     * @author Dmitriy Moroz
     */
    fun viewGenerationResult(result: AiGenerationResult) {
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            runCatching { cacheResultIfNeeded(result) }
                .onSuccess { cached ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(screenModal = GenerationModal.None) }
                        router.navigateToGalleryDetails(cached.id)
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(screenModal = GenerationModal.Error(t.localizedMessageText())) }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Persists and caches the selected result before opening the report workflow.
     *
     * @param result Completed image selected by the user.
     * @author Dmitriy Moroz
     */
    fun reportGenerationResult(result: AiGenerationResult) {
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            runCatching { cacheResultIfNeeded(result) }
                .onSuccess { cached ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(screenModal = GenerationModal.None) }
                        router.navigateToReportImage(cached.id)
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    withContext(dispatchersProvider.immediate) {
                        updateState { it.copy(screenModal = GenerationModal.Error(t.localizedMessageText())) }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Exports the supplied encoded image through the platform media saver and reports completion or failure.
     *
     * @param base64 Base64 image payload used by the operation.
     * @author Dmitriy Moroz
     */
    fun saveImage(base64: String) {
        if (currentState().savingImage || currentState().sharingImage) return
        updateState {
            it.copy(
                savingImage = true,
                error = null,
                message = null,
            )
        }
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            val result = runCatching { imageSaver.save(base64) }
                .getOrElse { t ->
                    if (t is CancellationException) throw t
                    ImageSaveResult.Failed(
                        t.message ?: Localization.string("error_unable_to_save_image")
                    )
                }
            withContext(dispatchersProvider.immediate) {
                updateState {
                    when (result) {
                        ImageSaveResult.Saved -> it.copy(
                            savingImage = false,
                            message = Localization.string("success").asUiText(),
                        )

                        ImageSaveResult.Unsupported -> it.copy(
                            savingImage = false,
                            error = Localization.string("error_image_saving_unavailable")
                                .asUiText(),
                        )

                        is ImageSaveResult.Failed -> it.copy(
                            savingImage = false,
                            error = result.message.asUiText(),
                        )
                    }
                }
            }
        }
    }

    /**
     * Shares the supplied encoded image through the platform share surface and restores presentation state afterward.
     *
     * @param base64 Base64 image payload used by the operation.
     * @author Dmitriy Moroz
     */
    fun shareImage(base64: String) {
        if (currentState().savingImage || currentState().sharingImage) return
        updateState {
            it.copy(
                sharingImage = true,
                error = null,
                message = null,
            )
        }
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            val result = runCatching { imageSharer.share(base64) }
                .getOrElse { t ->
                    if (t is CancellationException) throw t
                    ImageShareResult.Failed(
                        t.message ?: Localization.string("error_unable_to_share_image")
                    )
                }
            withContext(dispatchersProvider.immediate) {
                updateState {
                    when (result) {
                        ImageShareResult.Sent -> it.copy(
                            sharingImage = false,
                            message = Localization.string("success").asUiText(),
                        )

                        ImageShareResult.Unsupported -> it.copy(
                            sharingImage = false,
                            error = Localization.string("error_image_sharing_unavailable")
                                .asUiText(),
                        )

                        is ImageShareResult.Failed -> it.copy(
                            sharingImage = false,
                            error = result.message.asUiText(),
                        )
                    }
                }
            }
        }
    }

    /**
     * Evaluates the captured local request and either resumes generation or shows a benchmark recommendation.
     *
     * @param validatedState Validated form snapshot retained while the recommendation is visible.
     * @param payload Immutable request associated with the validated form.
     * @author Dmitriy Moroz
     */
    private fun evaluateBenchmarkGate(
        validatedState: TextToImageState,
        payload: TextToImagePayload,
    ) {
        pendingGeneration = PendingGeneration(validatedState, payload)
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            runCatching {
                evaluateGenerationBenchmarkUseCase()(
                    validatedState.toBenchmarkRequest(
                        payload
                    )
                )
            }
                .onSuccess { result ->
                    withContext(dispatchersProvider.immediate) {
                        when (result) {
                            LocalGenerationGateResult.Ready -> {
                                pendingGeneration = null
                                startGeneration(validatedState, payload)
                            }

                            LocalGenerationGateResult.AskRunBenchmark -> updateState {
                                it.copy(screenModal = GenerationModal.Benchmark.FirstLocalGeneration)
                            }

                            is LocalGenerationGateResult.ConfirmExceedsRecommendation -> updateState {
                                it.copy(
                                    screenModal = GenerationModal.Benchmark.ExceedsRecommendation(
                                        reasons = result.reasons,
                                        recommendation = result.recommendation,
                                    ),
                                )
                            }
                        }
                    }
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    onError(t)
                    withContext(dispatchersProvider.immediate) {
                        pendingGeneration = null
                        startGeneration(validatedState, payload)
                    }
                }
        }
    }

    /**
     * Schedules the immutable background payload or runs a captured foreground coroutine and publishes only active results.
     *
     * @param validatedState Validated form snapshot used to generate this operation.
     * @param payload Immutable text request with provider identity captured at submission.
     * @author Dmitriy Moroz
     */
    private fun startGeneration(
        validatedState: TextToImageState,
        payload: TextToImagePayload,
    ) {
        if (backgroundWorkObserver.hasActiveTasks()) {
            updateState {
                it.copy(screenModal = GenerationModal.Background.Running)
            }
            return
        }
        val shouldScheduleBackground = backgroundGenerationEnabled
        updateState {
            it.copy(
                generating = true,
                generationStartedAt = TimeSource.Monotonic.markNow(),
                error = null,
                message = null,
                screenModal = validatedState.progressModal(preferenceManager.localOnnxAllowCancel),
                results = emptyList(),
            )
        }
        val previousJob = generationJob
        previousJob?.cancel()
        val previousCleanup = retainCleanup(previousJob)
        val dispatchState = MutableStateFlow(false)
        generationDispatchState = dispatchState
        generationSource = payload.sourceSnapshot ?: validatedState.mode
        generationSelectionRevision =
            payload.providerSelectionRevision ?: validatedState.providerSelectionRevision
        generationJob = launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            previousCleanup?.join()
            runCatching {
                if (shouldScheduleBackground) {
                    withContext(dispatchersProvider.immediate) {
                        coroutineContext.ensureActive()
                        backgroundTaskManager.scheduleTextToImageTask(payload)
                        backgroundWorkObserver.refreshStatus()
                        updateState {
                            it.copy(
                                generating = false,
                                screenModal = if (buildInfoProvider.platform == Platform.ANDROID) {
                                    GenerationModal.Background.Scheduled
                                } else {
                                    GenerationModal.None
                                },
                            )
                        }
                    }
                    return@runCatching emptyList<AiGenerationResult>()
                }
                wakeLockInterActor.acquireWakelockUseCase()
                try {
                    dispatchState.value = true
                    coroutineContext.ensureActive()
                    textToImageUseCase(payload).let { results ->
                        if (preferenceManager.autoSaveAiResults) {
                            persistResultsIfNeeded(results)
                        } else {
                            results
                        }
                    }.also { results -> publishGenerationResults(payload, results) }
                } finally {
                    wakeLockInterActor.releaseWakeLockUseCase()
                }
            }
                .onSuccess {
                    if (!shouldScheduleBackground) platformServices.showGenerationSucceeded()
                }
                .onFailure { t ->
                    if (t is CancellationException) throw t
                    platformServices.showGenerationFailed()
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                generating = false,
                                screenModal = GenerationModal.Error(t.localizedMessageText()),
                                results = emptyList(),
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Retains cleanup outside generation jobs so cancelling a replacement cannot skip earlier remote cleanup.
     *
     * @param job Cancelled foreground job that may still own a remote task.
     * @param source Captured provider requiring legacy interruption; null retains coroutine cleanup only.
     * @param selectionRevision Captured revision that must still own the provider before legacy interruption.
     * @return Barrier joining every outstanding predecessor, or null when no cleanup is retained.
     * @author Dmitriy Moroz
     */
    private fun retainCleanup(
        job: Job?,
        source: ServerSource? = null,
        selectionRevision: Long? = null,
    ): Job? {
        val previousCleanup = cleanupJob?.takeUnless { it.isCompleted }
        if (job == null && source == null) return previousCleanup.also { cleanupJob = it }
        return launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            previousCleanup?.join()
            try {
                if (source != null && source != ServerSource.EASY_DIFFUSION &&
                    preferenceManager.source == source &&
                    preferenceManager.providerSelectionRevision == selectionRevision
                ) {
                    runCatching { interruptSourceGenerationUseCase(source) }.onFailure(onError)
                }
            } finally {
                job?.join()
            }
        }.also { cleanupJob = it }
    }

    /**
     * Whether both platform support and the saved preference permit scheduling background generation.
     *
     * @author Dmitriy Moroz
     */
    private val backgroundGenerationEnabled: Boolean
        get() = platformServices.supportsBackgroundGeneration && preferenceManager.backgroundGeneration

    /**
     * Preserves existing gallery identities and persists only newly generated results.
     *
     * @param results Completed images whose gallery identities should be retained or created.
     * @return Results carrying their existing or newly created gallery identities.
     * @author Dmitriy Moroz
     */
    private suspend fun persistResultsIfNeeded(results: List<AiGenerationResult>): List<AiGenerationResult> =
        results.map { persistResultIfNeeded(it) }

    /**
     * Returns an existing durable result unchanged or stores it and attaches the new gallery identity.
     *
     * @param result Completed image selected by the user.
     * @return Result with a durable gallery identity.
     * @author Dmitriy Moroz
     */
    private suspend fun persistResultIfNeeded(result: AiGenerationResult): AiGenerationResult =
        if (result.id > 0L) {
            result
        } else {
            result.copy(id = saveGenerationResultUseCase(result))
        }

    /**
     * Caches a result for downstream preview, reporting or sharing and returns the cached result.
     *
     * @param result Completed image selected by the user.
     * @return Cached result used by downstream presentation workflows.
     * @author Dmitriy Moroz
     */
    private suspend fun cacheResultIfNeeded(result: AiGenerationResult): AiGenerationResult =
        saveLastResultToCacheUseCase(result)

    /**
     * Checks the submitted account and durable source revision on main immediately before publishing.
     * No suspension separates the final checks from the state update while both guards are held.
     *
     * @param payload Immutable source/account/revision captured at submission.
     * @param results Completed generation results; never reused across identities.
     * @author Dmitriy Moroz
     */
    private suspend fun publishGenerationResults(
        payload: TextToImagePayload,
        results: List<AiGenerationResult>
    ) {
        withContext(dispatchersProvider.immediate) {
            val publish = {
                updateState {
                    it.copy(
                        generating = false,
                        screenModal = results.toTextToImageResultModal(
                            autoSaveEnabled = preferenceManager.autoSaveAiResults,
                            reportEnabled = buildInfoProvider.type != BuildType.FOSS,
                        ),
                        results = emptyList(),
                    )
                }
            }
            if (payload.sourceSnapshot == ServerSource.OPEN_AI_OAUTH) {
                val identity = OpenAiOAuthIdentity(
                    payload.openAiOAuthAccountId ?: throw OpenAiOAuthFailure("IDENTITY_CHANGED"),
                    payload.openAiOAuthRevision ?: throw OpenAiOAuthFailure("IDENTITY_CHANGED"),
                )
                providerSelectionGuard.withSelection {
                    openAiOAuthGateway.withIdentity(identity) {
                        if (payload.providerSelectionRevision == null || preferenceManager.source != ServerSource.OPEN_AI_OAUTH ||
                            preferenceManager.providerSelectionRevision != payload.providerSelectionRevision ||
                            results.any { it.openAiOAuthIdentity != identity || it.providerSelectionRevision != payload.providerSelectionRevision }
                        )
                            throw OpenAiOAuthFailure("IDENTITY_CHANGED")
                        publish()
                    }
                }
            } else publish()
        }
    }
}

/**
 * Immutable form and request retained while a local benchmark recommendation is visible.
 *
 * @param state Form snapshot retained while a benchmark or validation decision is pending.
 * @param payload Immutable generation request captured from that form snapshot.
 * @author Dmitriy Moroz
 */
private data class PendingGeneration(
    val state: TextToImageState,
    val payload: TextToImagePayload,
)

/**
 * Maps supported local-generation controls into a performance admission request without changing form state.
 *
 * @param payload Captured generation inputs or history transfer being applied to this form.
 * @author Dmitriy Moroz
 */
private fun TextToImageState.toBenchmarkRequest(payload: TextToImagePayload): LocalGenerationRequest =
    LocalGenerationRequest(
        source = mode,
        width = payload.width,
        height = payload.height,
        samplingSteps = payload.samplingSteps,
        batchCount = batchCount,
        hiresFix = hires.enabled,
        sdxlBackend = payload.sdxlBackend,
    )
