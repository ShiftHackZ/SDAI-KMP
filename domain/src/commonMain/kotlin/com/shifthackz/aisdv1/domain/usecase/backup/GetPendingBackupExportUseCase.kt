package com.shifthackz.aisdv1.domain.usecase.backup


/**
 * Checks durable export intent so startup restores its dedicated route.
 *
 * @author Dmitriy Moroz
 */
interface GetPendingBackupExportUseCase {
    /**
     * Checks durable export intent so startup restores its dedicated route.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): Boolean
}
