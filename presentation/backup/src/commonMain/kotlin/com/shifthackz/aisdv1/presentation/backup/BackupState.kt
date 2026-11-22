package com.shifthackz.aisdv1.presentation.backup

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult

/**
 * Nonsecret backup UI state. Archive bytes and provider credential values never enter this model.
 *
 * @param create Create inventory and choices.
 * @param restore Restore summary and choices.
 * @param isRestoreTab Current tab, or true in first-launch restore-only mode.
 * @param isBusy Whether a serialized file or commit job owns the screen.
 * @param isLeaving Whether Back is quietly cleaning up before the route exits.
 * @param isExporting Whether graph-owned destination copying is in progress.
 * @param hasPendingExport Whether a durable export intent owns the staged source.
 * @param isPicking Whether a native document picker is open.
 * @param progress Current bounded work stage.
 * @param conflict Named provider conflicts and opaque consent fingerprint.
 * @param result Durable completion report, including partial failure.
 * @param isFeedbackVisible Whether the current completion, cancellation or failure still needs acknowledgement.
 * @param hasError Whether the last operation failed before a completion report.
 * @param hasCanceled Whether the user canceled active work.
 * @param hasExported Whether native document export actually completed.
 * @param hasPendingSession Whether unfinished restore intent owns the staged archive.
 * @param isRestoreSelectionLocked Whether displayed choices are bound to a submitted or completed restore operation.
 *
 * @author Dmitriy Moroz
 */
data class BackupState(
    val create: BackupPageState = BackupPageState(),
    val restore: BackupPageState = BackupPageState(),
    val isRestoreTab: Boolean = false,
    val isBusy: Boolean = false,
    val isLeaving: Boolean = false,
    val isExporting: Boolean = false,
    val hasPendingExport: Boolean = false,
    val isPicking: Boolean = false,
    val progress: BackupProgress? = null,
    val conflict: BackupRestoreResult.Conflict? = null,
    val result: BackupRestoreResult.Finished? = null,
    val isFeedbackVisible: Boolean = false,
    val hasError: Boolean = false,
    val hasCanceled: Boolean = false,
    val hasExported: Boolean = false,
    val hasPendingSession: Boolean = false,
    val isRestoreSelectionLocked: Boolean = false,
) : MviState
