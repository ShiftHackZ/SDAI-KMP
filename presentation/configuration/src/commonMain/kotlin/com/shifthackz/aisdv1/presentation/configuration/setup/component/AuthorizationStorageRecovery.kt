package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import androidx.compose.material3.Text
import com.shifthackz.aisdv1.presentation.components.widget.action.TextActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.presentation.components.model.localizedDescription
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Distinguishes unavailable saved credentials from an anonymous provider configuration.
 * Reset requires a separate confirmation and affects only saved Basic Auth.
 *
 * @param state Editable provider state with its safe storage failure and pending action.
 * @param processIntent Dispatches retry and explicitly confirmed recovery through MVI.
 * @param modifier Layout modifiers for credential-storage failure and recovery controls.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun AuthorizationStorageRecovery(
    state: ServerSetupState,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reason = state.authorizationStorageFailure ?: return
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = AuthorizationStorageFailure(reason).localizedDescription(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            isEnabled = !state.isAuthorizationStorageBusy,
            onClick = { processIntent(ServerSetupIntent.RetryAuthorizationStorage) },
        ) { Text(Localization.string("retry")) }
        TextActionButton(
            isEnabled = !state.isAuthorizationStorageBusy,
            onClick = { processIntent(ServerSetupIntent.RequestAuthorizationReset) },
        ) { Text(Localization.string("auth_storage_reset")) }
    }
    if (state.isAuthorizationResetConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { processIntent(ServerSetupIntent.CancelAuthorizationReset) },
            title = { Text(Localization.string("auth_storage_reset")) },
            text = { Text(Localization.string("auth_storage_reset_confirm")) },
            confirmButton = {
                TextActionButton(onClick = { processIntent(ServerSetupIntent.ConfirmAuthorizationReset) }) {
                    Text(Localization.string("auth_storage_reset"))
                }
            },
            dismissButton = {
                TextActionButton(onClick = { processIntent(ServerSetupIntent.CancelAuthorizationReset) }) {
                    Text(Localization.string("cancel"))
                }
            },
        )
    }
}
