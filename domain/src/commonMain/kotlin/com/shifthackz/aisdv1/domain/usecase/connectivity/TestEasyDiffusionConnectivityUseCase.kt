package com.shifthackz.aisdv1.domain.usecase.connectivity

/** Checks that an anonymous Easy Diffusion candidate is ready or busy with valid generation work.
 * @author Dmitriy Moroz */
interface TestEasyDiffusionConnectivityUseCase {
    /** Accepts Online, Rendering and LoadingModel; starting/unavailable states remain actionable errors.
     * @param url Candidate HTTP(S) root or reverse-proxy prefix.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(url: String)
}
