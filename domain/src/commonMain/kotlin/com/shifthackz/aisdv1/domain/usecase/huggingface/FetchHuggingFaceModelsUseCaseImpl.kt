package com.shifthackz.aisdv1.domain.usecase.huggingface

import com.shifthackz.aisdv1.domain.entity.HuggingFaceModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.HuggingFaceModelsRepository

/**
 * Loads Hugging Face models only while that provider is selected.
 *
 * @param preferenceManager Identifies the currently selected generation provider.
 * @param repository Refreshes and reads the Hugging Face model catalog.
 *
 * @author Dmitriy Moroz
 */
internal class FetchHuggingFaceModelsUseCaseImpl(
    private val preferenceManager: PreferenceManager,
    private val repository: HuggingFaceModelsRepository,
) : FetchHuggingFaceModelsUseCase {

    /**
     * Returns refreshed Hugging Face models, or an empty list for another provider.
     *
     * @return The catalog available for the current provider.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(): List<HuggingFaceModel> {
        if (preferenceManager.source != ServerSource.HUGGING_FACE) return emptyList()

        return repository.fetchAndGetHuggingFaceModels()
    }
}
