package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.entity.ForgeModule
import com.shifthackz.aisdv1.domain.datasource.ForgeModulesDataSource
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ForgeModulesRepository

/**
 * Implements `ForgeModulesRepository` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class ForgeModulesRepositoryImpl(
    /**
     * Exposes the `remoteDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val remoteDataSource: ForgeModulesDataSource,
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
) : ForgeModulesRepository {

    /**
     * Loads SDAI data through `fetchModules`.
     *
     * @return Result produced by `fetchModules`.
     * @author Dmitriy Moroz
     */
    override suspend fun fetchModules(): List<ForgeModule> {
        val connection = connections.snapshot()
        return remoteDataSource.fetchModules(
            baseUrl = connection.automatic1111Url,
            credentials = connection.credentials,
        )
    }
}
