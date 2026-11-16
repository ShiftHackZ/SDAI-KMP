package com.shifthackz.aisdv1.domain.usecase.easydiffusion

/** Persists checkpoint selection only for the instance/revision that supplied the picker choices.
 * @author Dmitriy Moroz */
interface SelectEasyDiffusionModelUseCase {
    /** Rejects stale picker actions without changing the replacement connection.
     * @param modelId Exact model identifier from a successful endpoint-scoped catalog.
     * @param url Instance that supplied the displayed choices.
     * @param selectionRevision Provider revision associated with those choices.
     * @return True only if the same connection still owns the selection and the model remains cached.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(modelId: String, url: String, selectionRevision: Long): Boolean
}
