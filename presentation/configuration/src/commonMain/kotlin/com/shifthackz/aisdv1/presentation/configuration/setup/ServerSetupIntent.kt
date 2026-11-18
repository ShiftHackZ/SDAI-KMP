package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType

/**
 * User events handled by the provider setup wizard.
 *
 * Source-selection intents update search/filter/sort state, provider-form intents update the
 * active configuration form, and local-model intents drive download/delete confirmation flows.
 *
 * @author Dmitriy Moroz
 */
sealed interface ServerSetupIntent : MviIntent {
    /**
     * Retries only the catalog whose recovery action was selected.
     *
     * @param source Local provider owning the failed or empty catalog.
     *
     * @author Dmitriy Moroz
     */
    data class RetryLocalCatalog(val source: ServerSource) : ServerSetupIntent

    /**
     * Retries protected credentials without replacing storage or resetting configuration.
     *
     * @author Dmitriy Moroz
     */
    data object RetryAuthorizationStorage : ServerSetupIntent

    /**
     * Opens confirmation before removing only saved Basic Auth credentials.
     *
     * @author Dmitriy Moroz
     */
    data object RequestAuthorizationReset : ServerSetupIntent

    /**
     * Dismisses recovery confirmation without changing stored credentials.
     *
     * @author Dmitriy Moroz
     */
    data object CancelAuthorizationReset : ServerSetupIntent

    /**
     * Runs the explicitly confirmed reset; never dispatched by automatic error handling.
     *
     * @author Dmitriy Moroz
     */
    data object ConfirmAuthorizationReset : ServerSetupIntent

    /**
     * Starts device authorization for the selected ChatGPT provider.
     *
     * @author Dmitriy Moroz
     */
    data object OAuthLogin : ServerSetupIntent

    /**
     * Cancels pending polling without deleting the committed session.
     *
     * @author Dmitriy Moroz
     */
    data object OAuthCancel : ServerSetupIntent

    /**
     * Deletes the local protected session, including an unreadable envelope.
     *
     * @author Dmitriy Moroz
     */
    data object OAuthLogout : ServerSetupIntent

    /**
     * Opens the trusted verification page for the current transient device code.
     *
     * @author Dmitriy Moroz
     */
    data object OAuthOpenBrowser : ServerSetupIntent

    /**
     * Selects the candidate provider without committing configuration.
     *
     * @param mode Provider whose setup fields should be displayed.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateServerMode(val mode: ServerSource) : ServerSetupIntent

    /**
     * Changes provider discovery search text.
     *
     * @param query User-entered provider search text.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateSourceSearchQuery(val query: String) : ServerSetupIntent

    /**
     * Changes the optional provider hosting-type filter.
     *
     * @param type Required hosting type, or null for every type.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateSourceTypeFilter(val type: ServerSourceType?) : ServerSetupIntent

    /**
     * Adds or removes a provider readiness marker from discovery filters.
     *
     * @param readiness Readiness marker whose membership should toggle.
     *
     * @author Dmitriy Moroz
     */
    data class ToggleSourceReadinessFilter(val readiness: ServerSourceReadiness) : ServerSetupIntent

    /**
     * Adds or removes a required provider capability from discovery filters.
     *
     * @param tag Capability tag whose membership should toggle.
     *
     * @author Dmitriy Moroz
     */
    data class ToggleSourceTagFilter(val tag: FeatureTag) : ServerSetupIntent
    data object ResetSourceFilters : ServerSetupIntent

    /**
     * Changes ordering of the filtered provider catalog.
     *
     * @param sortOrder Ordering selected by the user.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateSourceSortOrder(val sortOrder: ServerSetupState.SourceSortOrder) :
        ServerSetupIntent

    /**
     * Edits the candidate AUTOMATIC1111 endpoint without committing it.
     *
     * @param url User-entered HTTP(S) instance root or prefix.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateServerUrl(val url: String) : ServerSetupIntent

    /**
     * Updates the anonymous candidate endpoint without changing saved settings.
     *
     * @param url User-entered HTTP(S) root or prefix.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateEasyDiffusionUrl(val url: String) : ServerSetupIntent

    /**
     * Edits the independent candidate SwarmUI endpoint.
     *
     * @param url User-entered HTTP(S) instance root or prefix.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateSwarmUiUrl(val url: String) : ServerSetupIntent

    /**
     * Chooses anonymous or Basic Auth for supporting self-hosted providers.
     *
     * @param type Authorization mode selected by the user.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateAuthType(val type: ServerSetupState.AuthType) : ServerSetupIntent

    /**
     * Edits the candidate Basic Auth login without persisting credentials.
     *
     * @param login User-entered login.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateLogin(val login: String) : ServerSetupIntent

    /**
     * Edits the candidate Basic Auth password without persisting credentials.
     *
     * @param password User-entered password.
     *
     * @author Dmitriy Moroz
     */
    data class UpdatePassword(val password: String) : ServerSetupIntent

    /**
     * Changes whether the setup password field reveals its text.
     *
     * @param visible Whether the password should be shown.
     *
     * @author Dmitriy Moroz
     */
    data class UpdatePasswordVisibility(val visible: Boolean) : ServerSetupIntent

    /**
     * Edits the candidate AI Horde API key.
     *
     * @param key User-entered AI Horde credential.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateHordeApiKey(val key: String) : ServerSetupIntent

    /**
     * Edits the independent compatible-server API prefix.
     *
     * @param url Complete HTTP(S) API prefix, including version and proxy path.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateOpenAiCompatibleUrl(val url: String) : ServerSetupIntent

    /**
     * Edits the optional compatible-server bearer key.
     *
     * @param key User-entered compatible-server credential; empty preserves anonymous access.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateOpenAiCompatibleApiKey(val key: String) : ServerSetupIntent
    data object AcknowledgeUnverifiedConnection : ServerSetupIntent

    /**
     * Edits the official OpenAI API key independently of OAuth sessions.
     *
     * @param key User-entered official OpenAI credential.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateOpenAiApiKey(val key: String) : ServerSetupIntent

    /**
     * Edits the candidate Stability AI API key.
     *
     * @param key User-entered Stability AI credential.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateStabilityAiApiKey(val key: String) : ServerSetupIntent

    /**
     * Edits the candidate Fal.ai API key.
     *
     * @param key User-entered Fal.ai credential.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateFalAiApiKey(val key: String) : ServerSetupIntent

    /**
     * Edits the candidate ArliAI API key.
     *
     * @param key User-entered ArliAI credential.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateArliAiApiKey(val key: String) : ServerSetupIntent

    /**
     * Edits the candidate Hugging Face token.
     *
     * @param key User-entered Hugging Face credential.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateHuggingFaceApiKey(val key: String) : ServerSetupIntent

    /**
     * Selects the candidate supported Hugging Face inference model.
     *
     * @param model Supported inference model alias.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateHuggingFaceModel(val model: String) : ServerSetupIntent

    /**
     * Chooses whether AUTOMATIC1111 should use the configured demonstration endpoint.
     *
     * @param value Whether demonstration mode is enabled.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateDemoMode(val value: Boolean) : ServerSetupIntent

    /**
     * Chooses the supported anonymous AI Horde key.
     *
     * @param value Whether the anonymous default should replace manual key input.
     *
     * @author Dmitriy Moroz
     */
    data class UpdateHordeDefaultApiKey(val value: Boolean) : ServerSetupIntent

    /**
     * Edits the active local runtime’s custom filesystem path.
     *
     * @param value User-selected custom local model path.
     *
     * @author Dmitriy Moroz
     */
    data class SelectLocalModelPath(val value: String) : ServerSetupIntent

    /**
     * Chooses an available model from the active local runtime catalog.
     *
     * @param model Local model item selected by the user.
     *
     * @author Dmitriy Moroz
     */
    data class SelectLocalModel(val model: ServerSetupState.LocalModel) : ServerSetupIntent

    /**
     * Enables or disables selecting a custom local model path.
     *
     * @param allow Whether custom path selection is enabled.
     *
     * @author Dmitriy Moroz
     */
    data class AllowLocalCustomModel(val allow: Boolean) : ServerSetupIntent

    /**
     * Requests external navigation to a supported setup/help destination.
     *
     * @param link Supported destination resolved by the injected links provider.
     *
     * @author Dmitriy Moroz
     */
    data class LaunchUrl(val link: ServerSetupLink) : ServerSetupIntent
    data object MainButtonClick : ServerSetupIntent
    data object DismissDialog : ServerSetupIntent
    data object ShowSourceFilters : ServerSetupIntent
    data object ShowSourceSort : ServerSetupIntent
    data object NavigateBack : ServerSetupIntent
    data object LaunchManageStoragePermission : ServerSetupIntent
    data object ConnectToLocalHost : ServerSetupIntent

    sealed interface LocalModel : ServerSetupIntent {
        /**
         * Requests the appropriate download/delete/source action for a clicked local model.
         *
         * @param model Clicked model with its current download/selection state.
         *
         * @author Dmitriy Moroz
         */
        data class ClickReduce(val model: ServerSetupState.LocalModel) : LocalModel

        /**
         * Starts downloading the confirmed model archive from its chosen mirror.
         *
         * @param modelId Stable catalog model identifier.
         * @param url Chosen archive mirror URL.
         * @param type Provider captured when its source picker opened.
         *
         * @author Dmitriy Moroz
         */
        data class DownloadConfirm(
            val modelId: String,
            val url: String,
            val type: LocalAiModel.Type
        ) : LocalModel

        /**
         * Deletes only the local model explicitly confirmed by the user.
         *
         * @param model Downloaded item whose files should be removed.
         * @param type Provider captured when deletion was requested.
         *
         * @author Dmitriy Moroz
         */
        data class DeleteConfirm(
            val model: ServerSetupState.LocalModel,
            val type: LocalAiModel.Type
        ) : LocalModel
    }
}

/**
 * External setup/help links routed through `LinksProvider`.
 *
 * @author Dmitriy Moroz
 */
enum class ServerSetupLink {
    A1111Instructions,
    SwarmUiInstructions,
    HordeInfo,
    HordeSignUp,
    HuggingFaceInfo,
    OpenAiInfo,
    StabilityAiInfo,
    FalAiInfo,
    ArliAiInfo,
}
