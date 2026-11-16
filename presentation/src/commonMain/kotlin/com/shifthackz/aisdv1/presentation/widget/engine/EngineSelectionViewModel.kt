package com.shifthackz.aisdv1.presentation.widget.engine

import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.CaptureEasyDiffusionConnectionUseCase
import com.shifthackz.aisdv1.domain.usecase.easydiffusion.SelectEasyDiffusionModelUseCase

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import kotlinx.coroutines.CancellationException

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.GetEasyDiffusionModelsUseCase

import com.shifthackz.aisdv1.domain.usecase.easydiffusion.FetchEasyDiffusionModelsUseCase

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StabilityAiEngine
import com.shifthackz.aisdv1.domain.entity.StableDiffusionModel
import com.shifthackz.aisdv1.domain.entity.SwarmUiModel
import com.shifthackz.aisdv1.domain.feature.bonsai.BonsaiModelSupport
import com.shifthackz.aisdv1.domain.feature.coreml.CoreMlModelSupport
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.ObserveLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.FetchAndGetStabilityAiEnginesUseCase
import com.shifthackz.aisdv1.domain.usecase.swarmmodel.FetchAndGetSwarmUiModelsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Loads and persists provider-specific checkpoint choices; Easy Diffusion refresh failures remain visible with an explicit retry.
 *
 * @param dispatchersProvider Application dispatchers for background I/O and immediate UI state updates.
 * @param fetchAndGetSwarmUiModelsUseCase Existing SwarmUI checkpoint discovery operation.
 * @param observeLocalOnnxModelsUseCase Hot ONNX download and selection catalog.
 * @param observeLocalCoreMlModelsUseCase Hot Core ML download and selection catalog.
 * @param observeLocalBonsaiModelsUseCase Hot Bonsai download and selection catalog.
 * @param observeLocalSdxlModelsUseCase Hot stable-diffusion.cpp download and selection catalog.
 * @param fetchAndGetStabilityAiEnginesUseCase Existing Stability AI engine discovery.
 * @param getHuggingFaceModelsUseCase Supported Hugging Face inference model catalog.
 * @param preferenceManager Current provider selection and independent generation preferences.
 * @param fetchEasyDiffusionModelsUseCase Strict refresh of the selected Easy Diffusion instance checkpoint catalog.
 * @param getEasyDiffusionModelsUseCase Reads only the successfully refreshed selected-instance catalog.
 * @param captureEasyDiffusionConnectionUseCase Atomically captures the expected instance and revision before discovery.
 * @param selectEasyDiffusionModelUseCase Rejects selection events from a replaced catalog before persistence.
 * @param providerSelectionGuard Excludes connection replacement during catalog publication to the picker.
 * @param getConfigurationUseCase Reads every provider’s independent saved settings and the selected source revision.
 * @param selectStableDiffusionModelUseCase Updates the active AUTOMATIC1111 checkpoint on the server.
 * @param getStableDiffusionModelsUseCase Reads cached AUTOMATIC1111 checkpoints and selection.
 * @param onError Reports loading failures through the application error sink.
 * @param diagnostics Application UI diagnostics sink used by the normal view-model lifecycle.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EngineSelectionViewModel(
    diagnostics: UiDiagnostics,

    private val dispatchersProvider: DispatchersProvider,

    private val fetchAndGetSwarmUiModelsUseCase: FetchAndGetSwarmUiModelsUseCase,

    private val observeLocalOnnxModelsUseCase: ObserveLocalOnnxModelsUseCase,

    private val observeLocalCoreMlModelsUseCase: ObserveLocalCoreMlModelsUseCase,

    private val observeLocalBonsaiModelsUseCase: ObserveLocalBonsaiModelsUseCase,

    private val observeLocalSdxlModelsUseCase: ObserveLocalSdxlModelsUseCase,

    private val fetchAndGetStabilityAiEnginesUseCase: FetchAndGetStabilityAiEnginesUseCase,

    private val getHuggingFaceModelsUseCase: FetchHuggingFaceModelsUseCase,

    private val preferenceManager: PreferenceManager,
    private val fetchEasyDiffusionModelsUseCase: FetchEasyDiffusionModelsUseCase,
    private val getEasyDiffusionModelsUseCase: GetEasyDiffusionModelsUseCase,
    private val captureEasyDiffusionConnectionUseCase: CaptureEasyDiffusionConnectionUseCase,
    private val selectEasyDiffusionModelUseCase: SelectEasyDiffusionModelUseCase,
    private val providerSelectionGuard: ProviderSelectionGuard,

    private val getConfigurationUseCase: GetConfigurationUseCase,

    private val selectStableDiffusionModelUseCase: SelectStableDiffusionModelUseCase,

    private val getStableDiffusionModelsUseCase: GetStableDiffusionModelsUseCase,

    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<EngineSelectionState, EngineSelectionIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.ENGINE_SELECTION,
    initialState = EngineSelectionState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        /**
         * Provider snapshot associated with these model choices.
         *
         * @author Dmitriy Moroz
         */
        val configuration = preferenceManager
            .observe()
            .map { getConfigurationUseCase() }
            .catch {
                onError(it)
                emit(Configuration())
            }

        val localAiModels = observeLocalOnnxModelsUseCase()
            .map { models ->
                models.filter { it.downloaded || it.id == LocalAiModel.CustomOnnx.id }
            }
            .catch {
                onError(it)
                emit(emptyList())
            }
        val localSdxlModels = observeLocalSdxlModelsUseCase()
            .map { models ->
                models.filter(LocalAiModel::downloaded)
            }
            .catch {
                onError(it)
                emit(emptyList())
            }
        val localCoreMlModels = observeLocalCoreMlModelsUseCase()
            .map { models ->
                models
                    .filter(LocalAiModel::downloaded)
                    .filter(CoreMlModelSupport::isSupported)
            }
            .catch {
                onError(it)
                emit(emptyList())
            }
        val localBonsaiModels = observeLocalBonsaiModelsUseCase()
            .map { models ->
                models
                    .filter(LocalAiModel::downloaded)
                    .filter(BonsaiModelSupport::isSupported)
            }
            .catch {
                onError(it)
                emit(emptyList())
            }

        launch(dispatchersProvider.io) {
            configuration
                .combine(localAiModels) { config, localModels -> config to localModels }
                .combine(localSdxlModels) { (config, localModels), sdxlModels ->
                    Triple(config, localModels, sdxlModels)
                }
                .combine(localCoreMlModels) { (config, localModels, sdxlModels), coreMlModels ->
                    LocalModelOptions(config, localModels, sdxlModels, coreMlModels)
                }
                .combine(localBonsaiModels) { localModelOptions, bonsaiModels ->
                    val config = localModelOptions.config
                    val visibleLocalModels = when (config.source) {
                        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localModelOptions.sdxlModels
                        ServerSource.LOCAL_APPLE_CORE_ML -> localModelOptions.coreMlModels
                        ServerSource.LOCAL_APPLE_BONSAI -> bonsaiModels
                        else -> localModelOptions.onnxModels
                    }
                    config to visibleLocalModels
                }
                .catch { onError(it) }
                .collectLatest { (config, localModels) ->
                    updateState {
                        it.copy(
                            loading = true,
                            mode = config.source,
                            localAiModels = localModels,
                        )
                    }
                    val options = loadOptions(config)
                    publishCurrentOptions(options) {
                        updateState { state ->
                            state.copy(
                                loading = false,
                                mode = options.config.source,
                                sdModels = options.sdModels.map { it.first.title },
                                selectedSdModel = options.sdModels
                                    .firstOrNull { it.second }
                                    ?.first
                                    ?.title
                                    ?: state.selectedSdModel,
                                easyDiffusionModels = options.easyDiffusionModels,
                                selectedEasyDiffusionModel = if (options.config.source == ServerSource.EASY_DIFFUSION) preferenceManager.easyDiffusionModel else options.config.easyDiffusionModel,
                                easyDiffusionUrl = options.config.easyDiffusionUrl,
                                easyDiffusionSelectionRevision = options.config.providerSelectionRevision,
                                hasEasyDiffusionModelLoadFailed = options.hasEasyDiffusionModelLoadFailed,
                                swarmModels = options.swarmModels.map { it.name },
                                selectedSwarmModel = options.swarmModels
                                    .firstOrNull { it.name == options.config.swarmUiModel }
                                    ?.name
                                    ?: state.selectedSwarmModel,
                                hfModels = options.hfModels.map { it.alias },
                                selectedHfModel = options.config.huggingFaceModel.safeHuggingFaceModelAlias(),
                                stEngines = options.stEngines.map { it.id },
                                selectedStEngine = options.config.stabilityAiEngineId,
                                localAiModels = localModels,
                                selectedLocalAiModelId = localModels
                                    .firstOrNull { it.id == options.config.selectedLocalModelId() }
                                    ?.id
                                    ?: state.selectedLocalAiModelId,
                            )
                        }
                    }
                }
        }
    }

    /** Routes a user intent to its state transition or authorized side-effect handler.
     * @param intent User event emitted by this screen’s production controls.
     * @author Dmitriy Moroz */

    override fun processIntent(intent: EngineSelectionIntent) {
        if (currentState.mode == ServerSource.EASY_DIFFUSION) {
            val displayed = currentState
            launch(dispatchersProvider.io) {
                if (intent.shouldRefresh) {
                    val config = getConfigurationUseCase().copy(
                        source = ServerSource.EASY_DIFFUSION,
                        easyDiffusionUrl = displayed.easyDiffusionUrl,
                        providerSelectionRevision = displayed.easyDiffusionSelectionRevision,
                    )
                    publishCurrentOptions(RemoteOptions(config)) { updateState { it.copy(loading = true) } }
                    val options = loadEasyDiffusionOptions(RemoteOptions(config))
                    publishCurrentOptions(options) {
                        updateState {
                            it.copy(
                                loading = false, easyDiffusionModels = options.easyDiffusionModels,
                                selectedEasyDiffusionModel = preferenceManager.easyDiffusionModel,
                                hasEasyDiffusionModelLoadFailed = options.hasEasyDiffusionModelLoadFailed,
                            )
                        }
                    }
                } else if (displayed.easyDiffusionModels.any { it.id == intent.value }) {
                    if (selectEasyDiffusionModelUseCase(
                            intent.value,
                            displayed.easyDiffusionUrl,
                            displayed.easyDiffusionSelectionRevision
                        )
                    ) {
                        publishCurrentOptions(
                            RemoteOptions(
                                getConfigurationUseCase().copy(
                                    source = ServerSource.EASY_DIFFUSION,
                                    easyDiffusionUrl = displayed.easyDiffusionUrl,
                                )
                            )
                        ) { updateState { it.copy(selectedEasyDiffusionModel = preferenceManager.easyDiffusionModel) } }
                    }
                }
            }
            return
        }
        when (currentState.mode) {
            ServerSource.AUTOMATIC1111 -> selectA1111Model(intent.value)
            ServerSource.SWARM_UI -> preferenceManager.swarmUiModel = intent.value
            ServerSource.HUGGING_FACE -> preferenceManager.huggingFaceModel =
                intent.value.safeHuggingFaceModelAlias()

            ServerSource.STABILITY_AI -> preferenceManager.stabilityAiEngineId = intent.value
            ServerSource.LOCAL_MICROSOFT_ONNX -> preferenceManager.localOnnxModelId = intent.value
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> preferenceManager.localSdxlModelId =
                intent.value

            ServerSource.LOCAL_APPLE_CORE_ML -> preferenceManager.localCoreMlModelId = intent.value
            ServerSource.LOCAL_APPLE_BONSAI -> preferenceManager.localBonsaiModelId = intent.value
            else -> Unit
        }
    }

    /** Loads catalogs appropriate to one captured configuration without invoking unrelated providers.
     * @param config Captured provider configuration associated with these model choices.
     * @author Dmitriy Moroz */

    private suspend fun loadOptions(config: Configuration): RemoteOptions {
        val remoteOptions = RemoteOptions(config = config)
        return when (config.source) {
            ServerSource.AUTOMATIC1111 -> remoteOptions.copy(
                sdModels = if (config.hasA1111Endpoint()) {
                    loadList { getStableDiffusionModelsUseCase() }
                } else {
                    emptyList()
                },
            )

            ServerSource.EASY_DIFFUSION -> loadEasyDiffusionOptions(remoteOptions)

            ServerSource.SWARM_UI -> remoteOptions.copy(
                swarmModels = if (config.hasSwarmEndpoint()) {
                    loadList { fetchAndGetSwarmUiModelsUseCase() }
                } else {
                    emptyList()
                },
            )

            ServerSource.HUGGING_FACE -> remoteOptions.copy(
                hfModels = loadList { getHuggingFaceModelsUseCase() }
                    .ifEmpty { HuggingFaceModel.supportedHfInferenceTextToImageModels },
            )

            ServerSource.STABILITY_AI -> remoteOptions.copy(
                stEngines = if (config.stabilityAiApiKey.isBlank()) {
                    emptyList()
                } else {
                    loadList { fetchAndGetStabilityAiEnginesUseCase() }
                },
            )

            ServerSource.LOCAL_MICROSOFT_ONNX,
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE,
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP,
            ServerSource.LOCAL_APPLE_CORE_ML,
            ServerSource.LOCAL_APPLE_BONSAI,
            ServerSource.HORDE,
            ServerSource.OPEN_AI,
            ServerSource.OPEN_AI_COMPATIBLE,
            ServerSource.OPEN_AI_OAUTH,
            ServerSource.FAL_AI,
            ServerSource.ARLI_AI,
                -> remoteOptions
        }
    }

    /** Refreshes strict catalog operations and records failure for the explicit retry UI.
     * @param options Current provider snapshot and initially empty catalog.
     * @author Dmitriy Moroz */
    private suspend fun loadEasyDiffusionOptions(options: RemoteOptions): RemoteOptions = try {
        val connection = captureEasyDiffusionConnectionUseCase(
            options.config.providerSelectionRevision,
            options.config.easyDiffusionUrl
        )
        fetchEasyDiffusionModelsUseCase(connection.url)
        options.copy(
            config = options.config.copy(
                easyDiffusionUrl = connection.url,
                easyDiffusionModel = connection.model,
                providerSelectionRevision = connection.selectionRevision
            ),
            easyDiffusionModels = getEasyDiffusionModelsUseCase(connection.url),
        )
    } catch (failure: CancellationException) {
        throw failure
    } catch (failure: Exception) {
        onError(failure)
        options.copy(hasEasyDiffusionModelLoadFailed = true)
    }

    /** Publishes endpoint-scoped results only while the displayed connection still owns the picker.
     * @param options Captured configuration associated with the fetched choices or refresh state.
     * @param publish Immediate state update performed while provider replacement is excluded.
     * @author Dmitriy Moroz */
    private suspend fun publishCurrentOptions(options: RemoteOptions, publish: () -> Unit) {
        if (options.config.source != ServerSource.EASY_DIFFUSION) {
            publish(); return
        }
        providerSelectionGuard.withSelection {
            if (preferenceManager.source == ServerSource.EASY_DIFFUSION &&
                preferenceManager.easyDiffusionUrl == options.config.easyDiffusionUrl &&
                preferenceManager.providerSelectionRevision == options.config.providerSelectionRevision
            ) publish()
        }
    }

    /** Publishes checkpoint-switch loading state, changes the server model and reloads the selected catalog.
     * @param value Exact AUTOMATIC1111 checkpoint title selected by the picker.
     * @author Dmitriy Moroz */

    private fun selectA1111Model(value: String) {
        updateState {
            it.copy(
                loading = true,
                selectedSdModel = value,
            )
        }

        launch(dispatchersProvider.io) {
            runCatching {
                selectStableDiffusionModelUseCase(value)
                getStableDiffusionModelsUseCase()
            }
                .onFailure { throwable ->
                    onError(throwable)
                    updateState { it.copy(loading = false) }
                }
                .onSuccess { sdModels ->
                    updateState { state ->
                        state.copy(
                            loading = false,
                            sdModels = sdModels.map { it.first.title },
                            selectedSdModel = sdModels
                                .firstOrNull { it.second }
                                ?.first
                                ?.title
                                ?: state.selectedSdModel,
                        )
                    }
                }
        }
    }

    /** Bounds legacy remote discovery, reporting failures and returning an empty choice list for the picker.
     * @param block Legacy provider catalog request subject to the model picker's discovery timeout.
     * @author Dmitriy Moroz */

    private suspend fun <T> loadList(block: suspend () -> List<T>): List<T> = runCatching {
        withTimeoutOrNull(REMOTE_OPTIONS_TIMEOUT_MILLIS.milliseconds) {
            block()
        } ?: emptyList()
    }
        .onFailure(onError)
        .getOrElse { emptyList() }
}

/**
 * Existing non-Easy-Diffusion model-loading deadline in milliseconds.
 *
 * @author Dmitriy Moroz
 */
private const val REMOTE_OPTIONS_TIMEOUT_MILLIS = 5_000L

/**
 * Selects the persisted model identifier belonging to the active local engine.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.selectedLocalModelId(): String = when (source) {
    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localSdxlModelId
    ServerSource.LOCAL_APPLE_CORE_ML -> localCoreMlModelId
    ServerSource.LOCAL_APPLE_BONSAI -> localBonsaiModelId
    else -> localOnnxModelId
}

/** Local catalogs combined with one configuration snapshot before provider-specific choice loading.
 * @param config Captured provider configuration associated with these local or remote model choices.
 * @param onnxModels Current ONNX model download/selection catalog.
 * @param sdxlModels Current stable-diffusion.cpp model download/selection catalog.
 * @param coreMlModels Current Core ML model download/selection catalog.
 * @author Dmitriy Moroz */
private data class LocalModelOptions(
    val config: Configuration,
    val onnxModels: List<LocalAiModel>,
    val sdxlModels: List<LocalAiModel>,
    val coreMlModels: List<LocalAiModel>,
)

/**
 * Checks whether AUTOMATIC1111 has a configured non-loopback remote endpoint.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.hasA1111Endpoint(): Boolean = serverUrl.isMobileRemoteEndpoint()

/**
 * Checks whether SwarmUI has a configured non-loopback remote endpoint.
 *
 * @author Dmitriy Moroz
 */
private fun Configuration.hasSwarmEndpoint(): Boolean = swarmUiUrl.isMobileRemoteEndpoint()

/**
 * Checks that a saved endpoint is nonempty and does not point at the mobile device’s loopback interface.
 *
 * @author Dmitriy Moroz
 */
private fun String.isMobileRemoteEndpoint(): Boolean {
    val value = trim().lowercase()
    return value.isNotBlank() &&
            !value.startsWith("http://localhost") &&
            !value.startsWith("https://localhost") &&
            !value.startsWith("http://127.0.0.1") &&
            !value.startsWith("https://127.0.0.1") &&
            !value.startsWith("http://[::1]") &&
            !value.startsWith("https://[::1]")
}

/**
 * Retains a supported inference alias or selects the application’s supported default.
 *
 * @author Dmitriy Moroz
 */
private fun String.safeHuggingFaceModelAlias(): String =
    takeIf(HuggingFaceModel.supportedHfInferenceTextToImageAliases::contains)
        ?: HuggingFaceModel.default.alias

/**
 * Endpoint-specific catalogs loaded for one configuration snapshot.
 *
 * @param config Captured provider configuration associated with these local or remote model choices.
 * @param sdModels Available AUTOMATIC1111 checkpoint titles and selection state.
 * @param swarmModels Available SwarmUI checkpoint choices.
 * @param easyDiffusionModels Latest successfully fetched checkpoint catalog for the selected Easy Diffusion endpoint.
 * @param hasEasyDiffusionModelLoadFailed Whether the latest strict checkpoint refresh failed; the UI offers an explicit retry.
 * @param hfModels Supported Hugging Face model choices.
 * @param stEngines Available Stability AI engines.
 * @author Dmitriy Moroz
 */
private data class RemoteOptions(
    /**
     * Provider snapshot associated with these model choices.
     *
     * @author Dmitriy Moroz
     */
    val config: Configuration,
    /**
     * Available AUTOMATIC1111 checkpoint titles and selection state.
     *
     * @author Dmitriy Moroz
     */
    val sdModels: List<Pair<StableDiffusionModel, Boolean>> = emptyList(),
    /**
     * Available SwarmUI checkpoint choices.
     *
     * @author Dmitriy Moroz
     */
    val swarmModels: List<SwarmUiModel> = emptyList(),
    val easyDiffusionModels: List<EasyDiffusionModel> = emptyList(),
    val hasEasyDiffusionModelLoadFailed: Boolean = false,
    /**
     * Supported Hugging Face model choices.
     *
     * @author Dmitriy Moroz
     */
    val hfModels: List<HuggingFaceModel> = emptyList(),
    /**
     * Available Stability AI engines.
     *
     * @author Dmitriy Moroz
     */
    val stEngines: List<StabilityAiEngine> = emptyList(),
)
