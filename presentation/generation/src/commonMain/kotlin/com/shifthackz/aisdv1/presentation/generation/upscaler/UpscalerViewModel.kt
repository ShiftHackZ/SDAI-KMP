package com.shifthackz.aisdv1.presentation.generation.upscaler

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.time.TimeProvider
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.MviEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.navigation.router.UpscalerRouter
import com.shifthackz.aisdv1.domain.entity.MAX_UPSCALER_INPUT_SIZE
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.gateway.GenerationRestoreGate
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.gateway.UpscalerGateway
import com.shifthackz.aisdv1.domain.interactor.wakelock.WakeLockInterActor
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.FinalizeGenerationUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetRandomImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.UpscaleImageUseCase
import com.shifthackz.aisdv1.presentation.components.core.GenerationPlatformServices
import com.shifthackz.aisdv1.domain.usecase.generation.ReadUpscalerImageUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.GetUpscalerOutputSizeUseCase
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImagePickResult
import com.shifthackz.aisdv1.presentation.generation.img2img.ImageToImagePlatformActions
import com.shifthackz.aisdv1.presentation.generation.model.GenerationModal
import com.shifthackz.aisdv1.presentation.generation.txt2img.localizedMessageText
import kotlin.io.encoding.Base64
import kotlin.time.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

/**
 * Owns standalone input, local progress and the normal generation result/save policy.
 *
 * @param diagnostics Bounded screen observer.
 * @param dispatchers Platform IO and UI dispatchers.
 * @param readImage Reads original input dimensions without altering selected pixels.
 * @param outputSize Predicts output dimensions from selected input and scale without changing pixels.
 * @param upscale Runs dedicated super-resolution without persistence.
 * @param finalize Applies the same autosave/media export policy as generation.
 * @param save Manual Gallery persistence offered by the common result dialog.
 * @param cache Retains unsaved results for normal detail/report navigation.
 * @param randomImage Loads one reference through the application's existing acquisition flow.
 * @param time Supplies creation timestamps.
 * @param restoreGate Serializes image operations against restoration.
 * @param picker Composition-bound photo/camera actions.
 * @param router Configuration and result navigation owner.
 * @param buildInfo Platform artwork and report availability.
 * @param gateway Observes the active platform upscaler's lifecycle stages.
 * @param preferences Current autosave policy used by the common result modal.
 * @param platformServices Presents the same foreground success/failure feedback as generation.
 * @param wakeLock Keeps the device awake using the generation policy during local inference.
 * @author Dmitriy Moroz
 */
class UpscalerViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchers: DispatchersProvider,
    private val readImage: ReadUpscalerImageUseCase,
    private val outputSize: GetUpscalerOutputSizeUseCase,
    private val upscale: UpscaleImageUseCase,
    private val finalize: FinalizeGenerationUseCase,
    private val save: SaveGenerationResultUseCase,
    private val cache: SaveLastResultToCacheUseCase,
    private val randomImage: GetRandomImageUseCase,
    private val time: TimeProvider,
    private val restoreGate: GenerationRestoreGate,
    private val picker: ImageToImagePlatformActions,
    private val router: UpscalerRouter,
    private val buildInfo: BuildInfoProvider,
    gateway: UpscalerGateway,
    private val preferences: PreferenceManager,
    private val wakeLock: WakeLockInterActor,
    private val platformServices: GenerationPlatformServices,
) : BaseMviViewModel<UpscalerState, UpscalerIntent, MviEffect>(
    diagnostics,
    NavigationDestination.UPSCALER.asDiagnosticScreen(),
    UpscalerState(
        platform = buildInfo.platform,
        isSupported = UpscalerSource.availableOn(buildInfo.platform).isNotEmpty()
    ),
    dispatchers.immediate
) {
    private var operation: Job? = null
    private var resultAction: Job? = null

    init {
        launch(dispatchers.immediate) {
            gateway.observeStage().collect { stage ->
                updateState { state ->
                    val modal = state.screenModal
                    if (state.isRunning && modal is GenerationModal.Generating)
                        state.copy(screenModal = modal.copy(nativeStage = stage))
                    else state
                }
            }
        }
    }

    /**
     * Dispatches one input, inference or shared-modal action while retaining captured image bytes.
     *
     * @param intent Image acquisition, inference, configuration or result action.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: UpscalerIntent) {
        if (currentState.hasDownscaleWarning && intent != UpscalerIntent.ConfirmDownscale &&
            intent != UpscalerIntent.DismissModal
        ) return
        if (currentState.isRunning && intent != UpscalerIntent.Cancel) return
        when (intent) {
            UpscalerIntent.OpenDrawer -> router.openDrawer()
            UpscalerIntent.Configure -> router.navigateToUpscalerSetup()
            UpscalerIntent.DismissModal -> updateState {
                it.copy(
                    screenModal = GenerationModal.None,
                    hasDownscaleWarning = false
                )
            }

            UpscalerIntent.ClearImage -> if (!currentState.isPickingImage) updateState {
                it.copy(
                    image = "",
                    width = 0,
                    height = 0,
                    outputSize = null,
                    screenModal = GenerationModal.None
                )
            }

            is UpscalerIntent.ConfigureModel -> updateState {
                it.copy(
                    configuration = intent.configuration,
                    outputSize = outputSize(it.width, it.height, intent.configuration)
                )
            }

            is UpscalerIntent.SelectImage -> acquire { intent.image }
            is UpscalerIntent.PickImage -> acquire {
                when (val result =
                    withContext(dispatchers.immediate) {
                        picker.pickImage(
                            intent.source,
                            shouldCropToSquare = false
                        )
                    }) {
                    is ImageToImagePickResult.Selected -> result.base64
                    ImageToImagePickResult.Cancelled -> null
                    ImageToImagePickResult.Unsupported -> error(Localization.string("upscaler_unavailable"))
                    is ImageToImagePickResult.Failed -> error(result.message)
                }
            }

            UpscalerIntent.RandomImage -> acquire { Base64.encode(randomImage()) }
            UpscalerIntent.Cancel -> operation?.cancel()
            UpscalerIntent.Run -> runUpscale()
            UpscalerIntent.ConfirmDownscale -> if (currentState.hasDownscaleWarning) runUpscale(
                shouldAllowDownscale = true
            )

            is UpscalerIntent.SaveResults -> performResultAction {
                intent.results.forEach { save(it) }
                withContext(dispatchers.immediate) { updateState { it.copy(screenModal = GenerationModal.None) } }
            }

            is UpscalerIntent.ViewResult -> performResultAction {
                val result = cache(intent.result)
                withContext(dispatchers.immediate) {
                    updateState { it.copy(screenModal = GenerationModal.None) }
                    router.navigateToGalleryDetails(result.id)
                }
            }

            is UpscalerIntent.ReportResult -> performResultAction {
                val result = cache(intent.result)
                withContext(dispatchers.immediate) {
                    updateState { it.copy(screenModal = GenerationModal.None) }
                    router.navigateToReportImage(result.id)
                }
            }
        }
    }

    /**
     * Retains one acquisition until validated original bytes are available; overlapping clicks are ignored.
     *
     * @param load Native or network selection; null means the user cancelled.
     * @author Dmitriy Moroz
     */
    private fun acquire(load: suspend () -> String?) {
        if (currentState.isPickingImage) return
        updateState { it.copy(isPickingImage = true, screenModal = GenerationModal.None) }
        operation = launch(dispatchers.io) {
            try {
                val encoded = load() ?: return@launch
                val prepared = readImage(encoded)
                updateState {
                    it.copy(
                        image = prepared.image,
                        width = prepared.width,
                        height = prepared.height,
                        outputSize = outputSize(prepared.width, prepared.height, it.configuration)
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                updateState { it.copy(screenModal = GenerationModal.Error(failure.localizedMessageText())) }
            } finally {
                updateState { it.copy(isPickingImage = false) }
            }
        }
    }

    /**
     * Captures input once, runs native inference, then publishes the common result modal after finalization.
     *
     * @param shouldAllowDownscale Whether the visible size warning was explicitly accepted.
     * @author Dmitriy Moroz
     */
    private fun runUpscale(shouldAllowDownscale: Boolean = false) {
        val snapshot = currentState
        val configuration = snapshot.configuration?.takeIf { it.modelId.isNotBlank() } ?: return
        if (snapshot.image.isBlank() || snapshot.isPickingImage) return
        if (!shouldAllowDownscale &&
            (snapshot.width > MAX_UPSCALER_INPUT_SIZE || snapshot.height > MAX_UPSCALER_INPUT_SIZE)
        ) {
            updateState { it.copy(hasDownscaleWarning = true) }
            return
        }
        val started = TimeSource.Monotonic.markNow()
        updateState {
            it.copy(
                isRunning = true, generationStartedAt = started, hasDownscaleWarning = false,
                screenModal = GenerationModal.Generating(
                    title = Localization.string("upscaler_running").asUiText(),
                    canCancel = true, nativeEngine = NativeGenerationEngine.UPSCALER,
                )
            )
        }
        operation = launch(dispatchers.io) {
            try {
                wakeLock.acquireWakelockUseCase()
                val result = restoreGate.generating {
                    val original = AiGenerationResult(
                        id = 0,
                        image = snapshot.image,
                        inputImage = snapshot.image,
                        createdAt = time.currentTimeMillis(),
                        type = AiGenerationResult.Type.UPSCALE,
                        prompt = "",
                        negativePrompt = "",
                        width = snapshot.width,
                        height = snapshot.height,
                        samplingSteps = 0,
                        cfgScale = 0f,
                        restoreFaces = false,
                        sampler = "",
                        seed = "",
                        subSeed = "",
                        subSeedStrength = 0f,
                        denoisingStrength = 0f,
                        hidden = false,
                        providerKey = configuration.source.key,
                        acceleration = "CPU"
                    )
                    finalize(
                        upscale(original, configuration).copy(
                            generationDurationMillis = started.elapsedNow().inWholeMilliseconds,
                        )
                    )
                }
                updateState {
                    it.copy(
                        screenModal = GenerationModal.Image.create(
                            listOf(result), autoSaveEnabled = preferences.autoSaveAiResults,
                            reportEnabled = buildInfo.type != BuildType.FOSS,
                        )
                    )
                }
                withContext(dispatchers.immediate) { platformServices.showGenerationSucceeded() }
            } catch (cancelled: CancellationException) {
                updateState { it.copy(screenModal = GenerationModal.None) }
                throw cancelled
            } catch (failure: Exception) {
                withContext(dispatchers.immediate) { platformServices.showGenerationFailed() }
                updateState { it.copy(screenModal = GenerationModal.Error(failure.localizedMessageText())) }
            } finally {
                wakeLock.releaseWakeLockUseCase()
                updateState { it.copy(isRunning = false) }
            }
        }
    }

    /**
     * Serializes result actions so repeated modal taps cannot duplicate Gallery writes.
     *
     * @param action Existing save/cache/navigation operation requested by the shared result dialog.
     * @author Dmitriy Moroz
     */
    private fun performResultAction(action: suspend () -> Unit) {
        if (resultAction?.isActive == true) return
        resultAction = launch(dispatchers.io) {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                updateState { it.copy(screenModal = GenerationModal.Error(failure.localizedMessageText())) }
            }
        }
    }
}
