package com.shifthackz.aisdv1.domain.usecase.backup

import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection

/**
 * Loads available portable categories without reading every gallery image.
 *
 * @author Dmitriy Moroz
 */
interface LoadBackupInventoryUseCase {
    /**
     * Loads available portable categories without reading every gallery image.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): BackupInspection
}
