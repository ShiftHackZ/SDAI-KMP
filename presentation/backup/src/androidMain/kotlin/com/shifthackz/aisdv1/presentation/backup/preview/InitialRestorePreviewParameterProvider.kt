package com.shifthackz.aisdv1.presentation.backup.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

/**
 * Supplies restore-only first-launch content, selection and operation feedback snapshots.
 *
 * @author Dmitriy Moroz
 */
internal class InitialRestorePreviewParameterProvider :
    PreviewParameterProvider<BackupPreviewCase> {
    /**
     * Fixed restore states rendered without tabs in the initial-recovery preview group.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<BackupPreviewCase> = BackupRestorePreviewCases.values.asSequence()

    /**
     * Identifies the recovery state shown above each generated preview.
     *
     * @param index Position in the ordered restore-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = BackupRestorePreviewCases.values[index].name
}
