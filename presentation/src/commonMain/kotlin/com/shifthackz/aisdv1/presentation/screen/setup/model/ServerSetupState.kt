package com.shifthackz.aisdv1.presentation.screen.setup.model

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Immutable UI state for the provider setup wizard.
 *
 * The first group of fields controls provider discovery and filtering, the middle group stores
 * per-provider form inputs, and the local model lists mirror downloadable model state for each
 * local runtime. Derived properties expose the active provider's local model selection.
 *
 * @param authorizationStorageFailure Safe failure retained separately from absent credentials.
 * @param isAuthorizationStorageBusy Whether explicit retry/reset is pending.
 * @param isAuthorizationResetConfirmationVisible Whether the user is confirming removal of saved Basic Auth.
 * @param openAiOAuthAccount Token-free account identifier, or null before initialization/sign-in.
 * @param openAiOAuthCode Transient device code displayed only during the current login attempt.
 * @param openAiOAuthBusy Whether device authorization is pending; disables conflicting actions.
 * @param openAiOAuthStorageError Whether protected storage requires recovery or an explicit local reset.
 * @param showBackNavArrow Whether this setup flow may return to its opening screen.
 * @param step Current provider-selection or provider-configuration stage.
 * @param mode Selected provider whose visible controls and generation dispatch apply.
 * @param platform Platform whose provider controls and local capabilities apply.
 * @param allowedModes Provider catalog permitted by the active build and setup launch flow.
 * @param sourceSearchQuery User-entered provider search text.
 * @param sourceTypeFilter Optional hosting-type restriction for provider discovery.
 * @param sourceReadinessFilters Readiness markers included by provider discovery filters.
 * @param sourceTagFilters Capability tags required by provider discovery filters.
 * @param sourceSortOrder Ordering applied to the filtered provider catalog.
 * @param allowLocalCustomModels Whether this setup flow permits selecting local filesystem paths.
 * @param modal Current setup progress, confirmation, filter or error dialog.
 * @param loadingConfiguration Whether saved provider controls are still being loaded.
 * @param serverUrl AUTOMATIC1111 server root used for metadata and generation.
 * @param swarmUiUrl SwarmUI server root, independent from other self-hosted connections.
 * @param easyDiffusionUrl Candidate or committed HTTP(S) Easy Diffusion instance root/prefix, according to its owning state.
 * @param easyDiffusionUrlValidationError Inline candidate Easy Diffusion URL feedback; null when valid.
 * @param hordeApiKey AI Horde credential; the anonymous key is supported independently of other services.
 * @param huggingFaceApiKey Hugging Face inference token.
 * @param openAiApiKey Official OpenAI API key; separate from compatible servers and OAuth sessions.
 * @param openAiCompatibleUrl Complete compatible-server API prefix, including any version and reverse-proxy path.
 * @param openAiCompatibleApiKey Optional bearer key for the independent compatible server.
 * @param openAiCompatibleUrlValidationError Inline compatible-server URL feedback; null when valid.
 * @param stabilityAiApiKey Stability AI API credential.
 * @param falAiApiKey Fal.ai API credential.
 * @param arliAiApiKey ArliAI API credential.
 * @param hordeDefaultApiKey Whether AI Horde uses its supported anonymous default key.
 * @param demoMode Whether AUTOMATIC1111 uses the configured demonstration endpoint.
 * @param demoModeUrl Configured AUTOMATIC1111 demonstration endpoint.
 * @param authType Selected anonymous or Basic Auth mode for supporting self-hosted providers.
 * @param login User-entered Basic Auth login.
 * @param password User-entered Basic Auth password.
 * @param huggingFaceModels Supported Hugging Face inference model choices.
 * @param huggingFaceModel Selected supported Hugging Face inference model alias.
 * @param localOnnxModels Current ONNX download/selection catalog for setup.
 * @param localOnnxCustomModel Whether setup currently selects a custom ONNX model path.
 * @param localOnnxCustomModelPath User-entered custom ONNX model filesystem path.
 * @param localMediaPipeModels Current MediaPipe download/selection catalog for setup.
 * @param localMediaPipeCustomModel Whether setup currently selects a custom MediaPipe model path.
 * @param localMediaPipeCustomModelPath User-entered custom MediaPipe model filesystem path.
 * @param localSdxlModels Current stable-diffusion.cpp download/selection catalog for setup.
 * @param localSdxlCustomModel Whether setup currently selects a custom stable-diffusion.cpp model path.
 * @param localSdxlCustomModelPath User-entered custom stable-diffusion.cpp model filesystem path.
 * @param localCoreMlModels Current Core ML download/selection catalog for setup.
 * @param localCoreMlCustomModelPath User-entered custom Core ML model filesystem path.
 * @param localBonsaiModels Current Bonsai download/selection catalog for setup.
 * @param localBonsaiCustomModel Whether setup currently selects a custom Bonsai model path.
 * @param localBonsaiCustomModelPath User-entered custom Bonsai model filesystem path.
 * @param passwordVisible Whether the setup password field reveals its entered text.
 * @param serverUrlValidationError Inline AUTOMATIC1111 endpoint feedback; null when valid.
 * @param swarmUiUrlValidationError Inline SwarmUI endpoint feedback; null when valid.
 * @param loginValidationError Inline Basic Auth login feedback; null when valid.
 * @param passwordValidationError Inline Basic Auth password feedback; null when valid.
 * @param hordeApiKeyValidationError Inline AI Horde key feedback; null when valid.
 * @param huggingFaceApiKeyValidationError Inline Hugging Face token feedback; null when valid.
 * @param openAiApiKeyValidationError Inline official OpenAI key feedback; null when valid.
 * @param stabilityAiApiKeyValidationError Inline Stability AI key feedback; null when valid.
 * @param falAiApiKeyValidationError Inline Fal.ai key feedback; null when valid.
 * @param arliAiApiKeyValidationError Inline ArliAI key feedback; null when valid.
 * @param localCustomOnnxPathValidationError Inline custom ONNX path feedback; null when valid.
 * @param localCustomMediaPipePathValidationError Inline custom MediaPipe path feedback; null when valid.
 * @param localCustomSdxlPathValidationError Inline custom stable-diffusion.cpp path feedback; null when valid.
 * @param localCustomBonsaiPathValidationError Inline custom Bonsai path feedback; null when valid.
 * @author Dmitriy Moroz
 */
@Immutable
data class ServerSetupState(
    val showBackNavArrow: Boolean = false,
    val step: Step = Step.SOURCE,
    val mode: ServerSource = ServerSource.AUTOMATIC1111,
    val platform: Platform = Platform.ANDROID,
    val allowedModes: List<ServerSource> = remoteSetupSources,
    val sourceSearchQuery: String = "",
    val sourceTypeFilter: ServerSourceType? = null,
    val sourceReadinessFilters: Set<ServerSourceReadiness> = emptySet(),
    val sourceTagFilters: Set<FeatureTag> = emptySet(),
    val sourceSortOrder: SourceSortOrder = SourceSortOrder.DEFAULT,
    val allowLocalCustomModels: Boolean = true,
    val modal: Modal = Modal.None,
    val loadingConfiguration: Boolean = true,
    val serverUrl: String = "",
    val swarmUiUrl: String = "",
    val easyDiffusionUrl: String = "",
    val easyDiffusionUrlValidationError: ValidationError? = null,
    val hordeApiKey: String = "",
    val huggingFaceApiKey: String = "",
    val openAiApiKey: String = "",
    val openAiCompatibleUrl: String = "",
    val openAiCompatibleApiKey: String = "",
    val openAiCompatibleUrlValidationError: ValidationError? = null,
    val openAiOAuthAccount: String? = null,
    val openAiOAuthCode: String? = null,
    val openAiOAuthBusy: Boolean = false,
    val openAiOAuthStorageError: Boolean = false,
    val stabilityAiApiKey: String = "",
    val falAiApiKey: String = "",
    val arliAiApiKey: String = "",
    val hordeDefaultApiKey: Boolean = false,
    val demoMode: Boolean = false,
    val demoModeUrl: String = "",
    val authorizationStorageFailure: AuthorizationStorageFailure.Reason? = null,
    val isAuthorizationStorageBusy: Boolean = false,
    val isAuthorizationResetConfirmationVisible: Boolean = false,
    val authType: AuthType = AuthType.ANONYMOUS,
    val login: String = "",
    val password: String = "",
    val huggingFaceModels: List<String> = emptyList(),
    val huggingFaceModel: String = HuggingFaceModel.default.alias,
    val localOnnxModels: List<LocalModel> = emptyList(),
    val localOnnxCustomModel: Boolean = false,
    val localOnnxCustomModelPath: String = "",
    val localMediaPipeModels: List<LocalModel> = emptyList(),
    val localMediaPipeCustomModel: Boolean = false,
    val localMediaPipeCustomModelPath: String = "",
    val localSdxlModels: List<LocalModel> = emptyList(),
    val localSdxlCustomModel: Boolean = false,
    val localSdxlCustomModelPath: String = "",
    val localCoreMlModels: List<LocalModel> = emptyList(),
    val localCoreMlCustomModelPath: String = "",
    val localBonsaiModels: List<LocalModel> = emptyList(),
    val localBonsaiCustomModel: Boolean = false,
    val localBonsaiCustomModelPath: String = "",
    val passwordVisible: Boolean = false,
    val serverUrlValidationError: ValidationError? = null,
    val swarmUiUrlValidationError: ValidationError? = null,
    val loginValidationError: ValidationError? = null,
    val passwordValidationError: ValidationError? = null,
    val hordeApiKeyValidationError: ValidationError? = null,
    val huggingFaceApiKeyValidationError: ValidationError? = null,
    val openAiApiKeyValidationError: ValidationError? = null,
    val stabilityAiApiKeyValidationError: ValidationError? = null,
    val falAiApiKeyValidationError: ValidationError? = null,
    val arliAiApiKeyValidationError: ValidationError? = null,
    val localCustomOnnxPathValidationError: ValidationError? = null,
    val localCustomMediaPipePathValidationError: ValidationError? = null,
    val localCustomSdxlPathValidationError: ValidationError? = null,
    val localCustomBonsaiPathValidationError: ValidationError? = null,
) : MviState {

    val localCustomModel: Boolean
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxCustomModel
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> localMediaPipeCustomModel
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localSdxlCustomModel
            else -> false
        }

    val localCustomModelPath: String
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxCustomModelPath
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> localMediaPipeCustomModelPath
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localSdxlCustomModelPath
            ServerSource.LOCAL_APPLE_CORE_ML -> localCoreMlCustomModelPath
            else -> ""
        }

    val localModels: List<LocalModel>
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localOnnxModels
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> localMediaPipeModels
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localSdxlModels
            ServerSource.LOCAL_APPLE_CORE_ML -> localCoreMlModels
            ServerSource.LOCAL_APPLE_BONSAI -> localBonsaiModels
            else -> emptyList()
        }

    val localCustomModelPathValidationError: ValidationError?
        get() = when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> localCustomOnnxPathValidationError
            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> localCustomMediaPipePathValidationError
            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> localCustomSdxlPathValidationError
            else -> null
        }

    /** Applies a protected authorization snapshot to setup fields without persisting it.
     * @param value Protected authorization snapshot applied to presentation inputs.
     * @author Dmitriy Moroz */

    fun withCredentials(value: AuthorizationCredentials): ServerSetupState = when (value) {
        is AuthorizationCredentials.HttpBasic -> copy(
            authType = AuthType.HTTP_BASIC,
            login = value.login,
            password = value.password,
        )

        AuthorizationCredentials.None -> copy(authType = AuthType.ANONYMOUS)
    }

    /** Builds the current anonymous or Basic Auth value for a subsequent checked configuration publication.
     * @author Dmitriy Moroz */

    fun credentialsDomain(): AuthorizationCredentials = when (authType) {
        AuthType.ANONYMOUS -> AuthorizationCredentials.None
        AuthType.HTTP_BASIC -> AuthorizationCredentials.HttpBasic(login, password)
    }

    /** Updates only the active local provider’s custom path and clears its stale path-validation error.
     * @param value User-entered path for the active local runtime.
     * @author Dmitriy Moroz */

    fun withLocalCustomModelPath(value: String): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            localOnnxCustomModelPath = value,
            localCustomOnnxPathValidationError = null,
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            localMediaPipeCustomModelPath = value,
            localCustomMediaPipePathValidationError = null,
        )

        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> copy(
            localSdxlCustomModelPath = value,
            localCustomSdxlPathValidationError = null,
        )

        ServerSource.LOCAL_APPLE_CORE_ML -> copy(localCoreMlCustomModelPath = value)

        else -> this
    }

    /** Replaces the matching catalog item only in the active local provider’s list.
     * @param value Updated download/selection metadata for one local catalog item.
     * @author Dmitriy Moroz */

    fun withUpdatedLocalModel(value: LocalModel): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            localOnnxModels = localOnnxModels.withCommonNewState(value),
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            localMediaPipeModels = localMediaPipeModels.withCommonNewState(value),
        )

        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> copy(
            localSdxlModels = localSdxlModels.withCommonNewState(value),
        )

        ServerSource.LOCAL_APPLE_CORE_ML -> copy(
            localCoreMlModels = localCoreMlModels.withCommonNewState(value),
        )

        ServerSource.LOCAL_APPLE_BONSAI -> copy(
            localBonsaiModels = localBonsaiModels.withCommonNewState(value),
        )

        else -> this
    }

    /** Marks the deleted item unavailable, clears its download state and dismisses the confirmation.
     * @param value Catalog item whose downloaded files have been removed.
     * @author Dmitriy Moroz */

    fun withDeletedLocalModel(value: LocalModel): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            modal = Modal.None,
            localOnnxModels = localOnnxModels.withCommonNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            ),
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            modal = Modal.None,
            localMediaPipeModels = localMediaPipeModels.withCommonNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            ),
        )

        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> copy(
            modal = Modal.None,
            localSdxlModels = localSdxlModels.withCommonNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            ),
        )

        ServerSource.LOCAL_APPLE_CORE_ML -> copy(
            modal = Modal.None,
            localCoreMlModels = localCoreMlModels.withCommonNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            ),
        )

        ServerSource.LOCAL_APPLE_BONSAI -> copy(
            modal = Modal.None,
            localBonsaiModels = localBonsaiModels.withCommonNewState(
                value.copy(
                    downloadState = DownloadState.Unknown,
                    downloaded = false,
                ),
            ),
        )

        else -> copy(modal = Modal.None)
    }

    /** Selects the chosen model in the active local provider’s catalog.
     * @param value Catalog item selected for the active local runtime.
     * @author Dmitriy Moroz */

    fun withSelectedLocalModel(value: LocalModel): ServerSetupState = when (mode) {
        ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
            localOnnxModels = localOnnxModels.withCommonNewState(value.copy(selected = true)),
        )

        ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
            localMediaPipeModels = localMediaPipeModels.withCommonNewState(value.copy(selected = true)),
        )

        ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> copy(
            localSdxlModels = localSdxlModels.withCommonNewState(value.copy(selected = true)),
        )

        ServerSource.LOCAL_APPLE_CORE_ML -> copy(
            localCoreMlModels = localCoreMlModels.withCommonNewState(value.copy(selected = true)),
        )

        ServerSource.LOCAL_APPLE_BONSAI -> copy(
            localBonsaiModels = localBonsaiModels.withCommonNewState(value.copy(selected = true)),
        )

        else -> this
    }

    /** Toggles custom local selection and reconciles selected downloaded/custom catalog items.
     * @param value Whether custom local model paths may be selected.
     * @author Dmitriy Moroz */

    fun withAllowCustomModel(value: Boolean): ServerSetupState {

        /** Marks only the requested custom-model identity selected within this model list.
         * @param id Stable local model or custom-model identifier to select or delete.
         * @author Dmitriy Moroz */
        fun List<LocalModel>.updateCustomModelSelection(id: String) = withCommonNewState(
            find { model -> model.id == id }?.copy(selected = value),
        )
        return when (mode) {
            ServerSource.LOCAL_MICROSOFT_ONNX -> copy(
                localOnnxCustomModel = value,
                localOnnxModels = localOnnxModels.updateCustomModelSelection(
                    id = LocalAiModel.CustomOnnx.id,
                ),
            )

            ServerSource.LOCAL_GOOGLE_MEDIA_PIPE -> copy(
                localMediaPipeCustomModel = value,
                localMediaPipeModels = localMediaPipeModels.updateCustomModelSelection(
                    id = LocalAiModel.CustomMediaPipe.id,
                ),
            )

            ServerSource.LOCAL_STABLE_DIFFUSION_CPP -> copy(
                localSdxlCustomModel = value,
                localSdxlModels = localSdxlModels.updateCustomModelSelection(
                    id = LocalAiModel.CustomSdxl.id,
                ),
            )

            else -> this
        }
    }

    enum class Step {
        SOURCE,
        CONFIGURE,
    }

    enum class AuthType {
        ANONYMOUS,
        HTTP_BASIC,
    }

    enum class ValidationError {
        EmptyField,
        EmptyUrl,
        InvalidScheme,
        InvalidPort,
        InvalidUrl,
    }

    enum class SourceSortOrder {
        DEFAULT,
        RECENTLY_UPDATED,
        OLDEST_UPDATED,
    }

    sealed interface Modal {
        data object ConnectionUnverified : Modal
        data object None : Modal
        data object Communicating : Modal
        data object SourceFilters : Modal
        data object SourceSort : Modal
        data object ConnectLocalHost : Modal

        /** Requests selection of a download mirror for a stable local model.
         * @param modelId Stable local model identifier whose advertised sources are displayed.
         * @author Dmitriy Moroz */
        data class SelectDownloadSource(val modelId: String) : Modal

        /** Retains the exact downloaded item awaiting deletion confirmation.
         * @param model Downloaded catalog item to remove after confirmation.
         * @author Dmitriy Moroz */
        data class DeleteLocalModelConfirm(val model: LocalModel) : Modal

        /** Displays a safe user-facing setup failure.
         * @param message User-facing error message; credentials and protocol payloads are not included.
         * @author Dmitriy Moroz */
        data class Error(val message: String) : Modal
    }

    /** Presentation model combining stable identity, available archives and current local selection/download state.
     * @param id Stable model identifier shared with the domain catalog.
     * @param name Human-readable model label.
     * @param size Display-ready archive or installed size.
     * @param sources Advertised archive download URLs in provider order.
     * @param downloaded Whether the model files are available locally.
     * @param downloadState Current progress, completion or failure of a model download.
     * @param selected Whether this model is selected for the active local runtime.
     * @author Dmitriy Moroz */
    data class LocalModel(
        val id: String,
        val name: String,
        val size: String,
        val sources: List<String> = emptyList(),
        val downloaded: Boolean = false,
        val downloadState: DownloadState = DownloadState.Unknown,
        val selected: Boolean = false,
    )

    companion object {
        val remoteSetupSources = listOf(
            ServerSource.AUTOMATIC1111,
            ServerSource.SWARM_UI,
            ServerSource.OPEN_AI_COMPATIBLE,
            ServerSource.EASY_DIFFUSION,
            ServerSource.HORDE,
            ServerSource.HUGGING_FACE,
            ServerSource.OPEN_AI,
            ServerSource.OPEN_AI_OAUTH,
            ServerSource.STABILITY_AI,
            ServerSource.FAL_AI,
            ServerSource.ARLI_AI,
        )
    }
}

/** Builds setup inputs from independent provider settings and current platform/local model catalogs.
 * @param allowedModes Provider catalog allowed by the current build and launch flow.
 * @param platform Platform whose provider availability and readiness apply.
 * @param huggingFaceModels Supported inference models available to remote setup.
 * @param localOnnxModels Current ONNX download/selection catalog.
 * @param localMediaPipeModels Current MediaPipe download/selection catalog.
 * @param localSdxlModels Current stable-diffusion.cpp download/selection catalog.
 * @param localCoreMlModels Current Core ML download/selection catalog.
 * @param localBonsaiModels Current Bonsai download/selection catalog.
 * @param allowLocalCustomModels Whether this setup flow permits custom local model paths.
 * @param demoModeUrl Configured AUTOMATIC1111 demonstration endpoint.
 * @param showBackNavArrow Whether setup may navigate back to the screen that opened it.
 * @author Dmitriy Moroz */

fun Configuration.toServerSetupState(
    allowedModes: List<ServerSource>,
    platform: Platform,
    huggingFaceModels: List<String>,
    localOnnxModels: List<LocalAiModel> = emptyList(),
    localMediaPipeModels: List<LocalAiModel> = emptyList(),
    localSdxlModels: List<LocalAiModel> = emptyList(),
    localCoreMlModels: List<LocalAiModel> = emptyList(),
    localBonsaiModels: List<LocalAiModel> = emptyList(),
    allowLocalCustomModels: Boolean = true,
    demoModeUrl: String = "",
    showBackNavArrow: Boolean = false,
): ServerSetupState {
    val safeMode = source.takeIf(allowedModes::contains) ?: ServerSource.AUTOMATIC1111
    val supportedHuggingFaceAliases = HuggingFaceModel.supportedHfInferenceTextToImageAliases
    val safeHuggingFaceModels = (
            huggingFaceModels
                .filter(supportedHuggingFaceAliases::contains)
                .ifEmpty {
                    HuggingFaceModel.supportedHfInferenceTextToImageModels.map(
                        HuggingFaceModel::alias
                    )
                }
                    + HuggingFaceModel.default.alias
            )
        .distinct()
    val safeHuggingFaceModel = huggingFaceModel
        .takeIf(safeHuggingFaceModels::contains)
        ?: HuggingFaceModel.default.alias
    return ServerSetupState(
        loadingConfiguration = false,
        authorizationStorageFailure = authorizationStorageFailure,
        showBackNavArrow = showBackNavArrow,
        mode = safeMode,
        platform = platform,
        allowedModes = allowedModes,
        allowLocalCustomModels = allowLocalCustomModels,
        serverUrl = serverUrl,
        swarmUiUrl = swarmUiUrl,
        easyDiffusionUrl = easyDiffusionUrl,
        hordeApiKey = hordeApiKey,
        hordeDefaultApiKey = hordeApiKey == HORDE_DEFAULT_API_KEY,
        huggingFaceApiKey = huggingFaceApiKey,
        huggingFaceModel = safeHuggingFaceModel,
        huggingFaceModels = safeHuggingFaceModels,
        localOnnxModels = localOnnxModels.mapToCommonSetupModels(),
        localOnnxCustomModel = localOnnxModels.hasCommonSelectedCustomModel(LocalAiModel.CustomOnnx.id),
        localOnnxCustomModelPath = localOnnxModelPath,
        localMediaPipeModels = localMediaPipeModels.mapToCommonSetupModels(),
        localMediaPipeCustomModel = localMediaPipeModels.hasCommonSelectedCustomModel(LocalAiModel.CustomMediaPipe.id),
        localMediaPipeCustomModelPath = localMediaPipeModelPath,
        localSdxlModels = localSdxlModels.mapToCommonSetupModels(),
        localSdxlCustomModel = localSdxlModels.hasCommonSelectedCustomModel(LocalAiModel.CustomSdxl.id),
        localSdxlCustomModelPath = localSdxlModelPath,
        localCoreMlModels = localCoreMlModels.mapToCommonSetupModels(),
        localCoreMlCustomModelPath = localCoreMlModelPath,
        localBonsaiModels = localBonsaiModels.mapToCommonSetupModels(),
        localBonsaiCustomModelPath = localBonsaiModelPath,
        openAiApiKey = openAiApiKey,
        openAiCompatibleUrl = openAiCompatibleUrl,
        openAiCompatibleApiKey = openAiCompatibleApiKey,
        stabilityAiApiKey = stabilityAiApiKey,
        falAiApiKey = falAiApiKey,
        arliAiApiKey = arliAiApiKey,
        demoMode = demoMode,
        demoModeUrl = demoModeUrl,
    ).withCredentials(
        if (demoMode) AuthorizationCredentials.None else authCredentials,
    )
}

const val HORDE_DEFAULT_API_KEY = "0000000000"

/** Converts domain local-model metadata into setup selection/download items.
 * @author Dmitriy Moroz */

private fun List<LocalAiModel>.mapToCommonSetupModels(): List<ServerSetupState.LocalModel> =
    map { model ->
        ServerSetupState.LocalModel(
            id = model.id,
            name = model.name,
            size = model.size,
            sources = model.sources,
            downloaded = model.downloaded,
            selected = model.selected,
        )
    }

/** Checks whether the saved selected model points to the provider’s custom model identity.
 * @param id Stable local model or custom-model identifier to select or delete.
 * @author Dmitriy Moroz */

private fun List<LocalAiModel>.hasCommonSelectedCustomModel(id: String): Boolean =
    any { model -> model.selected && model.id == id }

/** Replaces the matching setup item while retaining unrelated catalog entries.
 * @param model Updated catalog item to merge without changing unrelated models.
 * @author Dmitriy Moroz */

private fun List<ServerSetupState.LocalModel>.withCommonNewState(
    model: ServerSetupState.LocalModel?,
): List<ServerSetupState.LocalModel> =
    map { item ->
        if (model == null) {
            item
        } else if (item.id == model.id) {
            model
        } else if (model.selected) {
            item.copy(selected = false)
        } else {
            item
        }
    }
