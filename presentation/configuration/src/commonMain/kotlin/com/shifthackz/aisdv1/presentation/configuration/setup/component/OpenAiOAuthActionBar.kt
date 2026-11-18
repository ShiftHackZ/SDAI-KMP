package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.OpenAiOAuthSetupPhase
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.openAiOAuthPhase

/**
 * Pins only the actions available in the current OAuth phase below the scrolling form.
 * Its natural height accommodates wrapped labels and system font scaling above the safe area.
 *
 * @param state Current setup and device-authorization state.
 * @param strings Shared wizard labels, including the final connection action.
 * @param processIntent Dispatches the selected configuration action through MVI.
 * @param modifier Layout modifiers for the pinned OAuth action area.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun OpenAiOAuthActionBar(
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SetupActionBar(modifier = modifier) {
        when (state.openAiOAuthPhase) {
            OpenAiOAuthSetupPhase.SIGNED_OUT -> OAuthPrimaryAction(
                label = Localization.string("open_ai_oauth_login"),
                isEnabled = !state.loadingConfiguration,
                onClick = { processIntent(ServerSetupIntent.OAuthLogin) },
            )

            OpenAiOAuthSetupPhase.REQUESTING_CODE -> OAuthSecondaryAction(
                label = Localization.string("cancel"),
                onClick = { processIntent(ServerSetupIntent.OAuthCancel) },
            )

            OpenAiOAuthSetupPhase.AWAITING_AUTHORIZATION -> {
                OAuthPrimaryAction(
                    label = Localization.string("open_ai_oauth_open_browser"),
                    onClick = { processIntent(ServerSetupIntent.OAuthOpenBrowser) },
                )
                OAuthSecondaryAction(
                    label = Localization.string("cancel"),
                    onClick = { processIntent(ServerSetupIntent.OAuthCancel) },
                )
            }

            OpenAiOAuthSetupPhase.SIGNED_IN -> {
                OAuthPrimaryAction(
                    label = strings.connect,
                    isEnabled = !state.loadingConfiguration && state.mainButtonEnabled,
                    onClick = { processIntent(ServerSetupIntent.MainButtonClick) },
                )
                OAuthSecondaryAction(
                    label = Localization.string("open_ai_oauth_logout"),
                    onClick = { processIntent(ServerSetupIntent.OAuthLogout) },
                )
            }

            OpenAiOAuthSetupPhase.STORAGE_ERROR -> OAuthPrimaryAction(
                label = Localization.string("open_ai_oauth_reset"),
                isEnabled = !state.loadingConfiguration,
                onClick = { processIntent(ServerSetupIntent.OAuthLogout) },
            )
        }
    }
}

/**
 * Renders one full-width primary action without constraining multiline label height.
 *
 * @param label Localized visible and accessible action name.
 * @param modifier Layout modifiers for the shared action button.
 * @param isEnabled Whether the action can currently be submitted.
 * @param onClick Action callback.
 *
 * @author Dmitriy Moroz
 */
@Composable
private fun OAuthPrimaryAction(
    label: String,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    onClick: () -> Unit,
) {
    SetupPrimaryAction(
        modifier = modifier,
        isEnabled = isEnabled,
        onClick = onClick,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Renders a full-width secondary action with a minimum accessible touch target.
 *
 * @param label Localized visible and accessible action name.
 * @param onClick Action callback.
 * @param modifier Layout modifiers for the shared action button.
 *
 * @author Dmitriy Moroz
 */
@Composable
private fun OAuthSecondaryAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SetupSecondaryAction(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}
