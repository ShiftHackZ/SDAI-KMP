package com.shifthackz.aisdv1.presentation.screen.setup.reducer

import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.presentation.model.readinessFor
import com.shifthackz.aisdv1.presentation.screen.setup.model.HORDE_DEFAULT_API_KEY
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupEffect
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState
import com.shifthackz.aisdv1.presentation.screen.setup.viewmodel.url

/** Routes setup UI intents into candidate state changes and injected navigation/download/connection actions.
 * @param router Navigates back or completes the provider setup flow.
 * @param linksProvider Resolves supported setup/help/demo URLs.
 * @param currentState Reads current candidate configuration and provider discovery state.
 * @param updateState Atomically applies presentation-only setup edits.
 * @param emitEffect Delivers platform permission and external-navigation effects.
 * @param localModelDownloadClickReducer Chooses download-source or deletion confirmation for a clicked model.
 * @param deleteLocalModel Removes the confirmed downloaded model through its owning view model.
 * @param download Starts the confirmed model archive download with its stable identifier and chosen URL.
 * @param validateAndConnectToServer Validates active candidate fields before publishing provider configuration.
 * @param connectToServer Connects after an explicit confirmation has already satisfied setup validation.
 * @author Dmitriy Moroz */
internal class ServerSetupIntentProcessor(
    private val router: ServerSetupRouter,
    private val linksProvider: LinksProvider,
    private val currentState: () -> ServerSetupState,
    private val updateState: (((ServerSetupState) -> ServerSetupState) -> Unit),
    private val emitEffect: (ServerSetupEffect) -> Unit,
    private val localModelDownloadClickReducer: (ServerSetupState.LocalModel) -> Unit,
    private val deleteLocalModel: (String) -> Unit,
    private val download: (String, String) -> Unit,
    private val validateAndConnectToServer: () -> Unit,
    private val connectToServer: () -> Unit,
) {

    /** Applies a setup intent to its field, discovery-filter, navigation or local-model action.
     * @param intent User event emitted by this screen’s production controls.
     * @author Dmitriy Moroz */

    fun process(intent: ServerSetupIntent) {
        when (intent) {
            ServerSetupIntent.RetryAuthorizationStorage,
            ServerSetupIntent.RequestAuthorizationReset,
            ServerSetupIntent.CancelAuthorizationReset,
            ServerSetupIntent.ConfirmAuthorizationReset -> Unit

            ServerSetupIntent.OAuthLogin, ServerSetupIntent.OAuthCancel, ServerSetupIntent.OAuthLogout, ServerSetupIntent.OAuthOpenBrowser -> Unit
            is ServerSetupIntent.AllowLocalCustomModel -> updateState { state ->
                state.withAllowCustomModel(intent.allow)
            }

            is ServerSetupIntent.SelectLocalModel -> updateState { state ->
                state.withSelectedLocalModel(intent.model)
            }

            is ServerSetupIntent.SelectLocalModelPath -> updateState { state ->
                state.withLocalCustomModelPath(intent.value)
            }

            ServerSetupIntent.LaunchManageStoragePermission -> {
                emitEffect(ServerSetupEffect.LaunchManageStoragePermission)
            }

            is ServerSetupIntent.LocalModel.ClickReduce -> localModelDownloadClickReducer(intent.model)

            is ServerSetupIntent.LocalModel.DeleteConfirm -> updateState {
                deleteLocalModel(intent.model.id)
                it.withDeletedLocalModel(intent.model)
            }

            is ServerSetupIntent.LocalModel.DownloadConfirm -> with(intent) {
                download(modelId, url)
            }

            is ServerSetupIntent.UpdateServerMode -> updateState {
                it.copy(mode = intent.mode)
            }

            is ServerSetupIntent.UpdateSourceSearchQuery -> updateState {
                it.copy(sourceSearchQuery = intent.query)
            }

            is ServerSetupIntent.UpdateSourceTypeFilter -> updateState {
                if (
                    it.allowedModes.hasSourceFilterMatch(
                        state = it,
                        type = intent.type,
                        readinessFilters = it.sourceReadinessFilters,
                        tags = it.sourceTagFilters,
                    )
                ) {
                    it.copy(sourceTypeFilter = intent.type)
                        .withMatchingFilterSource()
                } else {
                    it
                }
            }

            is ServerSetupIntent.ToggleSourceReadinessFilter -> updateState { state ->
                val nextFilters = if (intent.readiness in state.sourceReadinessFilters) {
                    state.sourceReadinessFilters - intent.readiness
                } else {
                    state.sourceReadinessFilters + intent.readiness
                }
                if (
                    state.allowedModes.hasSourceFilterMatch(
                        state = state,
                        type = state.sourceTypeFilter,
                        readinessFilters = nextFilters,
                        tags = state.sourceTagFilters,
                    )
                ) {
                    state.copy(sourceReadinessFilters = nextFilters)
                        .withMatchingFilterSource()
                } else {
                    state
                }
            }

            is ServerSetupIntent.ToggleSourceTagFilter -> updateState { state ->
                val nextTags = if (intent.tag in state.sourceTagFilters) {
                    state.sourceTagFilters - intent.tag
                } else {
                    state.sourceTagFilters + intent.tag
                }
                if (
                    state.allowedModes.hasSourceFilterMatch(
                        state = state,
                        type = state.sourceTypeFilter,
                        readinessFilters = state.sourceReadinessFilters,
                        tags = nextTags,
                    )
                ) {
                    state.copy(sourceTagFilters = nextTags)
                        .withMatchingFilterSource()
                } else {
                    state
                }
            }

            ServerSetupIntent.ResetSourceFilters -> updateState {
                it.copy(
                    sourceTypeFilter = null,
                    sourceReadinessFilters = emptySet(),
                    sourceTagFilters = emptySet(),
                )
            }

            is ServerSetupIntent.UpdateSourceSortOrder -> updateState {
                it.copy(sourceSortOrder = intent.sortOrder)
            }

            ServerSetupIntent.ShowSourceFilters -> updateState {
                it.copy(modal = ServerSetupState.Modal.SourceFilters)
            }

            ServerSetupIntent.ShowSourceSort -> updateState {
                it.copy(modal = ServerSetupState.Modal.SourceSort)
            }

            ServerSetupIntent.AcknowledgeUnverifiedConnection -> Unit

            is ServerSetupIntent.UpdateOpenAiCompatibleUrl -> updateState {
                it.copy(openAiCompatibleUrl = intent.url, openAiCompatibleUrlValidationError = null)
            }

            is ServerSetupIntent.UpdateOpenAiCompatibleApiKey -> updateState {
                it.copy(openAiCompatibleApiKey = intent.key)
            }

            is ServerSetupIntent.UpdateServerUrl -> updateState {
                it.copy(serverUrl = intent.url, serverUrlValidationError = null)
            }

            is ServerSetupIntent.UpdateEasyDiffusionUrl -> updateState {
                it.copy(easyDiffusionUrl = intent.url, easyDiffusionUrlValidationError = null)
            }

            is ServerSetupIntent.UpdateSwarmUiUrl -> updateState {
                it.copy(swarmUiUrl = intent.url, swarmUiUrlValidationError = null)
            }

            is ServerSetupIntent.UpdateAuthType -> updateState {
                it.copy(authType = intent.type)
            }

            is ServerSetupIntent.UpdateLogin -> updateState {
                it.copy(login = intent.login, loginValidationError = null)
            }

            is ServerSetupIntent.UpdatePassword -> updateState {
                it.copy(password = intent.password, passwordValidationError = null)
            }

            is ServerSetupIntent.UpdatePasswordVisibility -> updateState {
                it.copy(passwordVisible = !intent.visible)
            }

            is ServerSetupIntent.UpdateHordeApiKey -> updateState {
                it.copy(
                    hordeApiKey = intent.key,
                    hordeDefaultApiKey = intent.key == HORDE_DEFAULT_API_KEY,
                    hordeApiKeyValidationError = null,
                )
            }

            is ServerSetupIntent.UpdateOpenAiApiKey -> updateState {
                it.copy(openAiApiKey = intent.key, openAiApiKeyValidationError = null)
            }

            is ServerSetupIntent.UpdateStabilityAiApiKey -> updateState {
                it.copy(stabilityAiApiKey = intent.key, stabilityAiApiKeyValidationError = null)
            }

            is ServerSetupIntent.UpdateFalAiApiKey -> updateState {
                it.copy(falAiApiKey = intent.key, falAiApiKeyValidationError = null)
            }

            is ServerSetupIntent.UpdateArliAiApiKey -> updateState {
                it.copy(arliAiApiKey = intent.key, arliAiApiKeyValidationError = null)
            }

            is ServerSetupIntent.UpdateHuggingFaceApiKey -> updateState {
                it.copy(huggingFaceApiKey = intent.key, huggingFaceApiKeyValidationError = null)
            }

            is ServerSetupIntent.UpdateHuggingFaceModel -> updateState {
                it.copy(huggingFaceModel = intent.model)
            }

            is ServerSetupIntent.UpdateDemoMode -> updateState {
                it.copy(
                    demoMode = intent.value,
                    authType = if (intent.value) {
                        ServerSetupState.AuthType.ANONYMOUS
                    } else {
                        it.authType
                    },
                    serverUrlValidationError = null,
                )
            }

            is ServerSetupIntent.UpdateHordeDefaultApiKey -> updateState {
                it.copy(
                    hordeDefaultApiKey = intent.value,
                    hordeApiKey = if (intent.value) HORDE_DEFAULT_API_KEY else "",
                    hordeApiKeyValidationError = null,
                )
            }

            is ServerSetupIntent.LaunchUrl -> {
                emitEffect(ServerSetupEffect.OpenUrl(intent.link.url(linksProvider)))
            }

            ServerSetupIntent.MainButtonClick -> when (currentState().step) {
                ServerSetupState.Step.SOURCE -> updateState {
                    it.copy(step = ServerSetupState.Step.CONFIGURE)
                }

                ServerSetupState.Step.CONFIGURE -> validateAndConnectToServer()
            }

            ServerSetupIntent.DismissDialog -> updateState {
                it.copy(modal = ServerSetupState.Modal.None)
            }

            ServerSetupIntent.NavigateBack -> if (currentState().step == ServerSetupState.Step.SOURCE) {
                router.navigateBack()
            } else {
                emitEffect(ServerSetupEffect.HideKeyboard)
                updateState {
                    it.copy(step = ServerSetupState.Step.SOURCE)
                }
            }

            ServerSetupIntent.ConnectToLocalHost -> connectToServer()
        }
    }
}

/** Checks whether candidate discovery filters leave at least one allowed provider visible.
 * @param state Current provider setup fields, catalogs and validation feedback.
 * @param type Optional hosting type required by the candidate filter.
 * @param readinessFilters Readiness markers permitted by the candidate filters.
 * @param tags Capability tags required by the candidate filters.
 * @author Dmitriy Moroz */

private fun List<ServerSource>.hasSourceFilterMatch(
    state: ServerSetupState,
    type: ServerSourceType?,
    readinessFilters: Set<ServerSourceReadiness>,
    tags: Set<FeatureTag>,
): Boolean = any { source ->
    val sourceReadiness = source.readinessFor(state.platform)
    (type == null || source.type == type) &&
            (readinessFilters.isEmpty() || sourceReadiness in readinessFilters) &&
            source.featureTags.containsAll(tags)
}

/** Retains a matching selected provider or chooses the first provider satisfying current discovery filters.
 * @author Dmitriy Moroz */

private fun ServerSetupState.withMatchingFilterSource(): ServerSetupState {
    if (
        listOf(mode).hasSourceFilterMatch(
            state = this,
            type = sourceTypeFilter,
            readinessFilters = sourceReadinessFilters,
            tags = sourceTagFilters,
        )
    ) {
        return this
    }
    val firstMatchingSource = allowedModes.firstOrNull { source ->
        listOf(source).hasSourceFilterMatch(
            state = this,
            type = sourceTypeFilter,
            readinessFilters = sourceReadinessFilters,
            tags = sourceTagFilters,
        )
    } ?: return this
    return copy(mode = firstMatchingSource)
}
