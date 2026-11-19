package com.shifthackz.aisdv1.presentation.backup

import com.shifthackz.aisdv1.core.mvi.MviEffect
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport

/**
 * Lifecycle-aware requests for native document pickers.
 * @author Dmitriy Moroz
 */
sealed interface BackupEffect : MviEffect {
    /** Opens the system document picker without discarding the previous file. @author Dmitriy Moroz */
    data object OpenDocument : BackupEffect

    /**
     * Presents native export after a complete binary archive has been staged.
     * @param backup Private document reference and suggested filename.
     * @author Dmitriy Moroz
     */
    data class ExportDocument(val backup: BackupExport) : BackupEffect
}
