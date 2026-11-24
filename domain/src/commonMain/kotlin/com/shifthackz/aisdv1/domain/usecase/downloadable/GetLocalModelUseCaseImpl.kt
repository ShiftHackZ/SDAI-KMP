package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Reads one model with current download and independent selection state through the model repository.
 *
 * @param repository Catalog and owned-model metadata reader.
 *
 * @author Dmitriy Moroz
 */
internal class GetLocalModelUseCaseImpl(
    private val repository: DownloadableModelRepository,
) : GetLocalModelUseCase {

    /**
     * Reads one catalog entry; lookup and storage failures remain observable.
     *
     * @param id Stable local model catalog identity.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(id: String): LocalAiModel = repository.getById(id)
}
