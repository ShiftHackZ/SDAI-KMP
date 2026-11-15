package com.shifthackz.aisdv1.presentation.screen.setup.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import com.shifthackz.aisdv1.presentation.model.localizedDescription
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupState

/**
 * Distinguishes unavailable saved credentials from an anonymous provider configuration.
 * Reset requires a separate confirmation and affects only saved Basic Auth.
 *
 * @param state Editable provider state with its safe storage failure and pending action.
 * @param processIntent Dispatches retry and explicitly confirmed recovery through MVI.
 * @author Dmitriy Moroz
 */
@Composable
internal fun AuthorizationStorageRecovery(
    state: ServerSetupState,
    processIntent: (ServerSetupIntent) -> Unit,
) {
    val reason = state.authorizationStorageFailure ?: return
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = AuthorizationStorageFailure(reason).localizedDescription(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(
            enabled = !state.isAuthorizationStorageBusy,
            onClick = { processIntent(ServerSetupIntent.RetryAuthorizationStorage) },
        ) { Text(Localization.string("retry")) }
        TextButton(
            enabled = !state.isAuthorizationStorageBusy,
            onClick = { processIntent(ServerSetupIntent.RequestAuthorizationReset) },
        ) { Text(Localization.string("auth_storage_reset")) }
    }
    if (state.isAuthorizationResetConfirmationVisible) {
        AlertDialog(
            onDismissRequest = { processIntent(ServerSetupIntent.CancelAuthorizationReset) },
            title = { Text(Localization.string("auth_storage_reset")) },
            text = { Text(Localization.string("auth_storage_reset_confirm")) },
            confirmButton = {
                TextButton(onClick = { processIntent(ServerSetupIntent.ConfirmAuthorizationReset) }) {
                    Text(Localization.string("auth_storage_reset"))
                }
            },
            dismissButton = {
                TextButton(onClick = { processIntent(ServerSetupIntent.CancelAuthorizationReset) }) {
                    Text(Localization.string("cancel"))
                }
            },
        )
    }
}
