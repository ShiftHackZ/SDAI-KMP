package com.shifthackz.aisdv1.presentation.backup.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.presentation.backup.BackupPageState
import com.shifthackz.aisdv1.presentation.backup.BackupIntent
import com.shifthackz.aisdv1.presentation.backup.component.BackupSelectionList

/**
 * Dedicated reusable create content with a visible portable-credential disclosure.
 *
 * @param state Create inventory and independent selection.
 * @param isEnabled Whether the checklist can be edited.
 * @param processIntent Selection and navigation actions.
 * @param modifier Root content placement.
 * @author Dmitriy Moroz
 */
@Composable
internal fun CreateBackupScreen(
    state: BackupPageState,
    isEnabled: Boolean,
    processIntent: (BackupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            Localization.string("backup_create_description"),
            style = MaterialTheme.typography.bodyLarge
        )
        BackupSelectionList(state, isEnabled, processIntent)
        if (BackupCategory.PROVIDERS in state.selection.categories) Text(
            Localization.string("backup_secrets_warning"),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
