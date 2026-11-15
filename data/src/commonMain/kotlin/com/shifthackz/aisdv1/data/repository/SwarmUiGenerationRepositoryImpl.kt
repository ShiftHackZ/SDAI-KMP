package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.mappers.withModelName
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.datasource.SwarmUiGenerationDataSource
import com.shifthackz.aisdv1.domain.datasource.SwarmUiModelsRemoteDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.preference.SessionPreference
import com.shifthackz.aisdv1.domain.repository.SwarmUiGenerationRepository
import com.shifthackz.aisdv1.network.exception.SwarmUiBadSessionException

/**
 * Implements `SwarmUiGenerationRepository` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class SwarmUiGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    /**
     * Exposes the `preferenceManager` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val preferenceManager: PreferenceManager,
    /**
     * Exposes the `sessionPreference` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val sessionPreference: SessionPreference,
    /**
     * Captures consistent connection inputs before starting network work.
     *
     * @author Dmitriy Moroz
     */
    private val connections: AuthenticatedConnectionProvider,
    /**
     * Exposes the `swarmSessionRemoteDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val swarmSessionRemoteDataSource: SwarmUiModelsRemoteDataSource,
    /**
     * Exposes the `remoteDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val remoteDataSource: SwarmUiGenerationDataSource.Remote,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
), SwarmUiGenerationRepository {

    override suspend fun checkApiAvailability() {
        val connection = connections.snapshot()
        getSessionId(
            baseUrl = connection.swarmUiUrl,
            credentials = connection.credentials,
        )
    }

    override suspend fun checkApiAvailability(url: String) {
        val connection = connections.snapshot(swarmUiUrl = url)
        forceRenewSession(
            baseUrl = url,
            credentials = connection.credentials,
        )
    }

    override suspend fun generateFromText(payload: TextToImagePayload): AiGenerationResult {
        val connection = connections.snapshot()
        val baseUrl = connection.swarmUiUrl
        val credentials = connection.credentials
        val model = connection.swarmUiModel
        val sessionId = getSessionId(baseUrl, credentials)
        val ai = try {
            remoteDataSource.textToImage(baseUrl, sessionId, model, credentials, payload)
        } catch (e: SwarmUiBadSessionException) {
            val renewedSessionId = forceRenewSession(baseUrl, credentials)
            remoteDataSource.textToImage(baseUrl, renewedSessionId, model, credentials, payload)
        }
        return insertGenerationResult(ai.withModelName(model))
    }

    override suspend fun generateFromImage(payload: ImageToImagePayload): AiGenerationResult {
        val connection = connections.snapshot()
        val baseUrl = connection.swarmUiUrl
        val credentials = connection.credentials
        val model = connection.swarmUiModel
        val sessionId = getSessionId(baseUrl, credentials)
        val ai = try {
            remoteDataSource.imageToImage(baseUrl, sessionId, model, credentials, payload)
        } catch (e: SwarmUiBadSessionException) {
            val renewedSessionId = forceRenewSession(baseUrl, credentials)
            remoteDataSource.imageToImage(baseUrl, renewedSessionId, model, credentials, payload)
        }
        return insertGenerationResult(ai.withModelName(model))
    }

    private suspend fun getSessionId(
        baseUrl: String,
        credentials: AuthorizationCredentials,
    ): String = sessionPreference
        .swarmUiSessionId
        .takeIf(String::isNotBlank)
        ?: forceRenewSession(baseUrl, credentials)

    private suspend fun forceRenewSession(
        baseUrl: String,
        credentials: AuthorizationCredentials,
    ): String = swarmSessionRemoteDataSource
        .getNewSession(baseUrl, credentials)
        .also { sessionId -> sessionPreference.swarmUiSessionId = sessionId }
}
