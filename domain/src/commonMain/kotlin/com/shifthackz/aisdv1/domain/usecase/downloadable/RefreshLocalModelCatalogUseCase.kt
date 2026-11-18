package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalModelCatalogResult

/**
 * Refreshes one runtime catalog while retaining an explicit failure and usable cached choices.
 *
 * @author Dmitriy Moroz
 */
interface RefreshLocalModelCatalogUseCase {
    /**
     * Reads the current cache after a strict provider fetch; cancellation and cache-read failures propagate.
     *
     * @param type Runtime family whose metadata is refreshed independently.
     *
     * @author Dmitriy Moroz
     */
    suspend operator fun invoke(type: LocalAiModel.Type): LocalModelCatalogResult
}
