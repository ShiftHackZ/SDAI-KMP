package com.shifthackz.aisdv1.domain.usecase.easydiffusion


/** Refreshes the configured instance catalog; fetch failures remain visible.
 * @author Dmitriy Moroz */
interface FetchEasyDiffusionModelsUseCase {
    /** Fetches the configured instance catalog strictly; failed requests preserve the previous cache and propagate to the caller.
     * @param url Captured instance root or prefix; the operation never rereads current preferences.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(url: String)
}
