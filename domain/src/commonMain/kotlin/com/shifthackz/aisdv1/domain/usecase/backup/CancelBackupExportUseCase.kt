package com.shifthackz.aisdv1.domain.usecase.backup


/**
 * Cancels the active destination copy while retaining its staged source for retry.
 * @author Dmitriy Moroz
 */
interface CancelBackupExportUseCase {
    /**
     * Cancels the active destination copy while retaining its staged source for retry.
     * @author Dmitriy Moroz
     */
    operator fun invoke()
}
