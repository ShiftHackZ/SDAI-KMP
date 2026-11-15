package com.shifthackz.aisdv1.domain.usecase.caching

import com.shifthackz.aisdv1.domain.repository.EmbeddingsRepository
import com.shifthackz.aisdv1.domain.repository.ServerConfigurationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionHyperNetworksRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionModelsRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionSamplersRepository
import kotlinx.coroutines.CancellationException

/**
 * Fetches connection metadata in order, leaving LoRA discovery to the extras picker.
 * A1111 may scan thousands of LoRA files before responding, so that request must
 * not consume the connection or startup preload deadline.
 * Recoverable hypernetwork discovery failures do not fail setup; the extras picker retries discovery.
 *
 * @param serverConfigurationRepository Active server options to cache before model discovery.
 * @param sdModelsRepository Available generation models.
 * @param sdSamplersRepository Available sampling algorithms.
 * @param sdHyperNetworksRepository Optional hypernetwork discovery whose failures allow preload to continue.
 * @param sdEmbeddingsRepository Available textual inversions.
 *
 * @author Dmitriy Moroz
 */
internal class DataPreLoaderUseCaseImpl(
    private val serverConfigurationRepository: ServerConfigurationRepository,
    private val sdModelsRepository: StableDiffusionModelsRepository,
    private val sdSamplersRepository: StableDiffusionSamplersRepository,
    private val sdHyperNetworksRepository: StableDiffusionHyperNetworksRepository,
    private val sdEmbeddingsRepository: EmbeddingsRepository,
) : DataPreLoaderUseCase {

    /**
     * Caches connection metadata, continuing to embeddings if optional hypernetwork loading fails.
     * Required metadata failures, cancellation and nonrecoverable errors propagate to the caller.
     * LoRAs are fetched on demand by the extras picker instead of during connection.
     *
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke() {
        serverConfigurationRepository.fetchConfiguration()
        sdModelsRepository.fetchModels()
        sdSamplersRepository.fetchSamplers()
        runCatching {
            sdHyperNetworksRepository.fetchHyperNetworks()
        }.onFailure {
            if (it is CancellationException || it !is Exception) throw it
        }
        sdEmbeddingsRepository.fetchEmbeddings()
    }
}
