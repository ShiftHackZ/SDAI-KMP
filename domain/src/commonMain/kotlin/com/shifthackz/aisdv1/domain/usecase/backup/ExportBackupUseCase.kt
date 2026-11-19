package com.shifthackz.aisdv1.domain.usecase.backup


/**
 * Copies a complete private archive to a user-selected Android destination; iOS exports the staged document natively.
 * @author Dmitriy Moroz
 */
interface ExportBackupUseCase {
    /**
     * Copies a complete private archive to a user-selected Android destination; iOS exports the staged document natively.
     * @param document Private archive reference produced by create.
     * @param destination Opaque destination reference from the native document picker.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(document: String, destination: String)
}
