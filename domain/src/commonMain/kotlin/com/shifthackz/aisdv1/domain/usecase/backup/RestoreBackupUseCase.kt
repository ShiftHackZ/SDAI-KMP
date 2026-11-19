package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult

/**
 * Merges selected data with category-atomic commits and durable retry identity; cancellation propagates.
 * @author Dmitriy Moroz
 */
interface RestoreBackupUseCase {
    /**
     * Merges selected data with category-atomic commits and durable retry identity; cancellation propagates.
     * @param request Validated archive, selection and conflict consent.
     * @param onProgress Receives current import stage progress.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        request: BackupRestoreRequest,
        onProgress: (BackupProgress) -> Unit
    ): BackupRestoreResult
}
