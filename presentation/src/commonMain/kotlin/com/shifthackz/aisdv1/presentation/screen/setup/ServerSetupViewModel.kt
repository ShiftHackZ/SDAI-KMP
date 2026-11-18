package com.shifthackz.aisdv1.presentation.screen.setup

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTimeoutFailure
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToEasyDiffusionUseCase

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleHttpFailure
import com.shifthackz.aisdv1.presentation.model.localizedDescription
import com.shifthackz.aisdv1.presentation.model.observeOpenAiOAuthIdentity
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import com.shifthackz.aisdv1.presentation.screen.setup.model.catalogModels
import com.shifthackz.aisdv1.presentation.screen.setup.model.withVerifiedOwnedModel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.currentCoroutineContext
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.validation.common.CommonStringValidator
import com.shifthackz.aisdv1.core.validation.path.FilePathValidator
import com.shifthackz.aisdv1.core.validation.url.UrlValidator
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.openaioauth.OpenAiOAuthGateway
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthFailure
import com.shifthackz.aisdv1.domain.usecase.settings.RetryAuthorizationStorageUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ResetAuthorizationCredentialsUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.SetServerConfigurationUseCase
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCase
import com.shifthackz.aisdv1.presentation.screen.setup.model.localCatalogType
import com.shifthackz.aisdv1.presentation.screen.setup.model.withCatalogResult
import com.shifthackz.aisdv1.presentation.screen.setup.model.LocalCatalogStatus
import com.shifthackz.aisdv1.domain.usecase.downloadable.RefreshLocalModelCatalogUseCase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMnnUseCase
import com.shifthackz.aisdv1.domain.usecase.huggingface.FetchHuggingFaceModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToA1111UseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToArliAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToBonsaiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToCoreMlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToFalAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHordeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToHuggingFaceUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToLocalDiffusionUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMediaPipeUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiCompatibleUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToOpenAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSdxlUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToStabilityAiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToSwarmUiUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.presentation.screen.setup.model.HORDE_DEFAULT_API_KEY
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupEffect
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.model.toServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.platform.ServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.screen.setup.reducer.ServerSetupIntentProcessor
import com.shifthackz.aisdv1.presentation.screen.setup.validation.validateServerSetup
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.HUGGING_FACE_MODELS_TIMEOUT_MILLIS
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.setupAllowedModes
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageObserver
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * Owns provider setup state, validation, local model downloads, and final connection side effects.
 *
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 * @param launchSource Screen that opened setup, used to decide back navigation and completion route.
 * @param dispatchersProvider App coroutine dispatchers used by setup work.
 * @param buildInfoProvider Build metadata used to filter platform-supported local providers.
 * @param getCachedLocalModelCatalogUseCase Publishes existing downloaded/custom choices before remote catalog work.
 * @param refreshLocalModelCatalogUseCase Refreshes independent local catalogs and reports offline recovery explicitly.
 * @param getConfigurationUseCase Reads the current provider configuration.
 * @param connectToMnnUseCase Validates the selected bundle and commits its independent backend.
 * @param fetchHuggingFaceModelsUseCase Fetches Hugging Face model list for remote setup.
 * @param urlValidator Validates provider URL input.
 * @param stringValidator Validates text settings entered by the user.
 * @param filePathValidator Validates local file path settings.
 * @param connectToA1111UseCase Applies Automatic1111 configuration.
 * @param connectToSwarmUiUseCase Applies SwarmUI configuration.
 * @param connectToHordeUseCase Applies Horde configuration.
 * @param connectToHuggingFaceUseCase Applies Hugging Face configuration.
 * @param connectToLocalDiffusionUseCase Applies ONNX local diffusion configuration.
 * @param connectToMediaPipeUseCase Applies MediaPipe local diffusion configuration.
 * @param connectToSdxlUseCase Applies SDXL local diffusion configuration.
 * @param connectToCoreMlUseCase Applies Core ML local diffusion configuration.
 * @param connectToBonsaiUseCase Publishes the selected Bonsai model and independent local runtime configuration.
 * @param connectToOpenAiCompatibleUseCase Validates and applies independent image-server configuration.
 * @param connectToEasyDiffusionUseCase Validates candidate readiness/catalog before atomically publishing an anonymous connection.
 * @param connectToOpenAiUseCase Applies OpenAI configuration.
 * @param connectToStabilityAiUseCase Applies Stability AI configuration.
 * @param connectToFalAiUseCase Applies Fal AI configuration.
 * @param connectToArliAiUseCase Validates ArliAI credentials and publishes the selected remote source.
 * @param downloadModelUseCase Downloads a selected local model.
 * @param deleteModelUseCase Deletes a selected local model.
 * @param downloadGuard Platform guard for model download prerequisites.
 * @param storageUsageObserver Invalidates Settings storage summaries after model changes.
 * @param linksProvider External links and demo mode URL used by setup.
 * @param preferenceManager Persists setup preferences.
 * @param router Setup navigation contract.
 * @param openAiOAuthGateway Device authorization and protected session lifecycle.
 * @param setServerConfigurationUseCase Commits the selected source after a completed login.
 * @param retryAuthorizationStorageUseCase Completes checked local persistence after an explicit retry.
 * @param resetAuthorizationCredentialsUseCase Resets only saved Basic Auth after dedicated confirmation.
 * @param onError Error callback forwarded to the app-level error handling pipeline.
 *
 * @author Dmitriy Moroz
 */
class ServerSetupViewModel(
    diagnostics: UiDiagnostics,
    private val launchSource: LaunchSource = LaunchSource.SPLASH,
    private val dispatchersProvider: DispatchersProvider,
    private val buildInfoProvider: BuildInfoProvider,
    private val getCachedLocalModelCatalogUseCase: GetCachedLocalModelCatalogUseCase,
    private val refreshLocalModelCatalogUseCase: RefreshLocalModelCatalogUseCase,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val connectToMnnUseCase: ConnectToMnnUseCase,
    private val fetchHuggingFaceModelsUseCase: FetchHuggingFaceModelsUseCase,
    private val urlValidator: UrlValidator,
    private val stringValidator: CommonStringValidator,
    private val filePathValidator: FilePathValidator,
    private val connectToA1111UseCase: ConnectToA1111UseCase,
    private val connectToSwarmUiUseCase: ConnectToSwarmUiUseCase,
    private val connectToHordeUseCase: ConnectToHordeUseCase,
    private val connectToHuggingFaceUseCase: ConnectToHuggingFaceUseCase,
    private val connectToLocalDiffusionUseCase: ConnectToLocalDiffusionUseCase,
    private val connectToMediaPipeUseCase: ConnectToMediaPipeUseCase,
    private val connectToSdxlUseCase: ConnectToSdxlUseCase,
    private val connectToCoreMlUseCase: ConnectToCoreMlUseCase,
    private val connectToBonsaiUseCase: ConnectToBonsaiUseCase,
    private val connectToOpenAiCompatibleUseCase: ConnectToOpenAiCompatibleUseCase,
    private val connectToEasyDiffusionUseCase: ConnectToEasyDiffusionUseCase,
    private val connectToOpenAiUseCase: ConnectToOpenAiUseCase,
    private val connectToStabilityAiUseCase: ConnectToStabilityAiUseCase,
    private val connectToFalAiUseCase: ConnectToFalAiUseCase,
    private val connectToArliAiUseCase: ConnectToArliAiUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val deleteModelUseCase: DeleteModelUseCase,
    private val downloadGuard: ServerSetupDownloadGuard,
    private val storageUsageObserver: StorageUsageObserver,
    private val linksProvider: LinksProvider,
    private val preferenceManager: PreferenceManager,
    private val router: ServerSetupRouter,
    private val openAiOAuthGateway: OpenAiOAuthGateway,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase,
    private val retryAuthorizationStorageUseCase: RetryAuthorizationStorageUseCase,
    private val resetAuthorizationCredentialsUseCase: ResetAuthorizationCredentialsUseCase,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<ServerSetupState, ServerSetupIntent, ServerSetupEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.SERVER_SETUP.asDiagnosticScreen(),
    initialState = ServerSetupState(
        showBackNavArrow = launchSource == LaunchSource.SETTINGS,
        platform = buildInfoProvider.platform,
        allowedModes = buildInfoProvider.setupAllowedModes(),
        demoModeUrl = linksProvider.demoModeUrl,
    ),
    effectDispatcher = dispatchersProvider.immediate,
) {

    private val downloadJobs =
        MutableStateFlow<Map<Pair<LocalAiModel.Type, String>, Job>>(emptyMap())
    private val downloadCancellationMutex = Mutex()
    private val catalogRefreshMutex = Mutex()
    private val refreshingCatalogs = mutableSetOf<LocalAiModel.Type>()
    private var oauthLoginJob: Job? = null
    private var oauthAttempt = 0L

    init {
        launch(dispatchersProvider.io) {
            observeOpenAiOAuthIdentity(
                gateway = openAiOAuthGateway,
                onIdentity = { identity ->
                    updateState { it.copy(openAiOAuthAccount = identity?.accountId) }
                },
                onUnavailable = { error ->
                    updateState {
                        it.copy(
                            openAiOAuthStorageError = true,
                            modal = if (it.mode == ServerSource.OPEN_AI_OAUTH) ServerSetupState.Modal.Error(
                                error.localizedDescription()
                            ) else it.modal
                        )
                    }
                },
                onReady = { updateState { it.copy(openAiOAuthStorageError = false) } },
            )
        }
        launch(dispatchersProvider.io) {
            runCatching {
                val configuration = getConfigurationUseCase()
                val allowedModes = buildInfoProvider.setupAllowedModes()
                val models = runCatching {
                    withTimeout(HUGGING_FACE_MODELS_TIMEOUT_MILLIS.milliseconds) {
                        fetchHuggingFaceModelsUseCase()
                    }
                }
                    .onFailure(onError)
                    .getOrElse { HuggingFaceModel.supportedHfInferenceTextToImageModels }
                    .ifEmpty { HuggingFaceModel.supportedHfInferenceTextToImageModels }
                    .map(HuggingFaceModel::alias)
                configuration.toServerSetupState(
                    allowedModes = allowedModes,
                    platform = buildInfoProvider.platform,
                    huggingFaceModels = models,
                    localMnnModels = if (buildInfoProvider.type != BuildType.PLAY) listOf(
                        LocalAiModel.CustomMnn.copy(selected = configuration.localMnnModelId == LocalAiModel.CustomMnn.id)
                    ) else emptyList(),
                    localOnnxModels = if (buildInfoProvider.type != BuildType.PLAY) listOf(
                        LocalAiModel.CustomOnnx.copy(selected = configuration.localOnnxModelId == LocalAiModel.CustomOnnx.id)
                    ) else emptyList(),
                    localMediaPipeModels = if (buildInfoProvider.type != BuildType.PLAY) listOf(
                        LocalAiModel.CustomMediaPipe.copy(selected = configuration.localMediaPipeModelId == LocalAiModel.CustomMediaPipe.id)
                    ) else emptyList(),
                    localSdxlModels = if (buildInfoProvider.type != BuildType.PLAY) listOf(
                        LocalAiModel.CustomSdxl.copy(selected = configuration.localSdxlModelId == LocalAiModel.CustomSdxl.id)
                    ) else emptyList(),
                    localCoreMlModels = if (buildInfoProvider.type != BuildType.PLAY) listOf(
                        LocalAiModel.CustomCoreMl.copy(selected = configuration.localCoreMlModelId == LocalAiModel.CustomCoreMl.id)
                    ) else emptyList(),
                    localBonsaiModels = if (buildInfoProvider.type != BuildType.PLAY) listOf(
                        LocalAiModel.CustomBonsai.copy(selected = configuration.localBonsaiModelId == LocalAiModel.CustomBonsai.id)
                    ) else emptyList(),
                    allowLocalCustomModels = buildInfoProvider.type != BuildType.PLAY,
                    demoModeUrl = linksProvider.demoModeUrl,
                    showBackNavArrow = launchSource == LaunchSource.SETTINGS,
                )
            }
                .onSuccess { state ->
                    withContext(dispatchersProvider.immediate) {
                        emitState(
                            state.copy(
                                openAiOAuthAccount = currentState.openAiOAuthAccount,
                                openAiOAuthStorageError = currentState.openAiOAuthStorageError,
                            )
                        )
                    }
                    currentState.allowedModes.forEach(::refreshLocalCatalog)
                }
                .onFailure { t ->
                    withContext(dispatchersProvider.immediate) {
                        updateState { state ->
                            state.copy(
                                loadingConfiguration = false,
                                modal = ServerSetupState.Modal.Error(
                                    t.message ?: "Unable to load configuration",
                                ),
                            )
                        }
                    }
                    onError(t)
                }
        }
    }

    private val intentProcessor = ServerSetupIntentProcessor(
        router = router,
        linksProvider = linksProvider,
        currentState = { currentState },
        updateState = { reducer -> updateState(reducer) },
        emitEffect = ::emitEffect,
        localModelDownloadClickReducer = ::localModelDownloadClickReducer,
        deleteLocalModel = ::deleteLocalModel,
        download = ::download,
        validateAndConnectToServer = ::validateAndConnectToServer,
        connectToServer = ::connectToServer,
    )

    /**
     * Routes a user intent to its state transition or authorized side-effect handler.
     *
     * @param intent User event emitted by this screen’s production controls.
     *
     * @author Dmitriy Moroz
     */

    override fun processIntent(intent: ServerSetupIntent) {
        when (intent) {
            is ServerSetupIntent.RetryLocalCatalog -> {
                refreshLocalCatalog(intent.source); return
            }

            ServerSetupIntent.RetryAuthorizationStorage -> {
                recoverAuthorizationStorage(shouldReset = false); return
            }

            ServerSetupIntent.RequestAuthorizationReset -> {
                updateState { it.copy(isAuthorizationResetConfirmationVisible = true) }
                return
            }

            ServerSetupIntent.CancelAuthorizationReset -> {
                updateState { it.copy(isAuthorizationResetConfirmationVisible = false) }
                return
            }

            ServerSetupIntent.ConfirmAuthorizationReset -> {
                if (currentState.isAuthorizationResetConfirmationVisible) recoverAuthorizationStorage(
                    shouldReset = true
                )
                return
            }

            ServerSetupIntent.OAuthLogin -> {
                startOAuthLogin(); return
            }

            ServerSetupIntent.OAuthCancel -> {
                cancelOAuthLogin(); return
            }

            ServerSetupIntent.OAuthLogout -> {
                cancelOAuthLogin()
                launch(dispatchersProvider.io) {
                    try {
                        openAiOAuthGateway.logout()
                        updateState {
                            it.copy(
                                openAiOAuthAccount = null,
                                openAiOAuthStorageError = false
                            )
                        }
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: OpenAiOAuthFailure) {
                        updateState { it.copy(modal = ServerSetupState.Modal.Error(error.localizedDescription())) }
                    }
                }
                return
            }

            ServerSetupIntent.OAuthOpenBrowser -> {
                if (currentState.openAiOAuthCode != null) emitEffect(ServerSetupEffect.OpenUrl("https://auth.openai.com/codex/device"))
                return
            }

            is ServerSetupIntent.UpdateServerMode, ServerSetupIntent.NavigateBack -> cancelOAuthLogin()
            else -> Unit
        }
        if (intent == ServerSetupIntent.AcknowledgeUnverifiedConnection) {
            if (currentState.modal == ServerSetupState.Modal.ConnectionUnverified) finishConnection()
        } else {
            intentProcessor.process(intent)
        }
    }

    /**
     * Releases the factory-created setup ViewModel when its route leaves composition.
     * Pending authorization, protected identity initialization and subscriptions cannot outlive it.
     *
     * @author Dmitriy Moroz
     */
    fun onRouteDisposed() {
        cancelOAuthLogin()
        viewModelScope.cancel()
    }

    /**
     * Refreshes one allowed provider independently and coalesces concurrent retries for that source.
     *
     * @param source Provider owning the refresh; completion never changes the visible mode.
     *
     * @author Dmitriy Moroz
     */
    private fun refreshLocalCatalog(source: ServerSource) {
        val type = source.localCatalogType ?: return
        if (source !in currentState.allowedModes) return
        launch(dispatchersProvider.io) {
            val canRefresh = catalogRefreshMutex.withLock { refreshingCatalogs.add(type) }
            if (!canRefresh) return@launch
            updateState { it.copy(localCatalogStatuses = it.localCatalogStatuses + (type to LocalCatalogStatus.LOADING)) }
            try {
                val cached = getCachedLocalModelCatalogUseCase(type)
                currentCoroutineContext().ensureActive()
                updateState { it.withCatalogResult(type, cached, LocalCatalogStatus.LOADING) }
                val result = refreshLocalModelCatalogUseCase(type)
                currentCoroutineContext().ensureActive()
                updateState { state ->
                    state.withCatalogResult(
                        type,
                        result.models,
                        if (result.failure == null) LocalCatalogStatus.READY else LocalCatalogStatus.ERROR
                    )
                }
                result.failure?.let(onError)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                updateState { it.withCatalogResult(type, null, LocalCatalogStatus.ERROR) }
                onError(failure)
            } finally {
                catalogRefreshMutex.withLock { refreshingCatalogs.remove(type) }
            }
        }
    }

    /**
     * Validates active provider inputs and starts connection only when the form has no blocking errors.
     *
     * @author Dmitriy Moroz
     */
    private fun validateAndConnectToServer() {
        if (currentState.isAuthorizationStorageBusy) return
        val requiresAuthorization = currentState.mode == ServerSource.SWARM_UI ||
                currentState.mode == ServerSource.AUTOMATIC1111
        if (requiresAuthorization && currentState.authorizationStorageFailure != null) return
        if (!validate()) return
        connectToServer()
    }

    /**
     * Retries storage or completes explicitly confirmed recovery without discarding editable settings.
     *
     * @param shouldReset True only after the dedicated destructive-action confirmation.
     *
     * @author Dmitriy Moroz
     */
    private fun recoverAuthorizationStorage(shouldReset: Boolean) {
        if (currentState.isAuthorizationStorageBusy) return
        updateState {
            it.copy(
                isAuthorizationStorageBusy = true,
                isAuthorizationResetConfirmationVisible = false
            )
        }
        launch(dispatchersProvider.io) {
            try {
                if (shouldReset) resetAuthorizationCredentialsUseCase() else retryAuthorizationStorageUseCase()
                val configuration = getConfigurationUseCase()
                updateState {
                    if (configuration.authorizationStorageFailure == null) {
                        it.withCredentials(configuration.authCredentials)
                            .copy(authorizationStorageFailure = null)
                    } else {
                        it.copy(authorizationStorageFailure = configuration.authorizationStorageFailure)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: AuthorizationStorageFailure) {
                updateState { it.copy(authorizationStorageFailure = error.reason) }
            } finally {
                updateState { it.copy(isAuthorizationStorageBusy = false) }
            }
        }
    }

    /**
     * Invalidates pending UI callbacks, cancels polling and discards only the transient device code.
     * The previously committed protected session remains unchanged.
     *
     * @author Dmitriy Moroz
     */
    private fun cancelOAuthLogin() {
        oauthAttempt++
        oauthLoginJob?.cancel()
        oauthLoginJob = null
        updateState { it.copy(openAiOAuthBusy = false, openAiOAuthCode = null) }
    }

    /**
     * Starts one device authorization attempt for the active OAuth form and publishes safe UI state.
     * Superseded attempts cannot restore their code, account or error after route cancellation.
     *
     * @author Dmitriy Moroz
     */
    private fun startOAuthLogin() {
        if (currentState.mode != ServerSource.OPEN_AI_OAUTH || currentState.openAiOAuthBusy) return
        updateState { it.copy(openAiOAuthBusy = true, openAiOAuthCode = null) }
        val attempt = ++oauthAttempt
        oauthLoginJob = launch(dispatchersProvider.io) {
            try {
                val identity = openAiOAuthGateway.login { code, _ ->
                    if (attempt == oauthAttempt) updateState { it.copy(openAiOAuthCode = code) }
                }
                if (attempt == oauthAttempt) updateState {
                    it.copy(
                        openAiOAuthAccount = identity.accountId,
                        openAiOAuthStorageError = false
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: OpenAiOAuthFailure) {
                if (attempt == oauthAttempt) updateState {
                    it.copy(
                        openAiOAuthStorageError = it.openAiOAuthStorageError || error.category == "STORAGE",
                        modal = ServerSetupState.Modal.Error(error.localizedDescription()),
                    )
                }
            } finally {
                if (attempt == oauthAttempt) updateState {
                    it.copy(
                        openAiOAuthBusy = false,
                        openAiOAuthCode = null
                    )
                }
            }
        }
    }

    /**
     * Publishes current provider validation feedback and returns whether connection may proceed.
     *
     * @author Dmitriy Moroz
     */

    private fun validate(): Boolean {
        val result = currentState.validateServerSetup(
            urlValidator = urlValidator,
            stringValidator = stringValidator,
            filePathValidator = filePathValidator,
        )
        emitState(result.state)
        return result.isValid
    }

    /**
     * Dispatches the selected provider’s connection operation and reports loading or failure through setup state.
     *
     * @author Dmitriy Moroz
     */

    private fun connectToServer() {
        emitEffect(ServerSetupEffect.HideKeyboard)
        updateState { it.copy(modal = ServerSetupState.Modal.Communicating) }
        launch(dispatchersProvider.io) {
            var connectionUnverified = false
            val result = try {
                when (currentState.mode) {
                    ServerSource.AUTOMATIC1111 -> connectToAutomaticInstance()
                    ServerSource.SWARM_UI -> connectToSwarmUi()
                    ServerSource.EASY_DIFFUSION -> connectToEasyDiffusionUseCase(currentState.easyDiffusionUrl)
                    ServerSource.HORDE -> connectToHorde()
                    ServerSource.HUGGING_FACE -> connectToHuggingFace()
                    ServerSource.OPEN_AI_COMPATIBLE -> connectToOpenAiCompatibleUseCase(
                        url = currentState.openAiCompatibleUrl,
                        apiKey = currentState.openAiCompatibleApiKey,
                    ).map { outcome ->
                        connectionUnverified = outcome == OpenAiCompatibleConnection.UNVERIFIED
                    }

                    ServerSource.OPEN_AI -> connectToOpenAi()
                    ServerSource.OPEN_AI_OAUTH -> {
                        if (openAiOAuthGateway.currentIdentity() == null) throw OpenAiOAuthFailure("LOGIN_REQUIRED")
                        val previous = getConfigurationUseCase()
                        setServerConfigurationUseCase(
                            previous.copy(
                                source = ServerSource.OPEN_AI_OAUTH,
                                demoMode = false
                            )
                        )
                        Result.success(Unit)
                    }

                    ServerSource.STABILITY_AI -> connectToStabilityAi()
                    ServerSource.FAL_AI -> connectToFalAi()
                    ServerSource.ARLI_AI -> connectToArliAi()
                    ServerSource.LOCAL_MICROSOFT_ONNX -> connectToLocalDiffusion()
                    ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> connectToMediaPipe()
                    ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> connectToSdxl()
                    ServerSource.LOCAL_APPLE_CORE_ML -> connectToCoreMl()
                    ServerSource.LOCAL_MNN -> connectToMnnUseCase(
                        if (currentState.isLocalMnnCustomModel) LocalAiModel.CustomMnn.id
                        else currentState.localMnnModels.find { it.selected }?.id.orEmpty(),
                        currentState.localMnnCustomModelPath, preferenceManager.localMnnBackend
                    )

                    ServerSource.LOCAL_APPLE_BONSAI -> connectToBonsai()
                }
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                Result.failure(t)
            }
            withContext(dispatchersProvider.immediate) {
                result.fold(
                    onSuccess = {
                        if (connectionUnverified) {
                            updateState { it.copy(modal = ServerSetupState.Modal.ConnectionUnverified) }
                        } else {
                            finishConnection()
                        }
                    },
                    onFailure = { t ->
                        updateState { state ->
                            state.copy(
                                modal = ServerSetupState.Modal.Error(
                                    (if (t is EasyDiffusionTimeoutFailure) Localization.string("easy_diffusion_timeout") else null)
                                        ?: (t as? AuthorizationStorageFailure)?.localizedDescription()
                                        ?: (t as? OpenAiOAuthFailure)?.localizedDescription()
                                        ?: (t as? OpenAiCompatibleHttpFailure)?.localizedDescription()
                                        ?: t.message ?: Localization.string("error_invalid"),
                                ),
                            )
                        }
                        onError(t)
                    },
                )
            }
        }
    }

    /**
     * Clears the forced-setup flag, closes progress and navigates through post-setup configuration loading.
     *
     * @author Dmitriy Moroz
     */

    private fun finishConnection() {
        preferenceManager.forceSetupAfterUpdate = false
        updateState { it.copy(modal = ServerSetupState.Modal.None) }
        router.navigateToPostSetupConfigLoader()
    }

    /**
     * Applies the candidate AUTOMATIC1111 endpoint, demonstration option and HTTP authorization.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToAutomaticInstance(): Result<Unit> {
        val isDemo = currentState.demoMode
        val url = if (isDemo) linksProvider.demoModeUrl else currentState.serverUrl
        return connectToA1111UseCase(
            url = url,
            isDemo = isDemo,
            credentials = if (isDemo) AuthorizationCredentials.None else currentState.credentialsDomain(),
        )
    }

    /**
     * Applies the candidate SwarmUI endpoint and supported HTTP authorization.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToSwarmUi(): Result<Unit> = connectToSwarmUiUseCase(
        url = currentState.swarmUiUrl,
        credentials = currentState.credentialsDomain(),
    )

    /**
     * Applies the candidate AI Horde key or its explicitly selected anonymous default.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToHorde(): Result<Unit> = connectToHordeUseCase(
        apiKey = if (currentState.hordeDefaultApiKey) {
            HORDE_DEFAULT_API_KEY
        } else {
            currentState.hordeApiKey
        },
    )

    /**
     * Applies the Hugging Face token and supported inference model.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToHuggingFace(): Result<Unit> = connectToHuggingFaceUseCase(
        apiKey = currentState.huggingFaceApiKey,
        model = currentState.huggingFaceModel,
    )

    /**
     * Applies the official OpenAI API key independently of compatible servers and OAuth sessions.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToOpenAi(): Result<Unit> = connectToOpenAiUseCase(
        apiKey = currentState.openAiApiKey,
    )

    /**
     * Applies the candidate Stability AI API key and completes provider setup.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToStabilityAi(): Result<Unit> = connectToStabilityAiUseCase(
        apiKey = currentState.stabilityAiApiKey,
    )

    /**
     * Applies the candidate Fal.ai API key and completes provider setup.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToFalAi(): Result<Unit> = connectToFalAiUseCase(
        apiKey = currentState.falAiApiKey,
    )

    /**
     * Applies the candidate ArliAI API key and completes provider setup.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToArliAi(): Result<Unit> = connectToArliAiUseCase(
        apiKey = currentState.arliAiApiKey,
    )

    /**
     * Applies the selected downloaded or custom ONNX model to local generation.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToLocalDiffusion(): Result<Unit> = connectToLocalDiffusionUseCase(
        modelId = currentState.localOnnxModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localOnnxCustomModelPath,
    )

    /**
     * Applies the selected downloaded or custom MediaPipe model to local generation.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToMediaPipe(): Result<Unit> = connectToMediaPipeUseCase(
        modelId = currentState.localMediaPipeModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localMediaPipeCustomModelPath,
    )

    /**
     * Applies the selected downloaded or custom stable-diffusion.cpp model.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToSdxl(): Result<Unit> = connectToSdxlUseCase(
        modelId = currentState.localSdxlModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localSdxlCustomModelPath,
    )

    /**
     * Applies the selected downloaded or custom Core ML model.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToCoreMl(): Result<Unit> = connectToCoreMlUseCase(
        modelId = currentState.localCoreMlModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localCoreMlCustomModelPath,
    )

    /**
     * Applies the selected downloaded or custom Bonsai model.
     *
     * @author Dmitriy Moroz
     */

    private suspend fun connectToBonsai(): Result<Unit> {
        return connectToBonsaiUseCase(
            modelId = currentState.localBonsaiModels.find { it.selected }?.id.orEmpty(),
            modelPath = "",
        )
    }

    /**
     * Uses the latest owning-list item so a stale row cannot cancel a completed transfer.
     *
     * @param value Clicked model identity; its old progress/selection snapshot is not reused.
     *
     * @author Dmitriy Moroz
     */
    private fun localModelDownloadClickReducer(value: ServerSetupState.LocalModel) {
        val type = currentState.mode.localCatalogType ?: return
        val model = currentState.catalogModels(type).firstOrNull { it.id == value.id } ?: return
        when {
            model.downloadState is DownloadState.Downloading -> cancelDownload(model.id, type)
            model.downloaded -> updateState {
                it.copy(
                    modal = ServerSetupState.Modal.DeleteLocalModelConfirm(
                        model,
                        type
                    )
                )
            }

            else -> updateState {
                it.copy(
                    modal = ServerSetupState.Modal.SelectDownloadSource(
                        model.id,
                        type
                    )
                )
            }
        }
    }

    /**
     * Cancels and joins only the owning transfer before removing partial files; completed installs survive late Cancel.
     *
     * @param id Stable model identity whose active transfer is cancelled.
     * @param type Provider captured when the action was requested.
     *
     * @author Dmitriy Moroz
     */
    private fun cancelDownload(id: String, type: LocalAiModel.Type) {
        launch(dispatchersProvider.io) {
            try {
                downloadCancellationMutex.withLock {
                    val job = downloadJobs.value[type to id] ?: return@withLock
                    job.cancelAndJoin()
                    if (currentState.catalogModels(type)
                            .firstOrNull { it.id == id }?.downloaded == true
                    ) return@withLock
                    val owned = getCachedLocalModelCatalogUseCase(type).firstOrNull { it.id == id }
                    if (owned?.downloaded == true) {
                        updateState { it.withVerifiedOwnedModel(id, type) }
                        storageUsageObserver.notifyChanged()
                        return@withLock
                    }
                    deleteModelUseCase(id)
                    updateState { state ->
                        val current = state.catalogModels(type).firstOrNull { it.id == id }
                            ?: return@updateState state
                        state.withDeletedLocalModel(current, type)
                    }
                    storageUsageObserver.notifyChanged()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                onDownloadFailure(id, type, failure)
            }
        }
    }

    /**
     * Starts the confirmed source-owned model transfer once, independent of subsequent mode changes.
     *
     * @param modelId Stable model identifier associated with the chosen download.
     * @param url Chosen archive mirror.
     * @param type Provider captured when its source picker opened.
     *
     * @author Dmitriy Moroz
     */
    private fun download(modelId: String, url: String, type: LocalAiModel.Type) {
        val model = currentState.catalogModels(type).firstOrNull { it.id == modelId } ?: return
        if (model.downloaded || downloadJobs.value[type to modelId]?.isActive == true) return
        updateState { state ->
            val current = state.catalogModels(type).firstOrNull { it.id == modelId }
                ?: return@updateState state
            state.copy(modal = ServerSetupState.Modal.None)
                .withUpdatedLocalModel(
                    current.copy(downloadState = DownloadState.Downloading()),
                    type
                )
        }
        val job = scopeLaunchDownload(modelId, url, type)
        downloadJobs.update { it + ((type to modelId) to job) }
        job.start()
    }

    /**
     * Publishes progress into the captured provider using its current selection, never the download-start snapshot.
     *
     * @param id Stable model identity being transferred.
     * @param url Chosen archive source.
     * @param type Runtime owning every progress, completion and error callback.
     *
     * @author Dmitriy Moroz
     */
    private fun scopeLaunchDownload(id: String, url: String, type: LocalAiModel.Type): Job =
        launch(dispatchersProvider.io, start = CoroutineStart.LAZY) {
            try {
                downloadGuard.withDownload {
                    downloadModelUseCase(id, url).distinctUntilChanged().catch { failure ->
                        if (failure is CancellationException) throw failure
                        onDownloadFailure(id, type, failure)
                    }.collect { progress ->
                        currentCoroutineContext().ensureActive()
                        updateState { state ->
                            val current = state.catalogModels(type).firstOrNull { it.id == id }
                                ?: return@updateState state
                            state.withUpdatedLocalModel(
                                current.copy(
                                    downloadState = progress,
                                    downloaded = progress is DownloadState.Complete
                                ), type
                            )
                        }
                        if (progress is DownloadState.Complete) storageUsageObserver.notifyChanged()
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                onDownloadFailure(id, type, failure)
            } finally {
                val job = currentCoroutineContext()[Job]
                downloadJobs.update { jobs -> if (jobs[type to id] == job) jobs - (type to id) else jobs }
                if (currentCoroutineContext().isActive) updateState { state ->
                    val current = state.catalogModels(type).firstOrNull { it.id == id }
                        ?: return@updateState state
                    if (current.downloadState is DownloadState.Downloading) state.withUpdatedLocalModel(
                        current.copy(downloadState = DownloadState.Unknown),
                        type
                    ) else state
                }
            }
        }

    /**
     * Keeps an archive failure with its owning item and reports it through the existing error sink.
     *
     * @param id Stable model identity whose transfer failed.
     * @param type Provider that started the transfer.
     * @param failure Underlying download failure.
     *
     * @author Dmitriy Moroz
     */
    private fun onDownloadFailure(id: String, type: LocalAiModel.Type, failure: Throwable) {
        updateState { state ->
            val current =
                state.catalogModels(type).firstOrNull { it.id == id } ?: return@updateState state
            state.withUpdatedLocalModel(
                current.copy(downloadState = DownloadState.Error(failure)),
                type
            )
                .copy(
                    modal = ServerSetupState.Modal.Error(
                        failure.message ?: Localization.string("error_title")
                    )
                )
        }
        onError(failure)
    }

    /**
     * Removes a confirmed owned model and updates only its captured provider after successful deletion.
     *
     * @param id Stable identity of the confirmed model.
     * @param type Provider captured in the deletion confirmation.
     *
     * @author Dmitriy Moroz
     */
    private fun deleteLocalModel(id: String, type: LocalAiModel.Type) {
        updateState { it.copy(modal = ServerSetupState.Modal.None) }
        launch(dispatchersProvider.io) {
            try {
                downloadCancellationMutex.withLock {
                    downloadJobs.value[type to id]?.cancelAndJoin()
                    deleteModelUseCase(id)
                    updateState { state ->
                        val current = state.catalogModels(type).firstOrNull { it.id == id }
                            ?: return@updateState state
                        state.withDeletedLocalModel(current, type)
                    }
                    storageUsageObserver.notifyChanged()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                onError(failure)
            }
        }
    }
}
