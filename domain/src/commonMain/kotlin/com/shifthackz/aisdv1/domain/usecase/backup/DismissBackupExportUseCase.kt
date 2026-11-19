package com.shifthackz.aisdv1.domain.usecase.backup


/**
 * Acknowledges a completed/abandoned export and releases only its private staging.
 * @author Dmitriy Moroz
 */
interface DismissBackupExportUseCase {
    /**
     * Acknowledges a completed/abandoned export and releases only its private staging.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}
