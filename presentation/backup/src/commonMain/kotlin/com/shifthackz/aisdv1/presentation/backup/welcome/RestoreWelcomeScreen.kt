package com.shifthackz.aisdv1.presentation.backup.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * First-launch invitation with two bottom actions and no path back to onboarding.
 * @param isEnabled Whether a durable navigation transition can start.
 * @param hasError Whether checked startup progression failed and can be retried.
 * @param onRestore Opens restore-only recovery.
 * @param onContinue Continues through provider setup toward home.
 * @param modifier Root full-screen layout.
 * @author Dmitriy Moroz
 */
@Composable
internal fun RestoreWelcomeScreen(
    isEnabled: Boolean,
    hasError: Boolean,
    onRestore: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Restore,
                null,
                Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.size(32.dp))
            Text(
                Localization.string("backup_welcome_title"),
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.size(16.dp))
            Text(
                Localization.string("backup_welcome_description"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (hasError) Text(
            Localization.string("backup_error"),
            color = MaterialTheme.colorScheme.error
        )
        Button(onClick = onRestore, enabled = isEnabled, modifier = Modifier.fillMaxWidth()) {
            Text(
                Localization.string("backup_restore_from_file")
            )
        }
        OutlinedButton(
            onClick = onContinue,
            enabled = isEnabled,
            modifier = Modifier.fillMaxWidth()
        ) { Text(Localization.string("backup_continue")) }
    }
}
