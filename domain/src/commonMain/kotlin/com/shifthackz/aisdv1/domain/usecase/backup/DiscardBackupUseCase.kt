package com.shifthackz.aisdv1.domain.usecase.backup


/**
 * Releases only app-owned staging for an abandoned or completed archive.
 *
 * @author Dmitriy Moroz
 */
interface DiscardBackupUseCase {
    /**
     * Releases only app-owned staging for an abandoned or completed archive.
     *
     * @param handle Opaque archive identity or its private document reference.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(handle: String)
}
