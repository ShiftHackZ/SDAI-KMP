package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Applies sequential download and fail-fast policy without hiding repository failures.
 *
 * @param catalog Reads current file-backed completion state.
 * @param download Streams one model through the normal archive activation path.
 * @author Dmitriy Moroz
 */
internal class DownloadModelsUseCaseImpl(
    private val catalog: GetCachedLocalModelCatalogUseCase,
    private val download: DownloadModelUseCase,
) : DownloadModelsUseCase {
    /**
     * Skips installed models, activates missing archives serially and stops at the first failure.
     *
     * @param request Provider-wide selection or one confirmed model mirror.
     * @author Dmitriy Moroz
     */
    override fun invoke(request: ModelDownloadRequest) = flow {
        val catalogModels = catalog(request.type)
        require(request.modelId == null || catalogModels.any { it.id == request.modelId && it.sources.isNotEmpty() }) {
            "The requested downloadable model is unavailable"
        }
        val models = catalogModels.filter {
            !it.downloaded && it.sources.isNotEmpty() && (request.modelId == null || request.modelId == it.id)
        }
        var snapshot = ModelDownloadBatch(
            request.type,
            totalModelCount = models.size,
            isBatch = request.isBatch
        )
        for (model in models) {
            snapshot = snapshot.copy(
                modelId = model.id,
                modelName = model.name,
                progress = DownloadState.Downloading()
            )
            emit(snapshot)
            var hasCompleted = false
            download(model.id, request.sourceUrl ?: model.sources.first()).distinctUntilChanged()
                .collect { progress ->
                    if (progress is DownloadState.Error) throw progress.error
                    if (progress is DownloadState.Complete) hasCompleted = true
                    snapshot = snapshot.copy(
                        progress = progress,
                        completedIds = if (progress is DownloadState.Complete) snapshot.completedIds + model.id else snapshot.completedIds,
                    )
                    emit(snapshot)
                }
            check(hasCompleted) { "Model download ended without installing ${model.id}" }
        }
        emit(snapshot.copy(isRunning = false))
    }
}
