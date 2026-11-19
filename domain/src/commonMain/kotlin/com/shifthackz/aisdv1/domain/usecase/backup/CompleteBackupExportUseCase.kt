package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion

/**
 * Queues native document results without depending on a screen initialization job.
 * @author Dmitriy Moroz
 */
interface CompleteBackupExportUseCase {
    /**
     * Queues native document results without depending on a screen initialization job.
     * @param completion Native outcome for the durably pending export.
     * @author Dmitriy Moroz
     */
    operator fun invoke(completion: BackupExportCompletion)
}
