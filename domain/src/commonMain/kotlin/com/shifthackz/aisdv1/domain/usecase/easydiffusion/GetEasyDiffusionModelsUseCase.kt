package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel

/** Reads only the configured instance catalog after an explicit refresh.
 * @author Dmitriy Moroz */
interface GetEasyDiffusionModelsUseCase {
    /** Returns the configured endpoint’s successfully fetched catalog, or an empty list before its first successful refresh.
     * @param url Captured instance root or prefix; the operation never rereads current preferences.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(url: String): List<EasyDiffusionModel>
}
