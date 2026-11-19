package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport

/**
 * Streams selected app data into a complete private binary archive; failure never returns an export.
 * @author Dmitriy Moroz
 */
interface CreateBackupUseCase {
    /**
     * Streams selected app data into a complete private binary archive; failure never returns an export.
     * @param selection Available categories and provider children to include.
     * @param onProgress Receives bounded work progress; callbacks contain no credentials.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        selection: BackupSelection,
        onProgress: (BackupProgress) -> Unit
    ): BackupExport
}
