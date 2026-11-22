package com.shifthackz.aisdv1.presentation.backup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.backup.create.CreateBackupScreen
import com.shifthackz.aisdv1.presentation.backup.restore.RestoreBackupScreen
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar

/**
 * Keeps each pager page's vertical scroll independent while reusing dedicated create/restore content.
 *
 * @param isRestore Whether this page selects and restores an existing backup.
 * @param page Independent inventory and checkbox selection for the displayed operation.
 * @param isEnabled Whether native document and selection actions can currently run.
 * @param canEditSelection Whether an owned staged operation still permits editing its selection.
 * @param processIntent Caller-owned selection and file actions.
 * @param modifier Placement within the pager or first-launch content area.
 * @param canReportToolbarScroll Whether this is the active pager page controlling toolbar overlap.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackupPageContent(
    isRestore: Boolean,
    page: BackupPageState,
    isEnabled: Boolean,
    canEditSelection: Boolean,
    processIntent: (BackupIntent) -> Unit,
    modifier: Modifier = Modifier,
    canReportToolbarScroll: Boolean = true,
) {
    val scrollState = rememberScrollState()
    if (canReportToolbarScroll) ToolbarScrollObserver(scrollState.canScrollBackward)
    Column(
        modifier.fillMaxSize()
            .verticalScrollbar(scrollState, topInset = LocalToolbarContentInset.current)
            .verticalScroll(scrollState)
            .padding(top = LocalToolbarContentInset.current)
            .padding(16.dp)
    ) {
        if (isRestore) RestoreBackupScreen(page, isEnabled, canEditSelection, processIntent)
        else CreateBackupScreen(page, isEnabled && canEditSelection, processIntent)
    }
}
