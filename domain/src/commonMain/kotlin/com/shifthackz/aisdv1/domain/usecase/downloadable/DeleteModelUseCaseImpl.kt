package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.entity.LocalAiModel

/**
 * Removes an owned model payload and reconciles any affected provider selection.
 *
 * @param downloadableModelRepository Owned model removal operation.
 * @param preferenceManager Independent provider selections and their observation stream.
 *
 * @author Dmitriy Moroz
 */
internal class DeleteModelUseCaseImpl(
    private val downloadableModelRepository: DownloadableModelRepository,
    private val preferenceManager: PreferenceManager,
) : DeleteModelUseCase {

    /**
     * Deletes the selected owned payload, clearing MNN selection only after removal is accepted.
     *
     * @param id identifier of the target entity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(id: String) {
        downloadableModelRepository.delete(id)
        if (id == "mnn_custom" || id == LocalAiModel.CustomMnn.id) preferenceManager.localMnnCustomActivationId =
            ""
        if ((id.startsWith("mnn_") && preferenceManager.localMnnModelId == id) ||
            ((id == "mnn_custom" || id == LocalAiModel.CustomMnn.id) && preferenceManager.localMnnModelId == LocalAiModel.CustomMnn.id)
        ) {
            preferenceManager.localMnnModelId = ""
            preferenceManager.localMnnActivationId = ""
            preferenceManager.refresh()
        }
    }
}
