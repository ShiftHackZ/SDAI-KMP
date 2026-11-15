package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.StableDiffusionSamplersDataSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionSampler
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Keeps sampler cache reads and replacement consistent, scoped to the configured server and credentials.
 *
 * @param remoteDataSource Fetches sampler metadata from AUTOMATIC1111.
 * @param localDataSource Stores disposable sampler metadata for this process.
 * @param preferenceManager Supplies the active AUTOMATIC1111 server URL.
 * @param connections Supplies consistent server/credential cache ownership.
 * @author Dmitriy Moroz
 */
internal class StableDiffusionSamplersRepositoryImpl(
    private val remoteDataSource: StableDiffusionSamplersDataSource.Remote,
    private val localDataSource: StableDiffusionSamplersDataSource.Local,
    private val preferenceManager: PreferenceManager,
    private val connections: AuthenticatedConnectionProvider,
) : StableDiffusionSamplersRepository {

    private val cacheMutex = Mutex()
    private var cachedServerUrl: String? = null
    private var cachedCredentials: AuthorizationCredentials? = null

    /**
     * Fetches a connection snapshot and caches it only while that connection remains selected.
     * Reads cannot observe the intermediate empty table during cache replacement.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchSamplers() {
        val connection = connections.snapshot()
        val serverUrl = connection.automatic1111Url
        val credentials = connection.credentials
        val samplers = remoteDataSource.fetchSamplers(
            baseUrl = serverUrl,
            credentials = credentials,
        )
        currentCoroutineContext().ensureActive()
        cacheMutex.withLock {
            val currentConnection = connections.snapshot()
            if (
                serverUrl != currentConnection.automatic1111Url ||
                credentials != currentConnection.credentials
            ) return@withLock
            cachedServerUrl = null
            cachedCredentials = null
            localDataSource.insertSamplers(samplers)
            cachedServerUrl = serverUrl
            cachedCredentials = credentials
        }
    }

    /**
     * Returns only samplers fetched for the current connection; other-server cache is unavailable.
     *
     * @return Cached samplers, or an empty list if the selected connection has no valid cache.
     * @author Dmitriy Moroz
     */
    override suspend fun getSamplers(): List<StableDiffusionSampler> = cacheMutex.withLock {
        val connection = connections.snapshot()
        if (
            cachedServerUrl != connection.automatic1111Url ||
            cachedCredentials != connection.credentials
        ) {
            emptyList()
        } else {
            localDataSource.getSamplers()
        }
    }
}
