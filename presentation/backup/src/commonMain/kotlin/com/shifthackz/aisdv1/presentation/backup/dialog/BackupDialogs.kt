package com.shifthackz.aisdv1.presentation.backup.dialog

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationInputEnabled
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.backup.BackupIntent
import com.shifthackz.aisdv1.presentation.backup.BackupState
import com.shifthackz.aisdv1.presentation.backup.component.providerName
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialogContent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.InfoDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialogCancelButton

/**
 * Presents exclusive progress, provider-consent and acknowledged result feedback using shared app dialogs.
 * @param state Nonsecret operation state; completed restore data stays available after acknowledgement.
 * @param processIntent Cancellation, conflict consent and acknowledgement event sink.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackupDialogs(state: BackupState, processIntent: (BackupIntent) -> Unit) {
    if (state.isLeaving || !LocalMviVisibility.current || !LocalNavigationInputEnabled.current) return
    val dismiss = { processIntent(BackupIntent.DismissFeedback) }
    when {
        state.isBusy || state.isExporting -> {
            val status = state.progress?.let { progress ->
                val category =
                    progress.category?.let { Localization.string("backup_category_" + it.key) }
                val count =
                    if (progress.total > 0) "${progress.completed} / ${progress.total}" else progress.completed.toString()
                listOfNotNull(category, count).joinToString(" · ")
            } ?: Localization.string("backup_working")
            ProgressDialog(
                title = Localization.string("backup_title").asUiText(),
                subTitle = status.asUiText(),
                canDismiss = false,
            ) {
                ProgressDialogCancelButton(onClick = { processIntent(BackupIntent.CancelWork) })
            }
        }

        state.conflict != null -> DecisionInteractiveDialogContent(
            title = Localization.string("backup_conflict_title"),
            text = Localization.string(
                "backup_conflict_description",
                state.conflict.providers.joinToString(", ", transform = ::providerName)
            ),
            confirmActionText = Localization.string("backup_replace_providers"),
            dismissActionText = Localization.string("backup_cancel"),
            onConfirmAction = { processIntent(BackupIntent.ConfirmProviderReplacement) },
            onDismissRequest = { processIntent(BackupIntent.DismissConflict) },
        )

        state.isFeedbackVisible -> when {
            state.hasCanceled -> InfoDialog(
                Localization.string("backup_title").asUiText(),
                Localization.string("backup_canceled").asUiText(),
                dismiss,
            )

            state.hasError -> ErrorDialog(Localization.string("backup_error").asUiText(), dismiss)
            state.result != null -> {
                val result = state.result
                if (result.hasFailed) {
                    ErrorDialog(backupResultText(result).asUiText(), dismiss)
                } else {
                    InfoDialog(
                        Localization.string("backup_restored").asUiText(),
                        backupResultText(result).asUiText(),
                        dismiss
                    )
                }
            }

            state.hasExported -> InfoDialog(
                Localization.string("backup_title").asUiText(),
                Localization.string("backup_exported").asUiText(),
                dismiss,
            )

        }
    }
}
