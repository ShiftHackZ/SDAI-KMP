package com.shifthackz.aisdv1.presentation.backup.platform

import androidx.compose.runtime.Composable
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport

/**
 * Native document presentation owned by the Compose route lifecycle.
 * @author Dmitriy Moroz
 */
interface BackupDocumentLauncher {
    /**
     * Opens a single backup file using the system document picker.
     * @author Dmitriy Moroz
     */
    fun open()

    /**
     * Presents native export for a complete staged document.
     * @param backup Private staged document and suggested public filename.
     * @author Dmitriy Moroz
     */
    fun export(backup: BackupExport)
}

/**
 * Remembers native activity-result launchers or a retained UIKit document delegate.
 * @param onPicked Opaque selected document, or null after neutral cancellation.
 * @param onExported Android destination reference or iOS completion flag; cancellation reports neither.
 * @author Dmitriy Moroz
 */
@Composable
expect fun rememberBackupDocumentLauncher(
    onPicked: (String?) -> Unit,
    onExported: (String?, Boolean, String) -> Unit
): BackupDocumentLauncher
