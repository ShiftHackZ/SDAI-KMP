package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Supplies every remote provider form, URL/key validation, Basic Auth recovery and all OAuth phases.
 * All account labels, passwords and codes are explicit samples rather than stored credentials.
 *
 * @author Dmitriy Moroz
 */
internal class RemoteSetupPreviewParameterProvider :
    PreviewParameterProvider<ServerSetupPreviewCase> {
    private val automatic = ConfigurationPreviewFixtures.configure(ServerSource.AUTOMATIC1111)
    private val oauth = ConfigurationPreviewFixtures.configure(ServerSource.OPEN_AI_OAUTH)
    private val basic = automatic.copy(
        authType = ServerSetupState.AuthType.HTTP_BASIC,
        login = "sample-user",
        password = "sample-password"
    )
    private val cases =
        ConfigurationPreviewFixtures.sources.filter { it.type != ServerSourceType.LOCAL }.map {
            ServerSetupPreviewCase("Form · ${it.name}", ConfigurationPreviewFixtures.configure(it))
        } + listOf(
            ServerSetupPreviewCase("Demo mode", automatic.copy(demoMode = true)),
            ServerSetupPreviewCase("Basic Auth · Hidden password", basic),
            ServerSetupPreviewCase(
                "Basic Auth · Visible password",
                basic.copy(passwordVisible = true)
            ),
            ServerSetupPreviewCase(
                "Basic Auth · Validation", basic.copy(
                    login = "",
                    password = "",
                    loginValidationError = ServerSetupState.ValidationError.EmptyField,
                    passwordValidationError = ServerSetupState.ValidationError.EmptyField,
                ), formOffset = 200
            ),
            ServerSetupPreviewCase(
                "Horde default key",
                ConfigurationPreviewFixtures.configure(ServerSource.HORDE)
                    .copy(hordeDefaultApiKey = true)
            ),
            ServerSetupPreviewCase("OAuth · Requesting code", oauth.copy(openAiOAuthBusy = true)),
            ServerSetupPreviewCase(
                "OAuth · Awaiting authorization",
                oauth.copy(openAiOAuthBusy = true, openAiOAuthCode = "SAMPLE-CODE")
            ),
            ServerSetupPreviewCase(
                "OAuth · Signed in",
                oauth.copy(openAiOAuthAccount = "sample-account")
            ),
            ServerSetupPreviewCase(
                "OAuth · Storage error",
                oauth.copy(openAiOAuthStorageError = true)
            ),
            ServerSetupPreviewCase(
                "Authorization retry busy", basic.copy(
                    authorizationStorageFailure = AuthorizationStorageFailure.Reason.UNAVAILABLE,
                    isAuthorizationStorageBusy = true,
                ), formOffset = 250
            ),
            ServerSetupPreviewCase(
                "Authorization reset consent", basic.copy(
                    authorizationStorageFailure = AuthorizationStorageFailure.Reason.UNREADABLE,
                    isAuthorizationResetConfirmationVisible = true,
                )
            ),
        ) + ServerSetupState.ValidationError.entries.map { error ->
            ServerSetupPreviewCase(
                "URL validation · $error",
                automatic.copy(serverUrl = "invalid", serverUrlValidationError = error)
            )
        } + listOf(
            ServerSource.SWARM_UI,
            ServerSource.EASY_DIFFUSION,
            ServerSource.OPEN_AI_COMPATIBLE,
            ServerSource.HORDE,
            ServerSource.HUGGING_FACE,
            ServerSource.OPEN_AI,
            ServerSource.STABILITY_AI,
            ServerSource.FAL_AI,
            ServerSource.ARLI_AI
        ).map { provider ->
            ServerSetupPreviewCase(
                "Validation · ${provider.name}",
                ConfigurationPreviewFixtures.configure(provider).copy(
                    swarmUiUrlValidationError = ServerSetupState.ValidationError.InvalidUrl,
                    easyDiffusionUrlValidationError = ServerSetupState.ValidationError.InvalidUrl,
                    openAiCompatibleUrlValidationError = ServerSetupState.ValidationError.InvalidUrl,
                    hordeApiKeyValidationError = ServerSetupState.ValidationError.EmptyField,
                    huggingFaceApiKeyValidationError = ServerSetupState.ValidationError.EmptyField,
                    openAiApiKeyValidationError = ServerSetupState.ValidationError.EmptyField,
                    stabilityAiApiKeyValidationError = ServerSetupState.ValidationError.EmptyField,
                    falAiApiKeyValidationError = ServerSetupState.ValidationError.EmptyField,
                    arliAiApiKeyValidationError = ServerSetupState.ValidationError.EmptyField,
                )
            )
        } + AuthorizationStorageFailure.Reason.entries.map { reason ->
            ServerSetupPreviewCase(
                "Authorization storage · $reason",
                basic.copy(authorizationStorageFailure = reason),
                formOffset = 250
            )
        }

    /**
     * Named remote form states rendered in both palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ServerSetupPreviewCase> = cases.asSequence()

    /**
     * Identifies the provider form, validation or authorization phase.
     *
     * @param index Position in the ordered remote-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
