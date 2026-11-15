package com.shifthackz.aisdv1.domain.usecase.settings

/**
 * Explicitly confirmed recovery of saved Basic Auth; never invoked as automatic error handling.
 *
 * @author Dmitriy Moroz
 */
interface ResetAuthorizationCredentialsUseCase {
    /**
     * Clears this credential store while preserving shared encryption keys and unrelated data.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}
