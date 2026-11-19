package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress

/**
 * Copies a document into private staging and validates its binary container before any user-data mutation.
 * @author Dmitriy Moroz
 */
interface InspectBackupUseCase {
    /**
     * Copies a document into private staging and validates its binary container before any user-data mutation.
     * @param document Opaque reference returned by the native document picker.
     * @param onProgress Receives staging and validation progress.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        document: String,
        onProgress: (BackupProgress) -> Unit
    ): BackupInspection
}
