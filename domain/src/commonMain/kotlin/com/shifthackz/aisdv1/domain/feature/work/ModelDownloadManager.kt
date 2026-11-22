package com.shifthackz.aisdv1.domain.feature.work

import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import kotlinx.coroutines.flow.Flow

/**
 * Owns single-model or provider-wide downloads independently of the setup screen lifetime.
 * Android persists work; iOS grants finite background execution time.
 *
 * @author Dmitriy Moroz
 */
interface ModelDownloadManager {
    /**
     * Live and terminal snapshots for restoring setup progress.
     *
     * @author Dmitriy Moroz
     */
    val batches: Flow<List<ModelDownloadBatch>>

    /**
     * Starts the confirmed single-model mirror or a provider's missing-model batch.
     * Distinct single models may transfer concurrently; a provider-wide batch runs exclusively.
     * Duplicate model requests are retained unless replacement was explicitly requested.
     *
     * @param request Provider and optional single-model mirror to download.
     * @param shouldReplace Whether to cancel existing work before scheduling this provider.
     * @author Dmitriy Moroz
     */
    suspend fun start(request: ModelDownloadRequest, shouldReplace: Boolean = false)

    /**
     * Stops matching work, preserving models that finished activation.
     *
     * @param type Provider to stop; null stops all model work.
     * @param modelId Single model to stop within the provider; null stops its whole group.
     * @author Dmitriy Moroz
     */
    suspend fun cancel(type: LocalAiModel.Type? = null, modelId: String? = null)
}
