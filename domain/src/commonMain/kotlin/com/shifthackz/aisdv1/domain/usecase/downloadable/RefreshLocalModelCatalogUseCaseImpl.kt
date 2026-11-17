package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalModelCatalogResult
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import kotlinx.coroutines.CancellationException

/**
 * Owns offline catalog recovery without hiding transport or persistence failures in data operations.
 *
 * @param repository Strict provider fetch and independent cached metadata reads.
 * @param buildInfoProvider Flavor availability policy; FOSS never fetches or offers MediaPipe models.
 *
 * @author Dmitriy Moroz
 */
internal class RefreshLocalModelCatalogUseCaseImpl(
    private val repository: DownloadableModelRepository,
    private val buildInfoProvider: BuildInfoProvider
) : RefreshLocalModelCatalogUseCase {
    /**
     * Refreshes one provider, retains recoverable fetch errors, and always reads current download/selection state.
     *
     * @param type Runtime family whose catalog is refreshed.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(type: LocalAiModel.Type): LocalModelCatalogResult {
        if (type == LocalAiModel.Type.MediaPipe && buildInfoProvider.type == BuildType.FOSS) return LocalModelCatalogResult(
            emptyList()
        )
        val failure = try {
            repository.fetchCatalog(type)
            null
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            failure
        }
        return LocalModelCatalogResult(repository.readCachedCatalog(type), failure)
    }
}
