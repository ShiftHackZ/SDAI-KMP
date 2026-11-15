package com.shifthackz.aisdv1.domain.repository

import com.shifthackz.aisdv1.domain.entity.StableDiffusionHyperNetwork

/**
 * Refreshes remote hypernetworks and reads cached entries, propagating operation failures.
 *
 * @author Dmitriy Moroz
 */
interface StableDiffusionHyperNetworksRepository {
    /**
     * Refreshes the cache for A1111, propagating fetch, storage and cancellation failures.
     * Other providers leave the cache unchanged.
     *
     * @author Dmitriy Moroz
     */
    suspend fun fetchHyperNetworks()

    /**
     * Reads cached hypernetworks without contacting the server.
     *
     * @return Previously stored entries, or an empty list if none were cached.
     * @author Dmitriy Moroz
     */
    suspend fun getHyperNetworks(): List<StableDiffusionHyperNetwork>
}
