package com.shifthackz.aisdv1.domain.usecase.sdmodel

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.StableDiffusionModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.ServerConfigurationRepository
import com.shifthackz.aisdv1.domain.repository.StableDiffusionModelsRepository

/**
 * Loads Automatic1111 checkpoints and marks the server-selected checkpoint.
 *
 * @param preferenceManager Identifies whether Automatic1111 is the selected provider.
 * @param serverConfigurationRepository Reads the refreshed server checkpoint selection.
 * @param sdModelsRepository Refreshes and reads available checkpoint descriptors.
 *
 * @author Dmitriy Moroz
 */
internal class GetStableDiffusionModelsUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val serverConfigurationRepository: ServerConfigurationRepository,
    private val sdModelsRepository: StableDiffusionModelsRepository,
) : GetStableDiffusionModelsUseCase {

    /**
     * Returns checkpoints paired with their selected state, or an empty list for another provider.
     *
     * @return The catalog available for the current provider.
     * @author Dmitriy Moroz
     */
    override suspend operator fun invoke(): List<Pair<StableDiffusionModel, Boolean>> {
        if (preferenceManager.source != ServerSource.AUTOMATIC1111) return emptyList()

        val config = serverConfigurationRepository.fetchAndGetConfiguration()
        val sdModels = sdModelsRepository.fetchAndGetModels()
        return sdModels.map { model ->
            model to (config.sdModelCheckpoint == model.title)
        }
    }
}
