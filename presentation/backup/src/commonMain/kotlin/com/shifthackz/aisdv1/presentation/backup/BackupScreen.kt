package com.shifthackz.aisdv1.presentation.backup

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreNotice
import com.shifthackz.aisdv1.presentation.backup.component.providerName
import com.shifthackz.aisdv1.presentation.backup.create.CreateBackupScreen
import com.shifthackz.aisdv1.presentation.backup.restore.RestoreBackupScreen
import com.shifthackz.aisdv1.presentation.backup.welcome.RestoreWelcomeScreen

/**
 * Stateless backup host; dedicated create/restore content remains reusable across launch contexts.
 * @param mode Normal tabs or first-launch recovery chrome.
 * @param state Current nonsecret screen state.
 * @param processIntent Caller-owned MVI event sink.
 * @param modifier Root screen placement.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    mode: BackupScreenMode,
    state: BackupState,
    processIntent: (BackupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = !state.isBusy && !state.isPicking && !state.isExporting
    if (mode == BackupScreenMode.WELCOME) {
        RestoreWelcomeScreen(
            isEnabled,
            state.hasError,
            { processIntent(BackupIntent.BeginInitialRestore) },
            { processIntent(BackupIntent.Continue) },
            modifier
        )
        return
    }
    val page = if (state.isRestoreTab) state.restore else state.create
    Scaffold(modifier.safeDrawingPadding(), topBar = {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = { Text(Localization.string("backup_title")) },
            navigationIcon = {
                IconButton(
                    onClick = { processIntent(BackupIntent.NavigateBack) },
                    enabled = isEnabled
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, Localization.string("action_back"))
                }
            })
    }, bottomBar = {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
            if (state.isBusy || state.isExporting) {
                Row {
                    CircularProgressIndicator(Modifier.size(24.dp)); Spacer(Modifier.size(12.dp)); Text(
                    Localization.string("backup_working")
                )
                }
                state.progress?.let { progress ->
                    Text(
                        if (progress.total > 0) "${progress.completed} / ${progress.total}" else progress.completed.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                TextButton(onClick = { processIntent(BackupIntent.CancelWork) }) {
                    Text(
                        Localization.string(
                            "backup_cancel"
                        )
                    )
                }
            } else {
                val isFinished =
                    if (state.isRestoreTab) state.result?.hasFailed == false else state.hasExported
                Button(
                    onClick = { processIntent(if (isFinished) BackupIntent.NavigateBack else BackupIntent.Submit) },
                    enabled = isEnabled && (isFinished || page.selection.categories.isNotEmpty()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(Localization.string(if (isFinished) "backup_done" else if (state.isRestoreTab) "backup_restore" else "backup_create"))
                }
            }
        }
    }) { padding ->
        Column(Modifier.padding(padding)) {
            if (mode == BackupScreenMode.SETTINGS) TabRow(selectedTabIndex = if (state.isRestoreTab) 1 else 0) {
                Tab(
                    selected = !state.isRestoreTab,
                    onClick = { processIntent(BackupIntent.SelectTab(false)) },
                    enabled = isEnabled,
                    text = { Text(Localization.string("backup_create_tab")) })
                Tab(
                    selected = state.isRestoreTab,
                    onClick = { processIntent(BackupIntent.SelectTab(true)) },
                    enabled = isEnabled,
                    text = { Text(Localization.string("backup_restore_tab")) })
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
                if (state.hasError) Text(
                    Localization.string("backup_error"),
                    color = MaterialTheme.colorScheme.error
                )
                if (state.hasCanceled) Text(
                    Localization.string("backup_canceled"),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (state.hasExported && !state.isRestoreTab) Text(
                    Localization.string("backup_exported"),
                    color = MaterialTheme.colorScheme.primary
                )
                state.result?.let { result ->
                    Text(
                        Localization.string(if (result.hasFailed) "backup_partial" else "backup_restored"),
                        style = MaterialTheme.typography.titleMedium
                    )
                    result.importedCounts.forEach { (category, count) -> Text(Localization.string("backup_category_" + category.key) + ": " + count) }
                    result.notices.forEach { notice ->
                        when (notice) {
                            is BackupRestoreNotice.SharedCredentialsRetained -> Text(
                                Localization.string(
                                    "backup_shared_credentials_retained",
                                    notice.providers.joinToString(", ", transform = ::providerName)
                                )
                            )
                        }
                    }
                    if (result.skippedCount > 0) Text(
                        Localization.string(
                            "backup_skipped",
                            result.skippedCount
                        )
                    )
                }
                if (state.isRestoreTab) RestoreBackupScreen(
                    page,
                    isEnabled,
                    !state.isRestoreSelectionLocked,
                    processIntent
                )
                else CreateBackupScreen(page, isEnabled && !state.hasPendingExport, processIntent)
            }
        }
    }
    state.conflict?.let { conflict ->
        AlertDialog(
            onDismissRequest = { processIntent(BackupIntent.DismissConflict) },
            title = { Text(Localization.string("backup_conflict_title")) },
            text = {
                Text(
                    Localization.string(
                        "backup_conflict_description",
                        conflict.providers.joinToString(", ", transform = ::providerName)
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { processIntent(BackupIntent.ConfirmProviderReplacement) }) {
                    Text(
                        Localization.string("backup_replace_providers")
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { processIntent(BackupIntent.DismissConflict) }) {
                    Text(
                        Localization.string("backup_cancel")
                    )
                }
            })
    }
}
