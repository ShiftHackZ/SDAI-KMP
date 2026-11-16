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
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.validation.common.CommonStringValidator
import com.shifthackz.aisdv1.core.validation.path.FilePathValidator
import com.shifthackz.aisdv1.core.validation.url.UrlValidator
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.OpenAiCompatibleConnection
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
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
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
 * @param retryAuthorizationStorageUseCase Completes checked local persistence after an explicit retry.
 * @param resetAuthorizationCredentialsUseCase Resets only saved Basic Auth after dedicated confirmation.
 * @param launchSource Screen that opened setup, used to decide back navigation and completion route.
 * @param dispatchersProvider App coroutine dispatchers used by setup work.
 * @param buildInfoProvider Build metadata used to filter platform-supported local providers.
 * @param getConfigurationUseCase Reads the current provider configuration.
 * @param getLocalOnnxModelsUseCase Reads locally configured ONNX models.
 * @param getLocalMediaPipeModelsUseCase Reads locally configured MediaPipe models.
 * @param getLocalSdxlModelsUseCase Reads locally configured SDXL models.
 * @param getLocalCoreMlModelsUseCase Reads locally configured Core ML models on supported builds.
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
 * @param connectToOpenAiCompatibleUseCase Validates and applies independent image-server configuration.
 * @param connectToOpenAiUseCase Applies OpenAI configuration.
 * @param connectToStabilityAiUseCase Applies Stability AI configuration.
 * @param connectToFalAiUseCase Applies Fal AI configuration.
 * @param downloadModelUseCase Downloads a selected local model.
 * @param deleteModelUseCase Deletes a selected local model.
 * @param downloadGuard Platform guard for model download prerequisites.
 * @param storageUsageObserver Invalidates Settings storage summaries after model changes.
 * @param linksProvider External links and demo mode URL used by setup.
 * @param preferenceManager Persists setup preferences.
 * @param router Setup navigation contract.
 * @param openAiOAuthGateway Device authorization and protected session lifecycle.
 * @param setServerConfigurationUseCase Commits the selected source after a completed login.
 * @param onError Error callback forwarded to the app-level error handling pipeline.
 *
 * @param connectToEasyDiffusionUseCase Validates candidate readiness/catalog before atomically publishing an anonymous connection.
 * @param getLocalBonsaiModelsUseCase Reads downloaded and custom Bonsai models available to provider setup.
 * @param connectToBonsaiUseCase Publishes the selected Bonsai model and independent local runtime configuration.
 * @param connectToArliAiUseCase Validates ArliAI credentials and publishes the selected remote source.
 * @author Dmitriy Moroz
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 */
class ServerSetupViewModel(
    diagnostics: UiDiagnostics,
    private val launchSource: LaunchSource = LaunchSource.SPLASH,
    private val dispatchersProvider: DispatchersProvider,
    private val buildInfoProvider: BuildInfoProvider,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val getLocalOnnxModelsUseCase: GetLocalOnnxModelsUseCase,
    private val getLocalMediaPipeModelsUseCase: GetLocalMediaPipeModelsUseCase,
    private val getLocalSdxlModelsUseCase: GetLocalSdxlModelsUseCase,
    private val getLocalCoreMlModelsUseCase: GetLocalCoreMlModelsUseCase,
    private val getLocalBonsaiModelsUseCase: GetLocalBonsaiModelsUseCase,
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

    private val downloadJobs: MutableMap<String, Job> = mutableMapOf()
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
                val onnxModels = if (ServerSource.LOCAL_MICROSOFT_ONNX in allowedModes) {
                    getLocalOnnxModelsUseCase()
                } else {
                    emptyList()
                }
                val mediaPipeModels = if (ServerSource.LOCAL_GOOGLE_MEDIA_PIPE in allowedModes) {
                    getLocalMediaPipeModelsUseCase()
                } else {
                    emptyList()
                }
                val sdxlModels = if (ServerSource.LOCAL_STABLE_DIFFUSION_CPP in allowedModes) {
                    getLocalSdxlModelsUseCase()
                } else {
                    emptyList()
                }
                val coreMlModels = if (ServerSource.LOCAL_APPLE_CORE_ML in allowedModes) {
                    getLocalCoreMlModelsUseCase()
                } else {
                    emptyList()
                }
                val bonsaiModels = if (ServerSource.LOCAL_APPLE_BONSAI in allowedModes) {
                    getLocalBonsaiModelsUseCase()
                } else {
                    emptyList()
                }
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
                    localOnnxModels = onnxModels,
                    localMediaPipeModels = mediaPipeModels,
                    localSdxlModels = sdxlModels,
                    localCoreMlModels = coreMlModels,
                    localBonsaiModels = bonsaiModels,
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

    /** Routes a user intent to its state transition or authorized side-effect handler.
     * @param intent User event emitted by this screen’s production controls.
     * @author Dmitriy Moroz */

    override fun processIntent(intent: ServerSetupIntent) {
        when (intent) {
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

    /** Validates active provider inputs and starts connection only when the form has no blocking errors.
     * @author Dmitriy Moroz */

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

    /** Publishes current provider validation feedback and returns whether connection may proceed.
     * @author Dmitriy Moroz */

    private fun validate(): Boolean {
        val result = currentState.validateServerSetup(
            urlValidator = urlValidator,
            stringValidator = stringValidator,
            filePathValidator = filePathValidator,
        )
        emitState(result.state)
        return result.isValid
    }

    /** Dispatches the selected provider’s connection operation and reports loading or failure through setup state.
     * @author Dmitriy Moroz */

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

    /** Clears the forced-setup flag, closes progress and navigates through post-setup configuration loading.
     * @author Dmitriy Moroz */

    private fun finishConnection() {
        preferenceManager.forceSetupAfterUpdate = false
        updateState { it.copy(modal = ServerSetupState.Modal.None) }
        router.navigateToPostSetupConfigLoader()
    }

    /** Applies the candidate AUTOMATIC1111 endpoint, demonstration option and HTTP authorization.
     * @author Dmitriy Moroz */

    private suspend fun connectToAutomaticInstance(): Result<Unit> {
        val isDemo = currentState.demoMode
        val url = if (isDemo) linksProvider.demoModeUrl else currentState.serverUrl
        return connectToA1111UseCase(
            url = url,
            isDemo = isDemo,
            credentials = if (isDemo) AuthorizationCredentials.None else currentState.credentialsDomain(),
        )
    }

    /** Applies the candidate SwarmUI endpoint and supported HTTP authorization.
     * @author Dmitriy Moroz */

    private suspend fun connectToSwarmUi(): Result<Unit> = connectToSwarmUiUseCase(
        url = currentState.swarmUiUrl,
        credentials = currentState.credentialsDomain(),
    )

    /** Applies the candidate AI Horde key or its explicitly selected anonymous default.
     * @author Dmitriy Moroz */

    private suspend fun connectToHorde(): Result<Unit> = connectToHordeUseCase(
        apiKey = if (currentState.hordeDefaultApiKey) {
            HORDE_DEFAULT_API_KEY
        } else {
            currentState.hordeApiKey
        },
    )

    /** Applies the Hugging Face token and supported inference model.
     * @author Dmitriy Moroz */

    private suspend fun connectToHuggingFace(): Result<Unit> = connectToHuggingFaceUseCase(
        apiKey = currentState.huggingFaceApiKey,
        model = currentState.huggingFaceModel,
    )

    /** Applies the official OpenAI API key independently of compatible servers and OAuth sessions.
     * @author Dmitriy Moroz */

    private suspend fun connectToOpenAi(): Result<Unit> = connectToOpenAiUseCase(
        apiKey = currentState.openAiApiKey,
    )

    /** Applies the candidate Stability AI API key and completes provider setup.
     * @author Dmitriy Moroz */

    private suspend fun connectToStabilityAi(): Result<Unit> = connectToStabilityAiUseCase(
        apiKey = currentState.stabilityAiApiKey,
    )

    /** Applies the candidate Fal.ai API key and completes provider setup.
     * @author Dmitriy Moroz */

    private suspend fun connectToFalAi(): Result<Unit> = connectToFalAiUseCase(
        apiKey = currentState.falAiApiKey,
    )

    /** Applies the candidate ArliAI API key and completes provider setup.
     * @author Dmitriy Moroz */

    private suspend fun connectToArliAi(): Result<Unit> = connectToArliAiUseCase(
        apiKey = currentState.arliAiApiKey,
    )

    /** Applies the selected downloaded or custom ONNX model to local generation.
     * @author Dmitriy Moroz */

    private suspend fun connectToLocalDiffusion(): Result<Unit> = connectToLocalDiffusionUseCase(
        modelId = currentState.localOnnxModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localOnnxCustomModelPath,
    )

    /** Applies the selected downloaded or custom MediaPipe model to local generation.
     * @author Dmitriy Moroz */

    private suspend fun connectToMediaPipe(): Result<Unit> = connectToMediaPipeUseCase(
        modelId = currentState.localMediaPipeModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localMediaPipeCustomModelPath,
    )

    /** Applies the selected downloaded or custom stable-diffusion.cpp model.
     * @author Dmitriy Moroz */

    private suspend fun connectToSdxl(): Result<Unit> = connectToSdxlUseCase(
        modelId = currentState.localSdxlModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localSdxlCustomModelPath,
    )

    /** Applies the selected downloaded or custom Core ML model.
     * @author Dmitriy Moroz */

    private suspend fun connectToCoreMl(): Result<Unit> = connectToCoreMlUseCase(
        modelId = currentState.localCoreMlModels.find { it.selected }?.id.orEmpty(),
        modelPath = currentState.localCoreMlCustomModelPath,
    )

    /** Applies the selected downloaded or custom Bonsai model.
     * @author Dmitriy Moroz */

    private suspend fun connectToBonsai(): Result<Unit> {
        return connectToBonsaiUseCase(
            modelId = currentState.localBonsaiModels.find { it.selected }?.id.orEmpty(),
            modelPath = "",
        )
    }

    /** Opens the appropriate download-source, deletion or unsupported-model confirmation for a local catalog item.
     * @param value Clicked model and its current downloaded/source state.
     * @author Dmitriy Moroz */

    private fun localModelDownloadClickReducer(value: ServerSetupState.LocalModel) {

        /** Finds the active provider’s selected local model or fails when the selection is unavailable.
         * @author Dmitriy Moroz */
        fun localModel(): ServerSetupState.LocalModel =
            currentState.localModels.firstOrNull { it.id == value.id }
                ?.let { value.copy(selected = it.selected) }
                ?: value

        when {
            localModel().downloadState is DownloadState.Downloading -> {
                downloadJobs.remove(localModel().id)?.cancel()
                deleteLocalModel(localModel().id)
                updateState { state ->
                    state.withUpdatedLocalModel(
                        value = localModel().copy(downloadState = DownloadState.Unknown),
                    )
                }
            }

            localModel().downloaded -> updateState {
                it.copy(modal = ServerSetupState.Modal.DeleteLocalModelConfirm(localModel()))
            }

            else -> updateState {
                it.copy(modal = ServerSetupState.Modal.SelectDownloadSource(localModel().id))
            }
        }
    }

    /** Checks platform download prerequisites before launching the selected model archive download.
     * @param modelId Stable local model identifier associated with the chosen download.
     * @param url Chosen local model archive source.
     * @author Dmitriy Moroz */

    private fun download(modelId: String, url: String) {
        val localModel = currentState.localModels.firstOrNull { it.id == modelId } ?: return

        updateState { state ->
            state
                .copy(modal = ServerSetupState.Modal.None)
                .withUpdatedLocalModel(
                    localModel.copy(downloadState = DownloadState.Downloading()),
                )
        }

        val job = scopeLaunchDownload(localModel, url)
        downloadJobs[localModel.id] = job
        job.start()
    }

    /** Downloads the chosen archive in the view-model scope and publishes model/storage state changes.
     * @param localModel Local catalog item whose download state is being changed.
     * @param url Chosen local model archive source.
     * @author Dmitriy Moroz */

    private fun scopeLaunchDownload(
        localModel: ServerSetupState.LocalModel,
        url: String,
    ): Job =
        launch(dispatchersProvider.io, start = CoroutineStart.LAZY) {
            downloadGuard.withDownload {
                try {
                    downloadModelUseCase(localModel.id, url)
                        .distinctUntilChanged()
                        .catch { t ->
                            if (t is CancellationException) throw t
                            onDownloadFailure(localModel, t)
                        }
                        .collect { downloadState ->
                            if (downloadState is DownloadState.Complete) {
                                storageUsageObserver.notifyChanged()
                            }
                            updateState { state ->
                                state.withUpdatedLocalModel(
                                    localModel.copy(
                                        downloadState = downloadState,
                                        downloaded = downloadState is DownloadState.Complete,
                                    ),
                                )
                            }
                        }
                } finally {
                    downloadJobs.remove(localModel.id)
                }
            }
        }

    /** Updates the failed local download state and forwards the underlying failure to the application error sink.
     * @param localModel Local catalog item whose download state is being changed.
     * @param t Underlying generation or download failure.
     * @author Dmitriy Moroz */

    private fun onDownloadFailure(localModel: ServerSetupState.LocalModel, t: Throwable) {
        updateState { state ->
            state
                .withUpdatedLocalModel(
                    localModel.copy(downloadState = DownloadState.Error(t)),
                )
                .copy(
                    modal = ServerSetupState.Modal.Error(
                        t.message ?: Localization.string("error_title"),
                    ),
                )
        }
        onError(t)
    }

    /** Deletes a downloaded model and invalidates model/storage state for the active setup screen.
     * @param id Stable local model or custom-model identifier to select or delete.
     * @author Dmitriy Moroz */

    private fun deleteLocalModel(id: String) {
        launch(dispatchersProvider.io) {
            runCatching { deleteModelUseCase(id) }
                .onSuccess { storageUsageObserver.notifyChanged() }
                .onFailure(onError)
        }
    }

}
