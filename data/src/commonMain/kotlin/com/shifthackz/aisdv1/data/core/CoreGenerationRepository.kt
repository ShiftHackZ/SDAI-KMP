package com.shifthackz.aisdv1.data.core

import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import kotlin.coroutines.coroutineContext
import com.shifthackz.aisdv1.domain.entity.GenerationMetadataContext
import com.shifthackz.aisdv1.domain.entity.GenerationFinalizationContext
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Freezes request provenance before optional gallery persistence and original-image export.
 *
 * @param mediaStoreGateway Platform original-image export adapter.
 * @param localDataSource Gallery writer used for autosave and background work.
 * @param preferenceManager Autosave preferences.
 * @param backgroundWorkObserver Determines whether a background task requires durable output.
 * @author Dmitriy Moroz
 */
internal abstract class CoreGenerationRepository(
    mediaStoreGateway: MediaStoreGateway,
    private val localDataSource: GenerationResultDataSource.Local,
    private val preferenceManager: PreferenceManager,
    private val backgroundWorkObserver: BackgroundWorkObserver,
) : CoreMediaStoreRepository(preferenceManager, mediaStoreGateway) {

    /**
     * Attaches request metadata to every result, including unsaved foreground images, before optional writes.
     *
     * @param ai Completed provider image, retaining its account guards.
     * @return Provenance-bearing image with the saved identity when autosave is active.
     * @author Dmitriy Moroz
     */
    protected suspend fun insertGenerationResult(ai: AiGenerationResult): AiGenerationResult {
        val result = coroutineContext[GenerationMetadataContext]?.completed(ai) ?: ai
        if (coroutineContext[GenerationFinalizationContext] != null) return result
        if (backgroundWorkObserver.hasActiveTasks() || preferenceManager.autoSaveAiResults) {
            val id = localDataSource.insert(result)
            exportToMediaStoreAsync(result)
            return result.copy(id = id)
        }
        return result
    }
}
