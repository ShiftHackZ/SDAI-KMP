package com.shifthackz.aisdv1.domain.usecase.settings

/** Candidate validation and atomic connection publication for an anonymous Easy Diffusion instance.
 * @author Dmitriy Moroz */
interface ConnectToEasyDiffusionUseCase {
    /** Keeps previous provider settings if candidate readiness or model discovery fails.
     * @param url HTTP(S) root or reverse-proxy prefix, without embedded credentials.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(url: String): Result<Unit>
}
