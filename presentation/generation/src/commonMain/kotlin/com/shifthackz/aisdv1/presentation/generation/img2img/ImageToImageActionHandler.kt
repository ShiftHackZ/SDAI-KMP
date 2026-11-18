package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.EvaluateGenerationBenchmarkUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.RecordBenchmarkPromptAnswerUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.SuppressBenchmarkWarningsUseCase
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationGateResult
import com.shifthackz.aisdv1.domain.entity.benchmark.LocalGenerationRequest
import com.shifthackz.aisdv1.domain.entity.benchmark.isLocalGenerationSource
import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.components.core.ViewModelLauncher
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.core.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.presentation.components.image.ImageSaveResult
import com.shifthackz.aisdv1.presentation.components.image.ImageSaver
import com.shifthackz.aisdv1.presentation.components.image.ImageShareResult
import com.shifthackz.aisdv1.presentation.components.image.ImageSharer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.io.encoding.Base64

/**
 * Handles form actions, captured foreground/background generation, result persistence and platform export.
 *
 * @param dispatchersProvider Dispatches persistence and generation off the UI thread and publishes state immediately.
 * @param originalImageBudget Bounds decoded image dimensions and allocation while cropping input images.
 * @param getRandomImageUseCase Downloads an input image through the application image source.
 * @param imageToImageUseCase Generates results from immutable image inputs through the selected provider.
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
 * @param platformActions Opens the platform image picker and resolves its supported input sources.
 * @param currentState Reads the current form without retaining a stale state snapshot between actions.
 * @param emitState Publishes a complete validated state before attempting generation.
 * @param updateState Atomically transforms current presentation state after asynchronous actions.
 * @param launch Starts coroutines in the owning view model lifecycle.
 * @param onError Reports generation, persistence, picker and export failures to the application error sink.
 *
 * @author Dmitriy Moroz
 */
internal class ImageToImageActionHandler(
    private val dispatchersProvider: DispatchersProvider,
    private val originalImageBudget: OriginalImageBudget,
    private val getRandomImageUseCase: GetRandomImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
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
    private val router: ImageToImageRouter,
    private val platformActions: ImageToImagePlatformActions,
    private val currentState: () -> ImageToImageState,
    private val emitState: (ImageToImageState) -> Unit,
    private val updateState: ((ImageToImageState) -> ImageToImageState) -> Unit,
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
     * Provider identity captured from the launched form snapshot, retained independently of later edits.
     *
     * @author Dmitriy Moroz
     */
    private var generationSource: ServerSource? = null

    /**
     * Selection revision captured with the launched form and used only for its cancellation ownership.
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

    private var pendingGenerationState: ImageToImageState? = null

    /**
     * Opens the requested platform input source and resets obsolete mask/results when a new image is selected.
     *
     * @param source Platform input source to open.
     *
     * @author Dmitriy Moroz
     */
    fun pickImage(source: ImageToImagePickSource) {
        if (currentState().pickingImage || currentState().generating) return
        updateState {
            it.copy(
                pickingImage = true,
                error = null,
                message = null,
            )
        }
        launch(dispatchersProvider.immediate, CoroutineStart.DEFAULT) {
            when (val result = platformActions.pickImage(source)) {
                is ImageToImagePickResult.Selected -> updateState {
                    it.copy(
                        pickingImage = false,
                        imageBase64 = result.base64,
                        inPaint = ImageInPaintState(),
                        results = emptyList(),
                    )
                }

                ImageToImagePickResult.Cancelled -> updateState {
                    it.copy(pickingImage = false)
                }

                ImageToImagePickResult.Unsupported -> updateState {
                    it.copy(
                        pickingImage = false,
                        error = Localization.string("error_image_picking_unavailable"),
                    )
                }

                is ImageToImagePickResult.Failed -> updateState {
                    it.copy(
                        pickingImage = false,
                        error = result.message,
                    )
                }
            }
        }
    }

    /**
     * Downloads and crops a random input image within the configured allocation budget, then resets obsolete mask/results.
     *
     * @author Dmitriy Moroz
     */
    fun pickRandomImage() {
        if (currentState().pickingImage || currentState().generating) return
        updateState {
            it.copy(
                pickingImage = true,
                error = null,
                message = null,
            )
        }
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            runCatching {
                cropBase64ImageToSquare(
                    Base64.encode(getRandomImageUseCase()),
                    originalImageBudget
                )
            }
                .onSuccess { base64 ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                pickingImage = false,
                                imageBase64 = base64,
                                inPaint = ImageInPaintState(),
                                results = emptyList(),
                            )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                pickingImage = false,
                                error = t.message ?: Localization.string("error_invalid"),
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Validates the current form, rejects overlapping work and starts or schedules the captured generation request.
     *
     * @throws IllegalStateException when the current state is invalid.
     *
     * @author Dmitriy Moroz
     */
    fun generate() {
        if (currentState().generating) return
        val validatedState = currentState().validated(dimensionValidator)
        emitState(validatedState)
        if (!validatedState.canGenerate) return

        if (backgroundWorkObserver.hasActiveTasks()) {
            updateState {
                it.copy(screenModal = GenerationModal.Background.Running)
            }
            return
        }

        if (validatedState.mode.isLocalGenerationSource()) {
            evaluateBenchmarkGate(validatedState)
            return
        }

        startGeneration(validatedState)
    }

    /**
     * Records the benchmark prompt response and navigates to the local performance benchmark.
     *
     * @author Dmitriy Moroz
     */
    fun runBenchmarkFromPrompt() {
        recordBenchmarkPromptAnswerUseCase()()
        pendingGenerationState = null
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
        val pending = pendingGenerationState
            ?: return updateState { it.copy(screenModal = GenerationModal.None) }
        pendingGenerationState = null
        updateState { it.copy(screenModal = GenerationModal.None) }
        startGeneration(pending)
    }

    /**
     * Resumes the captured generation after a local performance warning.
     *
     * @param suppressFutureWarnings Whether future recommendation warnings should be disabled.
     *
     * @author Dmitriy Moroz
     */
    fun continueAfterBenchmarkWarning(suppressFutureWarnings: Boolean) {
        if (suppressFutureWarnings) {
            suppressBenchmarkWarningsUseCase()()
        }
        val pending = pendingGenerationState
            ?: return updateState { it.copy(screenModal = GenerationModal.None) }
        pendingGenerationState = null
        updateState { it.copy(screenModal = GenerationModal.None) }
        startGeneration(pending)
    }

    /**
     * Discards the pending generation and closes the local performance recommendation.
     *
     * @author Dmitriy Moroz
     */
    fun dismissBenchmarkDialog() {
        pendingGenerationState = null
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
     *
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
                                message = Localization.string("success"),
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
     *
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
     *
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
     *
     * @author Dmitriy Moroz
     */
    fun saveImage(base64: String) {
        if (currentState().savingImage || currentState().sharingImage) return
        updateState { it.copy(savingImage = true, error = null, message = null) }
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
                            message = Localization.string("message_image_saved"),
                        )

                        ImageSaveResult.Unsupported -> it.copy(
                            savingImage = false,
                            error = Localization.string("error_image_saving_unavailable"),
                        )

                        is ImageSaveResult.Failed -> it.copy(
                            savingImage = false,
                            error = result.message
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
     *
     * @author Dmitriy Moroz
     */
    fun shareImage(base64: String) {
        if (currentState().savingImage || currentState().sharingImage) return
        updateState { it.copy(sharingImage = true, error = null, message = null) }
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
                            message = Localization.string("message_share_sheet_opened"),
                        )

                        ImageShareResult.Unsupported -> it.copy(
                            sharingImage = false,
                            error = Localization.string("error_image_sharing_unavailable"),
                        )

                        is ImageShareResult.Failed -> it.copy(
                            sharingImage = false,
                            error = result.message
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
     *
     * @author Dmitriy Moroz */
    private fun evaluateBenchmarkGate(validatedState: ImageToImageState) {
        pendingGenerationState = validatedState
        launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            val requestPayload = validatedState.mapToPayload()
            runCatching {
                evaluateGenerationBenchmarkUseCase()(
                    validatedState.toBenchmarkRequest(
                        requestPayload
                    )
                )
            }
                .onSuccess { result ->
                    withContext(dispatchersProvider.immediate) {
                        when (result) {
                            LocalGenerationGateResult.Ready -> {
                                pendingGenerationState = null
                                startGeneration(validatedState)
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
                        pendingGenerationState = null
                        startGeneration(validatedState)
                    }
                }
        }
    }

    /**
     * Schedules the immutable background payload or runs a captured foreground coroutine and publishes only active results.
     *
     * @param validatedState Validated form snapshot used to generate this operation.
     *
     * @author Dmitriy Moroz
     */
    private fun startGeneration(validatedState: ImageToImageState) {
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
        generationSource = validatedState.mode
        generationSelectionRevision = validatedState.providerSelectionRevision
        generationJob = launch(dispatchersProvider.io, CoroutineStart.DEFAULT) {
            previousCleanup?.join()
            runCatching {
                val maskBase64 = validatedState.inPaint
                    .takeIf { validatedState.sourceSupportsInPaint }
                    ?.takeIf(ImageInPaintState::hasMask)
                    ?.let {
                        encodeInPaintMaskBase64(
                            validatedState.imageBase64,
                            it.strokes,
                            originalImageBudget
                        )
                    }
                    ?: ""
                if (
                    validatedState.sourceSupportsInPaint &&
                    validatedState.inPaint.hasMask &&
                    maskBase64.isBlank()
                ) {
                    error(Localization.string("error_invalid"))
                }
                val payload = validatedState.mapToPayload(maskBase64)
                if (shouldScheduleBackground) {
                    withContext(dispatchersProvider.immediate) {
                        coroutineContext.ensureActive()
                        backgroundTaskManager.scheduleImageToImageTask(payload)
                        backgroundWorkObserver.refreshStatus()
                        updateState {
                            it.copy(
                                generating = false,
                                screenModal = GenerationModal.Background.Scheduled,
                            )
                        }
                    }
                    return@runCatching emptyList()
                }
                wakeLockInterActor.acquireWakelockUseCase()
                try {
                    dispatchState.value = true
                    coroutineContext.ensureActive()
                    imageToImageUseCase(payload).let { results ->
                        if (preferenceManager.autoSaveAiResults) {
                            persistResultsIfNeeded(results)
                        } else {
                            results
                        }
                    }
                } finally {
                    wakeLockInterActor.releaseWakeLockUseCase()
                }
            }
                .onSuccess { results ->
                    if (shouldScheduleBackground) {
                        return@onSuccess
                    }
                    platformServices.showGenerationSucceeded()
                    withContext(dispatchersProvider.immediate) {
                        updateState {
                            it.copy(
                                generating = false,
                                screenModal = results.toImageToImageResultModal(
                                    autoSaveEnabled = preferenceManager.autoSaveAiResults,
                                    reportEnabled = buildInfoProvider.type != BuildType.FOSS,
                                ),
                                results = emptyList(),
                            )
                        }
                    }
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
     *
     * @return Barrier joining every outstanding predecessor, or null when no cleanup is retained.
     *
     * @author Dmitriy Moroz */
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
     *
     * @return Results carrying their existing or newly created gallery identities.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun persistResultsIfNeeded(results: List<AiGenerationResult>): List<AiGenerationResult> =
        results.map { persistResultIfNeeded(it) }

    /**
     * Returns an existing durable result unchanged or stores it and attaches the new gallery identity.
     *
     * @param result Completed image selected by the user.
     *
     * @return Result with a durable gallery identity.
     *
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
     *
     * @return Cached result used by downstream presentation workflows.
     *
     * @author Dmitriy Moroz
     */
    private suspend fun cacheResultIfNeeded(result: AiGenerationResult): AiGenerationResult =
        saveLastResultToCacheUseCase(result)
}

/**
 * Maps captured image-generation controls into a local performance admission request.
 *
 * @param payload Captured generation inputs or history transfer being applied to this form.
 *
 * @author Dmitriy Moroz */
private fun ImageToImageState.toBenchmarkRequest(payload: ImageToImagePayload): LocalGenerationRequest =
    LocalGenerationRequest(
        source = mode,
        width = payload.width,
        height = payload.height,
        samplingSteps = payload.samplingSteps,
        batchCount = payload.batchCount,
    )
