package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import kotlinx.coroutines.flow.Flow

/**
 * Observes catalog model download and selection state used by MNN setup.
 *
 * @author Dmitriy Moroz
 */
interface ObserveLocalMnnModelsUseCase {
    /**
     * Emits cached model states without starting another network refresh.
     *
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Flow<List<LocalAiModel>>
}
