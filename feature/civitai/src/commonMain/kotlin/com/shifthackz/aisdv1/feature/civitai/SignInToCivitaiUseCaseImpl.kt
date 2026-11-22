package com.shifthackz.aisdv1.feature.civitai

import com.shifthackz.aisdv1.domain.gateway.CivitaiOAuthBrowser
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.SignInToCivitaiUseCase

/**
 * Exchanges only a native-browser-verified PKCE proof before saving a validated account.
 *
 * @param browser Native authentication-session owner.
 * @param repository Strict token exchange and protected session persistence.
 * @author Dmitriy Moroz
 */
internal class SignInToCivitaiUseCaseImpl(
    private val browser: CivitaiOAuthBrowser,
    private val repository: CivitaiRepository,
) : SignInToCivitaiUseCase {
    /**
     * Authorizes SDAI and saves the validated account
     * cancellation never replaces a prior account.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke() {
        val proof = browser.authorize()
        repository.connectOAuth(proof)
    }
}
