package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.StableDiffusionScripts
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionScriptsDataSource
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.StableDiffusionScriptsRepository

/**
 * Implements `StableDiffusionScriptsRepository` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class StableDiffusionScriptsRepositoryImpl(
    /**
     * Exposes the `remoteDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val remoteDataSource: StableDiffusionScriptsDataSource,
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
) : StableDiffusionScriptsRepository {

    /**
     * Loads SDAI data through `fetchScripts`.
     *
     * @return Result produced by `fetchScripts`.
     * @author Dmitriy Moroz
     */
    override suspend fun fetchScripts(): StableDiffusionScripts {
        val connection = connections.snapshot()
        return remoteDataSource.fetchScripts(
            baseUrl = connection.automatic1111Url,
            credentials = connection.credentials,
        )
    }
}
