package com.shifthackz.aisdv1.domain.usecase.caching

import com.shifthackz.aisdv1.domain.repository.EmbeddingsRepository
import com.shifthackz.aisdv1.domain.repository.ServerConfigurationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionModelsRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository

/**
 * Fetches connection metadata in order, leaving LoRA discovery to the extras picker.
 * A1111 may scan thousands of LoRA files before responding, so that request must
 * not consume the connection or startup preload deadline.
 *
 * @param serverConfigurationRepository Active server options to cache before model discovery.
 * @param sdModelsRepository Available generation models.
 * @param sdSamplersRepository Available sampling algorithms.
 * @param sdHyperNetworksRepository Available hypernetworks.
 * @param sdEmbeddingsRepository Available textual inversions.
 *
 * @author Dmitriy Moroz
 */
internal class DataPreLoaderUseCaseImpl(
    /**
     * Exposes the `serverConfigurationRepository` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val serverConfigurationRepository: ServerConfigurationRepository,
    /**
     * Exposes the `sdModelsRepository` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val sdModelsRepository: StableDiffusionModelsRepository,
    /**
     * Exposes the `sdSamplersRepository` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val sdSamplersRepository: StableDiffusionSamplersRepository,
    /**
     * Exposes the `sdHyperNetworksRepository` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val sdHyperNetworksRepository: StableDiffusionHyperNetworksRepository,
    /**
     * Exposes the `sdEmbeddingsRepository` value used by the SDAI domain layer.
     *
     * @author Dmitriy Moroz
     */
    private val sdEmbeddingsRepository: EmbeddingsRepository,
) : DataPreLoaderUseCase {

    /**
     * Caches connection metadata, propagating failures and cancellation to the caller.
     * LoRAs are fetched on demand by the extras picker instead of during connection.
     *
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke() {
        serverConfigurationRepository.fetchConfiguration()
        sdModelsRepository.fetchModels()
        sdSamplersRepository.fetchSamplers()
        sdHyperNetworksRepository.fetchHyperNetworks()
        sdEmbeddingsRepository.fetchEmbeddings()
    }
}
