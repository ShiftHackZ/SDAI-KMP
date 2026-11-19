package com.shifthackz.aisdv1.domain.usecase.swarmmodel

import com.shifthackz.aisdv1.domain.datasource.SwarmUiModelsRemoteDataSource
import com.shifthackz.aisdv1.domain.entity.SwarmUiModel
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials

/**
 * Loads model descriptors from an authenticated Swarm UI session.
 *
 * @author Dmitriy Moroz
 */
interface FetchSwarmUiModelsUseCase {

    /**
     * Returns models reported by the selected Swarm UI session.
     *
     * @param baseUrl Full API base URL of the selected Swarm UI server.
     * @param sessionId Active Swarm UI session identifier.
     * @param credentials Optional transport authorization for the server.
     * @return The catalog available for the current provider.
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(
        baseUrl: String,
        sessionId: String,
        credentials: AuthorizationCredentials = AuthorizationCredentials.None,
    ): List<SwarmUiModel>
}

/**
 * Loads models from the selected Swarm UI server session.
 *
 * @param remoteDataSource Requests model descriptors with server session and transport credentials.
 *
 * @author Dmitriy Moroz
 */
internal class FetchSwarmUiModelsUseCaseImpl(
    private val remoteDataSource: SwarmUiModelsRemoteDataSource,
) : FetchSwarmUiModelsUseCase {

    /**
     * Returns models reported by the selected Swarm UI session.
     *
     * @param baseUrl Full API base URL of the selected Swarm UI server.
     * @param sessionId Active Swarm UI session identifier.
     * @param credentials Optional transport authorization for the server.
     * @return The catalog available for the current provider.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(
        baseUrl: String,
        sessionId: String,
        credentials: AuthorizationCredentials,
    ): List<SwarmUiModel> =
        remoteDataSource.fetchSwarmModels(baseUrl, sessionId, credentials)
}
