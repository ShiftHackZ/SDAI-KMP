package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.domain.gateway.RemoteGenerationProgressGateway
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.ObserveEasyDiffusionProgressUseCase
import com.shifthackz.aisdv1.presentation.generation.model.toGenerationModal
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.presentation.components.widget.image.OriginalImageBudget
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.validation.dimension.DimensionValidator
import com.shifthackz.aisdv1.core.validation.seed.SeedValidator
import com.shifthackz.aisdv1.presentation.generation.widget.input.withSeedValidation
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.entity.StableDiffusionModel
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.feature.work.BackgroundTaskManager
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.arliai.FetchAndGetArliAiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.forgemodule.GetForgeModulesUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ImageToImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.InterruptSourceGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveHordeProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveCoreMlProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.ObserveLocalDiffusionProcessStatusUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.sdscript.IsADetailerAvailableUseCase
import com.shifthackz.aisdv1.domain.usecase.sdsampler.GetStableDiffusionSamplersUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.EvaluateGenerationBenchmarkUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.RecordBenchmarkPromptAnswerUseCase
import com.shifthackz.aisdv1.domain.usecase.benchmark.SuppressBenchmarkWarningsUseCase
import com.shifthackz.aisdv1.presentation.components.core.GenerationFormUpdateEvent
import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.core.navigation.router.ImageToImageRouter
import com.shifthackz.aisdv1.presentation.components.image.ImageSaver
import com.shifthackz.aisdv1.presentation.components.image.ImageSharer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext

/**
 * Owns the retained image-to-image form, including the state shared with the inpaint route.
 *
 * Loads the selected configuration, observes preference and restoration updates, and reconciles
 * provider metadata. Delegates picking, generation, cancellation, export and benchmark decisions
 * to the action handler; every published form update revalidates the applicable seed fields.
 *
 * @param diagnostics Receives this screen's state and lifecycle diagnostics through the base MVI view model.
 * @param dispatchersProvider Selects I/O execution and immediate UI state publication.
 * @param getConfigurationUseCase Loads the configured source used for initial form reconciliation.
 * @param getStableDiffusionSamplersUseCase Refreshes AUTOMATIC1111 samplers, allowing the use case's nonempty-cache fallback.
 * @param getForgeModulesUseCase Reads Forge module choices for the configured AUTOMATIC1111 endpoint.
 * @param fetchAndGetArliAiModelsUseCase Loads checkpoint identifiers for ArliAI model selection.
 * @param isADetailerAvailableUseCase Checks whether the configured endpoint exposes ADetailer.
 * @param getRandomImageUseCase Obtains encoded input for the random-image action.
 * @param imageToImageUseCase Executes the captured foreground image-to-image request.
 * @param saveGenerationResultUseCase Gives unsaved outputs durable gallery identities when saving is requested.
 * @param saveLastResultToCacheUseCase Retains unsaved selected outputs for detail and report navigation.
 * @param interruptSourceGenerationUseCase Requests cancellation of the source captured by a dispatched foreground operation.
 * @param remoteGenerationProgressGateway Request-correlated remote job/workflow measurements.
 * @param observeEasyDiffusionProgressUseCase Supplies queue and sampling progress for Easy Diffusion progress dialogs.
 * @param observeHordeProcessStatusUseCase Supplies Horde queue status displayed by communicating dialogs.
 * @param observeCoreMlProcessStatusUseCase Reports native Core ML sampling counters.
 * @param observeLocalDiffusionProcessStatusUseCase Supplies local-diffusion status displayed by non-Easy-Diffusion generating dialogs.
 * @param preferenceManager Observes provider and form settings and persists steps, guidance scale and edited ArliAI model selection.
 * @param backgroundTaskManager Schedules the captured request when background generation is selected and supported.
 * @param backgroundWorkObserver Checks existing background work and refreshes status after scheduling.
 * @param wakeLockInterActor Holds the wake lock around foreground generation and releases it on completion or failure.
 * @param platformServices Reports background-generation support and presents foreground outcome feedback.
 * @param buildInfoProvider Supplies platform and flavor information for initial state and result actions.
 * @param generationFormUpdateEvent Supplies pending gallery/history transfers for this retained form.
 * @param dimensionValidator Checks requested dimensions before generation admission.
 * @param seedValidator Validates web-provider seeds within the JavaScript-safe integer range.
 * @param nativeSeedValidator Validates seeds for native providers without losing integer precision.
 * @param easyDiffusionSeedValidator Validates Easy Diffusion's unsigned 32-bit seed range.
 * @param evaluateGenerationBenchmarkUseCase Lazily resolves the use case deciding whether a local-generation prompt is needed.
 * @param recordBenchmarkPromptAnswerUseCase Lazily resolves the recorder for the initial benchmark prompt answer.
 * @param suppressBenchmarkWarningsUseCase Lazily resolves the use case persisting warning suppression.
 * @param imageSaver Saves encoded output through the current platform's image-export contract.
 * @param imageSharer Hands encoded output to the current platform's share surface.
 * @param router Handles image detail, report, configuration, inpaint and benchmark navigation.
 * @param platformActions Acquires or transforms input images through supported native image actions.
 * @param originalImageBudget Bounds full-resolution mask encoding for the selected source image.
 * @param onError Receives failures reported by configuration, metadata and delegated image operations.
 *
 * @author Dmitriy Moroz
 */
class ImageToImageViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val getStableDiffusionSamplersUseCase: GetStableDiffusionSamplersUseCase,
    private val getForgeModulesUseCase: GetForgeModulesUseCase,
    private val fetchAndGetArliAiModelsUseCase: FetchAndGetArliAiModelsUseCase,
    private val isADetailerAvailableUseCase: IsADetailerAvailableUseCase,
    private val getRandomImageUseCase: GetRandomImageUseCase,
    private val imageToImageUseCase: ImageToImageUseCase,
    private val saveGenerationResultUseCase: SaveGenerationResultUseCase,
    private val saveLastResultToCacheUseCase: SaveLastResultToCacheUseCase,
    private val interruptSourceGenerationUseCase: InterruptSourceGenerationUseCase,
    private val remoteGenerationProgressGateway: RemoteGenerationProgressGateway,
    private val observeEasyDiffusionProgressUseCase: ObserveEasyDiffusionProgressUseCase,
    private val observeHordeProcessStatusUseCase: ObserveHordeProcessStatusUseCase,
    private val observeCoreMlProcessStatusUseCase: ObserveCoreMlProcessStatusUseCase,
    private val observeLocalDiffusionProcessStatusUseCase: ObserveLocalDiffusionProcessStatusUseCase,
    private val preferenceManager: PreferenceManager,
    private val backgroundTaskManager: BackgroundTaskManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
    private val wakeLockInterActor: WakeLockInterActor,
    private val platformServices: GenerationPlatformServices,
    private val buildInfoProvider: BuildInfoProvider,
    private val generationFormUpdateEvent: GenerationFormUpdateEvent,
    private val dimensionValidator: DimensionValidator,
    private val seedValidator: SeedValidator,
    private val nativeSeedValidator: SeedValidator,
    private val easyDiffusionSeedValidator: SeedValidator,
    private val evaluateGenerationBenchmarkUseCase: () -> EvaluateGenerationBenchmarkUseCase,
    private val recordBenchmarkPromptAnswerUseCase: () -> RecordBenchmarkPromptAnswerUseCase,
    private val suppressBenchmarkWarningsUseCase: () -> SuppressBenchmarkWarningsUseCase,
    private val imageSaver: ImageSaver,
    private val imageSharer: ImageSharer,
    private val router: ImageToImageRouter,
    private val platformActions: ImageToImagePlatformActions,
    private val originalImageBudget: OriginalImageBudget,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ImageToImageState, ImageToImageIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.IMAGE_TO_IMAGE.asDiagnosticScreen(),
    initialState = ImageToImageState(
        samplingSteps = preferenceManager.imageToImageSamplingSteps,
        cfgScale = preferenceManager.imageToImageCfgScale,
        platform = buildInfoProvider.platform,
        bonsaiBackendSelectionVisible = buildInfoProvider.platform == Platform.ANDROID,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    /**
     * Applies a reducer and validates provider-specific seed fields in the same state publication.
     *
     * @param mutation Transformation applied to the current image-to-image state.
     *
     * @author Dmitriy Moroz
     */
    private fun updateFormState(mutation: (ImageToImageState) -> ImageToImageState) {
        updateState {
            mutation(it).withSeedValidation(
                seedValidator,
                nativeSeedValidator,
                easyDiffusionSeedValidator
            )
        }
    }

    /**
     * Validates seed fields before publishing a replacement state supplied by the action handler.
     *
     * @param state Replacement form state, including its provider selection and seed text.
     *
     * @author Dmitriy Moroz
     */
    private fun emitFormState(state: ImageToImageState) {
        emitState(
            state.withSeedValidation(
                seedValidator,
                nativeSeedValidator,
                easyDiffusionSeedValidator
            )
        )
    }

    private var stableDiffusionSamplers: List<String>? = null
    private var stableDiffusionSamplersKey: StableDiffusionSamplersKey? = null
    private var forgeModules: List<ForgeModule>? = null
    private var forgeModulesKey: StableDiffusionSamplersKey? = null
    private var aDetailerAvailable: Boolean? = null
    private var aDetailerAvailabilityKey: StableDiffusionSamplersKey? = null
    private var arliAiModels: List<String>? = null
    private var arliAiModelsKey: String? = null
    private var samplersLoadJob: Job? = null

    init {
        loadConfiguration()
        observeSettings()
        consumePendingGenerationFormUpdate()
        observeGenerationFormUpdate()
        observeGenerationProgress()
    }

    private val actionHandler = ImageToImageActionHandler(
        dispatchersProvider = dispatchersProvider,
        getRandomImageUseCase = getRandomImageUseCase,
        imageToImageUseCase = imageToImageUseCase,
        saveGenerationResultUseCase = saveGenerationResultUseCase,
        saveLastResultToCacheUseCase = saveLastResultToCacheUseCase,
        interruptSourceGenerationUseCase = interruptSourceGenerationUseCase,
        preferenceManager = preferenceManager,
        backgroundTaskManager = backgroundTaskManager,
        backgroundWorkObserver = backgroundWorkObserver,
        wakeLockInterActor = wakeLockInterActor,
        platformServices = platformServices,
        buildInfoProvider = buildInfoProvider,
        dimensionValidator = dimensionValidator,
        evaluateGenerationBenchmarkUseCase = evaluateGenerationBenchmarkUseCase,
        recordBenchmarkPromptAnswerUseCase = recordBenchmarkPromptAnswerUseCase,
        suppressBenchmarkWarningsUseCase = suppressBenchmarkWarningsUseCase,
        imageSaver = imageSaver,
        imageSharer = imageSharer,
        router = router,
        platformActions = platformActions,
        originalImageBudget = originalImageBudget,
        currentState = { currentState },
        emitState = ::emitFormState,
        updateState = { reducer -> updateFormState(reducer) },
        launch = { dispatcher, start, block -> launch(dispatcher, start, block) },
        onError = onError,
    )

    private val intentProcessor = ImageToImageIntentProcessor(
        router = router,
        updateState = { reducer -> updateFormState(reducer) },
        pickImage = actionHandler::pickImage,
        pickRandomImage = actionHandler::pickRandomImage,
        generate = actionHandler::generate,
        cancelGeneration = actionHandler::cancelGeneration,
        saveImage = actionHandler::saveImage,
        shareImage = actionHandler::shareImage,
        saveGenerationResults = actionHandler::saveGenerationResults,
        viewGenerationResult = actionHandler::viewGenerationResult,
        reportGenerationResult = actionHandler::reportGenerationResult,
        applyGenerationResult = ::applyGenerationResult,
    )

    /**
     * Dispatches form and benchmark actions, handles sampler retries, then persists steps and guidance scale.
     *
     * @param intent User interaction or foreground-recovery event for the retained form.
     *
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: ImageToImageIntent) {
        when (intent) {
            ImageToImageIntent.RetrySamplers -> {
                if (stableDiffusionSamplers.isNullOrEmpty()) loadSamplers()
            }

            ImageToImageIntent.RefreshADetailerAvailability -> loadADetailerAvailability(force = true)
            ImageToImageIntent.OpenADetailerInstallInstructions -> Unit
            ImageToImageIntent.RunBenchmarkFromPrompt -> actionHandler.runBenchmarkFromPrompt()
            ImageToImageIntent.SkipBenchmarkPrompt -> actionHandler.skipBenchmarkPrompt()
            ImageToImageIntent.ContinueAfterBenchmarkWarning ->
                actionHandler.continueAfterBenchmarkWarning(suppressFutureWarnings = false)

            ImageToImageIntent.SuppressBenchmarkWarningAndContinue ->
                actionHandler.continueAfterBenchmarkWarning(suppressFutureWarnings = true)

            ImageToImageIntent.DismissModal -> actionHandler.dismissBenchmarkDialog()
            is ImageToImageIntent.UpdateArliAiModel -> {
                preferenceManager.arliAiModel = intent.value
                intentProcessor.process(intent)
            }

            else -> intentProcessor.process(intent)
        }
        persistGenerationParameters()
    }

    /**
     * Restores result metadata and optionally replaces the input with the saved source image.
     * When replacement is requested but the saved source is blank, the generated output is used.
     *
     * @param ai Saved result whose metadata is applied to the form.
     * @param inputImage Whether to replace the current input; false retains its image and mask.
     *
     * @author Dmitriy Moroz
     */
    private fun applyGenerationResult(
        ai: AiGenerationResult,
        inputImage: Boolean,
    ) {
        applyGenerationResult(
            ai = ai,
            imageBase64 = if (inputImage) {
                ai.inputImage.ifBlank { ai.image }
            } else {
                null
            },
        )
    }

    /**
     * Always restores the prompt and dimensions. Restores the negative prompt, seed/variation fields,
     * step count, guidance, face restoration and denoising only when saved sampling steps are positive.
     * Sampler selection is independently reconciled against the current catalog.
     * Replacing the input clears its mask and retained results; null keeps both.
     *
     * @param ai Saved result supplying metadata to restore.
     * @param imageBase64 Replacement encoded input, or null to preserve the current input and mask.
     *
     * @author Dmitriy Moroz
     */
    private fun applyGenerationResult(
        ai: AiGenerationResult,
        imageBase64: String?,
    ) {
        val hasDiffusionParameters = ai.samplingSteps > 0
        updateFormState { state ->
            state.copy(
                advancedOptionsVisible = true,
                imageBase64 = imageBase64 ?: state.imageBase64,
                inPaint = if (imageBase64 == null) state.inPaint else ImageInPaintState(),
                results = if (imageBase64 == null) state.results else emptyList(),
                prompt = ai.prompt,
                negativePrompt = if (hasDiffusionParameters) ai.negativePrompt else state.negativePrompt,
                width = ai.width.toString(),
                height = ai.height.toString(),
                seed = if (hasDiffusionParameters) ai.seed else state.seed,
                subSeed = if (hasDiffusionParameters) ai.subSeed else state.subSeed,
                subSeedStrength = if (hasDiffusionParameters) ai.subSeedStrength else state.subSeedStrength,
                samplingSteps = if (hasDiffusionParameters) ai.samplingSteps else state.samplingSteps,
                cfgScale = if (hasDiffusionParameters) ai.cfgScale else state.cfgScale,
                restoreFaces = if (hasDiffusionParameters) ai.restoreFaces else state.restoreFaces,
                denoisingStrength = if (hasDiffusionParameters) ai.denoisingStrength else state.denoisingStrength,
                selectedSampler = ai.sampler.takeIf(state.availableSamplers::contains)
                    ?: state.selectedSampler,
                error = null,
                message = null,
            )
        }
        persistGenerationParameters()
    }

    /**
     * Persists only the effective image-to-image sampling steps and guidance scale.
     * Unchanged values do not trigger preference writes.
     *
     * @author Dmitriy Moroz
     */
    private fun persistGenerationParameters() {
        val state = currentState
        if (preferenceManager.imageToImageSamplingSteps != state.samplingSteps) {
            preferenceManager.imageToImageSamplingSteps = state.samplingSteps
        }
        if (preferenceManager.imageToImageCfgScale != state.cfgScale) {
            preferenceManager.imageToImageCfgScale = state.cfgScale
        }
    }

    /**
     * Applies image-to-image payloads emitted by the shared restoration channel.
     * This collector does not clear the channel's pending payload.
     *
     * @author Dmitriy Moroz
     */

    private fun observeGenerationFormUpdate() {
        launch(dispatchersProvider.immediate) {
            generationFormUpdateEvent.observeImg2ImgForm().collect { payload ->
                if (payload is GenerationFormUpdateEvent.Payload.I2IForm) {
                    applyGenerationResult(payload)
                }
            }
        }
    }

    /**
     * Takes and clears any pending image-to-image transfer during initialization, then applies it.
     * This call does not wait for asynchronous configuration loading to finish.
     *
     * @author Dmitriy Moroz
     */

    private fun consumePendingGenerationFormUpdate() {
        generationFormUpdateEvent.consumeImg2ImgForm()?.let(::applyGenerationResult)
    }

    /**
     * Restores a transfer's metadata and replaces the form input with its selected saved image.
     *
     * @param payload Uses the saved input when requested and nonblank, otherwise the generated output.
     *
     * @author Dmitriy Moroz
     */

    private fun applyGenerationResult(payload: GenerationFormUpdateEvent.Payload.I2IForm) {
        applyGenerationResult(
            ai = payload.ai,
            imageBase64 = if (payload.inputImage && payload.ai.inputImage.isNotBlank()) {
                payload.ai.inputImage
            } else {
                payload.ai.image
            },
        )
    }

    /**
     * Updates visible progress dialogs from the shared provider status streams.
     * Running Easy Diffusion updates replace its communicating/generating modal; Horde status updates
     * communicating modals, while local-diffusion status updates generating modals outside Easy Diffusion.
     *
     * @author Dmitriy Moroz
     */

    private fun observeGenerationProgress() {
        launch(dispatchersProvider.immediate) {
            remoteGenerationProgressGateway.observe().collect { progress ->
                updateFormState { state ->
                    val modal = state.screenModal
                    val active =
                        progress.lastOrNull { it.ownerId == null && it.source == state.mode }
                    if (modal is GenerationModal.Generating &&
                        state.mode in setOf(ServerSource.AUTOMATIC1111, ServerSource.SWARM_UI)
                    ) {
                        state.copy(
                            screenModal = modal.copy(
                                status = active?.percentage?.let { LocalDiffusionStatus(it, 100) }
                            ))
                    } else state
                }
            }
        }
        launch(dispatchersProvider.immediate) {
            observeEasyDiffusionProgressUseCase().collect { progress ->
                updateFormState { state ->
                    if (state.mode == ServerSource.EASY_DIFFUSION && progress.isRunning &&
                        (state.screenModal is GenerationModal.Communicating || state.screenModal is GenerationModal.Generating)
                    ) {
                        state.copy(screenModal = progress.toGenerationModal())
                    } else state
                }
            }
        }
        launch(dispatchersProvider.immediate) {
            observeHordeProcessStatusUseCase().collect { status ->
                updateFormState { state ->
                    val modal = state.screenModal
                    if (modal is GenerationModal.Communicating) {
                        state.copy(screenModal = modal.copy(hordeProcessStatus = status))
                    } else {
                        state
                    }
                }
            }
        }
        launch(dispatchersProvider.immediate) {
            observeCoreMlProcessStatusUseCase().collect { status ->
                updateFormState { state ->
                    val modal = state.screenModal
                    if (modal is GenerationModal.Generating && state.mode == ServerSource.LOCAL_APPLE_CORE_ML)
                        state.copy(screenModal = modal.copy(status = status))
                    else state
                }
            }
        }
        launch(dispatchersProvider.immediate) {
            observeLocalDiffusionProcessStatusUseCase().collect { status ->
                updateFormState { state ->
                    val modal = state.screenModal
                    if (modal is GenerationModal.Generating && state.mode == ServerSource.LOCAL_MICROSOFT_ONNX) {
                        state.copy(screenModal = modal.copy(status = status))
                    } else {
                        state
                    }
                }
            }
        }
    }

    /**
     * Loads the configured source and reconciles the form with currently available metadata.
     * Publishes loading completion or an inline configuration error; metadata discovery is driven
     * separately by the settings observer.
     *
     * @author Dmitriy Moroz
     */
    private fun loadConfiguration() {
        updateFormState { it.copy(loadingConfiguration = true, error = null) }
        launch(dispatchersProvider.io) {
            runCatching { getConfigurationUseCase() }
                .onSuccess { configuration ->
                    withContext(dispatchersProvider.immediate) {
                        updateFormState {
                            it.copy(
                                loadingConfiguration = false,
                            ).withSource(
                                source = configuration.source,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateFormState {
                            it.copy(
                                loadingConfiguration = false,
                                error = t.message ?: "Unable to load configuration",
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Reconciles preference emissions with the form and triggers provider-specific metadata refreshes.
     * Preference-stream failures publish an inline error and are forwarded to the error sink.
     *
     * @author Dmitriy Moroz
     */
    private fun observeSettings() {
        launch(dispatchersProvider.immediate) {
            preferenceManager
                .observe()
                .catch { t ->
                    updateFormState {
                        it.copy(
                            error = t.message ?: Localization.string("error_invalid")
                        )
                    }
                    onError(t)
                }
                .collect { settings ->
                    refreshStableDiffusionMetadataIfNeeded(settings)
                    refreshArliAiModelsIfNeeded(settings)
                    updateFormState { state ->
                        state.withSettings(
                            settings = settings,
                            stableDiffusionSamplers = stableDiffusionSamplers,
                            forgeModules = forgeModules,
                            aDetailerAvailable = aDetailerAvailable,
                            arliAiModels = arliAiModels,
                        )
                    }
                }
        }
    }

    /**
     * Tracks the AUTOMATIC1111 server/demo key and starts metadata discovery when that key changes.
     * Switching away cancels the sampler job and invalidates metadata keys. A changed server clears
     * cached choices before starting sampler, Forge-module and ADetailer availability loads.
     *
     * @param settings Provider selection, server address and demo mode identifying the metadata source.
     *
     * @author Dmitriy Moroz
     */
    private fun refreshStableDiffusionMetadataIfNeeded(settings: Settings) {
        if (settings.source != ServerSource.AUTOMATIC1111) {
            samplersLoadJob?.cancel()
            updateFormState { it.copy(isLoadingSamplers = false, hasSamplerLoadFailed = false) }
            stableDiffusionSamplersKey = null
            forgeModulesKey = null
            forgeModules = null
            aDetailerAvailabilityKey = null
            aDetailerAvailable = null
            return
        }
        val key = StableDiffusionSamplersKey(
            serverUrl = settings.serverUrl,
            demoMode = settings.demoMode,
        )
        if (
            stableDiffusionSamplersKey == key &&
            forgeModulesKey == key &&
            aDetailerAvailabilityKey == key
        ) return

        stableDiffusionSamplersKey = key
        samplersLoadJob?.cancel()
        forgeModulesKey = key
        aDetailerAvailabilityKey = key
        stableDiffusionSamplers = emptyList()
        forgeModules = emptyList()
        aDetailerAvailable = null
        updateFormState {
            it.copy(isLoadingSamplers = false, hasSamplerLoadFailed = false).withSource(
                source = ServerSource.AUTOMATIC1111,
                stableDiffusionSamplers = stableDiffusionSamplers,
                forgeModules = forgeModules,
                aDetailerAvailable = aDetailerAvailable,
                arliAiModels = arliAiModels,
            )
        }
        loadSamplers()
        loadForgeModules()
        loadADetailerAvailability()
    }

    /**
     * Refreshes sampler choices for the current AUTOMATIC1111 key unless another sampler load is active.
     * Failed or empty results expose retry. Cancellation and server-key checks prevent a superseded
     * sampler attempt from publishing into a different server's form.
     *
     * @author Dmitriy Moroz
     */
    private fun loadSamplers() {
        val key = stableDiffusionSamplersKey ?: return
        if (currentState.isLoadingSamplers) return
        updateFormState { it.copy(isLoadingSamplers = true, hasSamplerLoadFailed = false) }
        samplersLoadJob = launch(dispatchersProvider.immediate) {
            try {
                val samplers = withContext(dispatchersProvider.io) {
                    getStableDiffusionSamplersUseCase().map(StableDiffusionSampler::name)
                        .filter(String::isNotBlank)
                        .distinct()
                }
                ensureActive()
                if (stableDiffusionSamplersKey != key) return@launch
                stableDiffusionSamplers = samplers
                updateFormState {
                    it.copy(
                        isLoadingSamplers = false,
                        hasSamplerLoadFailed = samplers.isEmpty(),
                    ).withSource(
                        source = it.mode,
                        stableDiffusionSamplers = samplers,
                        forgeModules = forgeModules,
                        aDetailerAvailable = aDetailerAvailable,
                        arliAiModels = arliAiModels,
                    )
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                if (stableDiffusionSamplersKey != key) return@launch
                updateFormState { it.copy(isLoadingSamplers = false, hasSamplerLoadFailed = true) }
                onError(error)
            }
        }
    }

    /**
     * Loads Forge choices and reconciles the form with them; failures reconcile an empty catalog.
     * Loading failures are also forwarded to the error sink.
     *
     * @author Dmitriy Moroz
     */
    private fun loadForgeModules() {
        launch(dispatchersProvider.io) {
            runCatching { getForgeModulesUseCase() }
                .onSuccess { modules ->
                    forgeModules = modules
                    withContext(dispatchersProvider.immediate) {
                        updateFormState {
                            it.withSource(
                                source = it.mode,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    forgeModules = emptyList()
                    withContext(dispatchersProvider.immediate) {
                        updateFormState {
                            it.withSource(
                                source = it.mode,
                                stableDiffusionSamplers = stableDiffusionSamplers,
                                forgeModules = forgeModules,
                                aDetailerAvailable = aDetailerAvailable,
                                arliAiModels = arliAiModels,
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Checks ADetailer availability and publishes the loading flag plus the resolved result.
     * A completed in-memory result avoids another check unless forced; failure marks the extension
     * unavailable and is forwarded to the error sink.
     *
     * @param force Whether to ignore an already resolved in-memory availability result.
     *
     * @author Dmitriy Moroz
     */
    private fun loadADetailerAvailability(force: Boolean = false) {
        if (!force && aDetailerAvailable != null) return
        updateFormState { it.copy(aDetailerRefreshing = true) }
        launch(dispatchersProvider.io) {
            runCatching { isADetailerAvailableUseCase() }
                .onSuccess { available ->
                    aDetailerAvailable = available
                    withContext(dispatchersProvider.immediate) {
                        updateFormState {
                            it.copy(
                                aDetailerAvailable = available,
                                aDetailerRefreshing = false,
                                error = null,
                            )
                        }
                    }
                }
                .onFailure { t ->
                    aDetailerAvailable = false
                    withContext(dispatchersProvider.immediate) {
                        updateFormState { state ->
                            state.copy(
                                aDetailerAvailable = false,
                                aDetailerRefreshing = false,
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    /**
     * Starts ArliAI model discovery when its active API-key identity changes.
     * Leaving ArliAI invalidates the key so returning to it triggers another load.
     *
     * @param settings Persisted provider selection used to decide whether ArliAI discovery applies.
     *
     * @author Dmitriy Moroz
     */
    private fun refreshArliAiModelsIfNeeded(settings: Settings) {
        if (settings.source != ServerSource.ARLI_AI) {
            arliAiModelsKey = null
            return
        }
        val key = preferenceManager.arliAiApiKey
        if (arliAiModelsKey == key) return

        arliAiModelsKey = key
        arliAiModels = emptyList()
        updateFormState {
            it.withSource(
                source = ServerSource.ARLI_AI,
                stableDiffusionSamplers = stableDiffusionSamplers,
                forgeModules = forgeModules,
                aDetailerAvailable = aDetailerAvailable,
                arliAiModels = arliAiModels,
            )
        }
        loadArliAiModels()
    }

    /**
     * Loads distinct nonblank ArliAI checkpoint names and reconciles the preferred model with them.
     * Failure publishes an inline error and is forwarded to the error sink.
     *
     * @author Dmitriy Moroz
     */
    private fun loadArliAiModels() {
        launch(dispatchersProvider.io) {
            runCatching {
                fetchAndGetArliAiModelsUseCase()
                    .map(StableDiffusionModel::arliAiCheckpointName)
                    .filter(String::isNotBlank)
                    .distinct()
            }
                .onSuccess { models ->
                    arliAiModels = models
                    withContext(dispatchersProvider.immediate) {
                        updateFormState {
                            it.copy(arliAiModel = preferenceManager.arliAiModel)
                                .withSource(
                                    source = it.mode,
                                    stableDiffusionSamplers = stableDiffusionSamplers,
                                    forgeModules = forgeModules,
                                    aDetailerAvailable = aDetailerAvailable,
                                    arliAiModels = arliAiModels,
                                )
                        }
                    }
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateFormState { it.copy(error = t.localizedMessageString()) }
                    }
                    onError(t)
                }
        }
    }
}

private val StableDiffusionModel.arliAiCheckpointName: String
    get() = title.ifBlank { modelName }.ifBlank { filename }
