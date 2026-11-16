package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository

/** Makes stale picker events harmless while keeping the active instance's complete checkpoint ID.
 * @param preferences Active connection and selected checkpoint preferences.
 * @param repository Endpoint-scoped model cache; this operation never performs network discovery.
 * @param selectionGuard Serializes validation and the preference write with connection replacement.
 * @author Dmitriy Moroz */
internal class SelectEasyDiffusionModelUseCaseImpl(
    private val preferences: PreferenceManager,
    private val repository: EasyDiffusionRepository,
    private val selectionGuard: ProviderSelectionGuard,
) : SelectEasyDiffusionModelUseCase {
    /** Writes the checkpoint only when the same endpoint/revision still owns a successfully fetched catalog.
     * @param modelId Exact catalog checkpoint ID to persist.
     * @param url Endpoint associated with the displayed picker.
     * @param selectionRevision Revision associated with the displayed picker.
     * @author Dmitriy Moroz */
    override suspend fun invoke(modelId: String, url: String, selectionRevision: Long): Boolean =
        selectionGuard.withSelection {
            if (preferences.source != ServerSource.EASY_DIFFUSION || preferences.easyDiffusionUrl != url ||
                preferences.providerSelectionRevision != selectionRevision
            ) return@withSelection false
            if (repository.getModels(url).none { it.id == modelId }) return@withSelection false
            preferences.easyDiffusionModel = modelId
            true
        }
}
