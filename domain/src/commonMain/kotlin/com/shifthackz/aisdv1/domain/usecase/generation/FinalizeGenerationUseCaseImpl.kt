package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.GenerationResultRepository

/**
 * Finalizes transformed images through the normal guarded gallery writer.
 *
 * @param repository Persists images and applies media export preferences.
 * @param preferences Foreground autosave policy.
 * @param work Identifies work requiring durable output.
 * @author Dmitriy Moroz
 */
internal class FinalizeGenerationUseCaseImpl(
    private val repository: GenerationResultRepository,
    private val preferences: PreferenceManager, private val work: BackgroundWorkObserver
) : FinalizeGenerationUseCase {
    /**
     * Keeps manual-save foreground output transient; background output is always persisted.
     *
     * @param result Final pixels with the original provider/account guards.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(result: AiGenerationResult): AiGenerationResult =
        if (preferences.autoSaveAiResults || work.hasActiveTasks()) result.copy(
            id = repository.insert(
                result
            )
        ) else result
}
