package com.shifthackz.aisdv1.domain.usecase.civitai

/**
 * User-initiated native OAuth authorization and protected session establishment.
 *
 * @author Dmitriy Moroz
 */
fun interface SignInToCivitaiUseCase {
    /**
     * Authorizes SDAI and saves the validated account
     * cancellation never replaces a prior account.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke()
}
