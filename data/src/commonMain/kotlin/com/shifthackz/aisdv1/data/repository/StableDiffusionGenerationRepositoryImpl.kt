package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.mappers.withModelName
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.datasource.StableDiffusionGenerationDataSource
import com.shifthackz.aisdv1.domain.demo.ImageToImageDemo
import com.shifthackz.aisdv1.domain.demo.TextToImageDemo
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.ImageToImagePayload
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.gateway.AuthenticatedConnectionProvider
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.StableDiffusionGenerationRepository

/**
 * Implements `StableDiffusionGenerationRepository` behavior in the SDAI data layer.
 *
 * @author Dmitriy Moroz
 */
internal class StableDiffusionGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    backgroundWorkObserver: BackgroundWorkObserver,
    localDataSource: GenerationResultDataSource.Local,
    /**
     * Exposes the `remoteDataSource` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val remoteDataSource: StableDiffusionGenerationDataSource.Remote,
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
    /**
     * Exposes the `textToImageDemo` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val textToImageDemo: TextToImageDemo,
    /**
     * Exposes the `imageToImageDemo` value used by the SDAI data layer.
     *
     * @author Dmitriy Moroz
     */
    private val imageToImageDemo: ImageToImageDemo,
) : CoreGenerationRepository(
    mediaStoreGateway = mediaStoreGateway,
    localDataSource = localDataSource,
    preferenceManager = preferenceManager,
    backgroundWorkObserver = backgroundWorkObserver,
), StableDiffusionGenerationRepository {

    override suspend fun checkApiAvailability() {
        val connection = connections.snapshot()
        remoteDataSource.checkAvailability(
            baseUrl = connection.automatic1111Url,
            credentials = connection.credentials,
        )
    }

    override suspend fun checkApiAvailability(url: String) {
        val connection = connections.snapshot(automatic1111Url = url)
        remoteDataSource.checkAvailability(
            baseUrl = url,
            credentials = connection.credentials,
        )
    }

    override suspend fun generateFromText(payload: TextToImagePayload): List<AiGenerationResult> {
        val connection = connections.snapshot()
        val ai =
            if (preferenceManager.demoMode) List(payload.batchCount.coerceAtLeast(1)) {
                textToImageDemo.getDemoBase64(payload)
            }
            else remoteDataSource.textToImage(
                baseUrl = connection.automatic1111Url,
                credentials = connection.credentials,
                payload = payload,
            )

        return ai
            .withModelName(preferenceManager.sdModel)
            .map { insertGenerationResult(it) }
    }

    override suspend fun generateFromImage(payload: ImageToImagePayload): List<AiGenerationResult> {
        val connection = connections.snapshot()
        val ai =
            if (preferenceManager.demoMode) List(payload.batchCount.coerceAtLeast(1)) {
                imageToImageDemo.getDemoBase64(payload)
            }
            else remoteDataSource.imageToImage(
                baseUrl = connection.automatic1111Url,
                credentials = connection.credentials,
                payload = payload,
            )

        return ai
            .withModelName(preferenceManager.sdModel)
            .map { insertGenerationResult(it) }
    }

    override suspend fun interruptGeneration() {
        val connection = connections.snapshot()
        remoteDataSource.interruptGeneration(
            baseUrl = connection.automatic1111Url,
            credentials = connection.credentials,
        )
    }
}
