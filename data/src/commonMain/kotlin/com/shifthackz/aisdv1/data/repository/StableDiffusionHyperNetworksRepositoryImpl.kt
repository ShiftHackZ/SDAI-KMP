package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.StableDiffusionHyperNetworksDataSource
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionHyperNetwork
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository

/**
 * Fetches A1111 hypernetworks and stores successful responses in the local cache.
 * Fetch and cache failures propagate so each use case can choose its recovery policy.
 *
 * @param remoteDataSource Discovers hypernetworks exposed by the server.
 * @param localDataSource Replaces and reads cached hypernetwork entries.
 * @param preferenceManager Identifies whether the active provider supports A1111 metadata.
 * @param connections Captures the server URL and credentials for each fetch.
 * @author Dmitriy Moroz
 */
internal class StableDiffusionHyperNetworksRepositoryImpl(
    private val remoteDataSource: StableDiffusionHyperNetworksDataSource.Remote,
    private val localDataSource: StableDiffusionHyperNetworksDataSource.Local,
    private val preferenceManager: PreferenceManager,
    private val connections: AuthenticatedConnectionProvider,
) : StableDiffusionHyperNetworksRepository {

    /**
     * Replaces cached hypernetworks after a successful fetch from the active A1111 server.
     * Fetch and storage failures propagate so callers can apply their own recovery policy.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchHyperNetworks() {
        val connection = connections.snapshot()
        if (preferenceManager.source != ServerSource.AUTOMATIC1111) return
        val hyperNetworks = remoteDataSource.fetchHyperNetworks(
            baseUrl = connection.automatic1111Url,
            credentials = connection.credentials,
        )
        localDataSource.insertHyperNetworks(hyperNetworks)
    }

    /**
     * Reads cached entries without requesting remote discovery.
     *
     * @return Previously stored hypernetworks, or an empty list before the first successful fetch.
     * @author Dmitriy Moroz
     */
    override suspend fun getHyperNetworks(): List<StableDiffusionHyperNetwork> =
        localDataSource.getHyperNetworks()
}
