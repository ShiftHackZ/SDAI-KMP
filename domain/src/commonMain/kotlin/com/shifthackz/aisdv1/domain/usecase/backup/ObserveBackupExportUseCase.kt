package com.shifthackz.aisdv1.domain.usecase.backup

import kotlinx.coroutines.flow.StateFlow
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportOperation

/**
 * Observes process-owned export progress across screen/activity recreation.
 *
 * @author Dmitriy Moroz
 */
interface ObserveBackupExportUseCase {
    /**
     * Observes process-owned export progress across screen/activity recreation.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): StateFlow<BackupExportOperation>
}
