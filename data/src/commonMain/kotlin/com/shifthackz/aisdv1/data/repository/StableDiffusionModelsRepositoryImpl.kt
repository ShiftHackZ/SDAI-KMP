package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.datasource.StableDiffusionModelsDataSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionModel
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.StableDiffusionModelsRepository

/**
 * Implements `StableDiffusionModelsRepository` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class StableDiffusionModelsRepositoryImpl(
    /**
     * Exposes the `remoteDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val remoteDataSource: StableDiffusionModelsDataSource.Remote,
    /**
     * Exposes the `localDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val localDataSource: StableDiffusionModelsDataSource.Local,
    /**
     * Exposes the `preferenceManager` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
    /**
     * Captures consistent connection inputs before starting network work.
     *
     * @author Dmitriy Moroz
     */
    private val connections: AuthenticatedConnectionProvider,
) : StableDiffusionModelsRepository {

    /**
     * Loads SDAI data through `fetchModels`.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun fetchModels() {
        val connection = connections.snapshot()
        val models = remoteDataSource.fetchSdModels(
            baseUrl = connection.automatic1111Url,
            credentials = connection.credentials,
        )
        localDataSource.insertModels(models)
    }

    /**
     * Loads SDAI data through `fetchAndGetModels`.
     *
     * @return Result produced by `fetchAndGetModels`.
     * @author Dmitriy Moroz
     */
    override suspend fun fetchAndGetModels(): List<StableDiffusionModel> {
        runCatching { fetchModels() }
        return getModels()
    }

    /**
     * Loads SDAI data through `getModels`.
     *
     * @return Result produced by `getModels`.
     * @author Dmitriy Moroz
     */
    override suspend fun getModels(): List<StableDiffusionModel> =
        localDataSource.getModels()
}
