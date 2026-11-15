package com.shifthackz.aisdv1.domain.usecase.sdhypernet

import com.shifthackz.aisdv1.domain.entity.StableDiffusionHyperNetwork
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository
import kotlinx.coroutines.CancellationException

/**
 * Refreshes optional hypernetwork discovery for the extras picker, using cached entries on failure.
 *
 * @param stableDiffusionHyperNetworksRepository Owns remote discovery and cached entries.
 * @author Dmitriy Moroz
 */
internal class FetchAndGetHyperNetworksUseCaseImpl(
    private val stableDiffusionHyperNetworksRepository: StableDiffusionHyperNetworksRepository,
) : FetchAndGetHyperNetworksUseCase {
    /**
     * Attempts a refresh on every invocation and recovers ordinary discovery failures with cached data.
     * Cancellation, nonrecoverable errors and cache read failures propagate to the caller.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(): List<StableDiffusionHyperNetwork> {
        runCatching {
            stableDiffusionHyperNetworksRepository.fetchHyperNetworks()
        }.onFailure {
            if (it is CancellationException || it !is Exception) throw it
        }
        return stableDiffusionHyperNetworksRepository.getHyperNetworks()
    }
}
