package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Retains only meaningful MNN setup state changes.
 *
 * @param repository Cached metadata and selection observation.
 *
 * @author Dmitriy Moroz
 */
internal class ObserveLocalMnnModelsUseCaseImpl(private val repository: DownloadableModelRepository) :
    ObserveLocalMnnModelsUseCase {
    /**
     * Observes cached models without swallowing storage failures.
     *
     * @author Dmitriy Moroz
     */
    override fun invoke() = repository.observeAllMnn().distinctUntilChanged()
}
