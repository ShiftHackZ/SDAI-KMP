package com.shifthackz.aisdv1.presentation.backup.restore

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.backup.BackupPageState
import com.shifthackz.aisdv1.presentation.backup.BackupIntent
import com.shifthackz.aisdv1.presentation.backup.component.BackupArchiveCard
import com.shifthackz.aisdv1.presentation.backup.component.BackupSelectionList

/**
 * Dedicated restore content shared by settings tabs and first-launch recovery.
 *
 * @param state Inspected file summary and category choices.
 * @param isEnabled Whether a new file can be selected.
 * @param canEditSelection Whether choices are still editable before a journal-bound restore.
 * @param processIntent Native picker and selection requests.
 * @param modifier Root content placement.
 * @author Dmitriy Moroz
 */
@Composable
internal fun RestoreBackupScreen(
    state: BackupPageState,
    isEnabled: Boolean,
    canEditSelection: Boolean,
    processIntent: (BackupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            Localization.string("backup_restore_description"),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OutlinedButton(
                onClick = { processIntent(BackupIntent.ChooseFile) },
                isEnabled = isEnabled,
            ) { Text(Localization.string("backup_choose_file")) }
        }
        state.inspection?.let { archive ->
            Spacer(Modifier.height(16.dp))
            BackupArchiveCard(archive)
            if (archive.skippedCount > 0) Text(
                Localization.string(
                    "backup_skipped",
                    archive.skippedCount
                ), color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            BackupSelectionList(state, isEnabled && canEditSelection, processIntent)
            Spacer(Modifier.height(16.dp))
            Text(
                Localization.string("backup_secrets_warning"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
