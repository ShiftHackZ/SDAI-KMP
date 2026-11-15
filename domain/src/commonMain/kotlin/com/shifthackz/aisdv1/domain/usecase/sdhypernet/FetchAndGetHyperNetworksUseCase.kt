package com.shifthackz.aisdv1.domain.usecase.sdhypernet

import com.shifthackz.aisdv1.domain.entity.StableDiffusionHyperNetwork

/**
 * Refreshes hypernetworks whenever their extras picker is opened.
 *
 * @author Dmitriy Moroz
 */
interface FetchAndGetHyperNetworksUseCase {
    /**
     * Attempts remote discovery and returns cached entries if refresh fails.
     * Cancellation and cache read failures propagate to the caller.
     *
     * @return Refreshed or cached hypernetworks, possibly empty.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(): List<StableDiffusionHyperNetwork>
}
