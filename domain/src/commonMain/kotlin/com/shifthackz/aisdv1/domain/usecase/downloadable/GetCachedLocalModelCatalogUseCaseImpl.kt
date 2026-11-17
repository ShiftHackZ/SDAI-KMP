package com.shifthackz.aisdv1.domain.usecase.downloadable

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository

/**
 * Applies flavor availability to a strict cache-only catalog read.
 *
 * @param repository Cached metadata and current owned-file state; never refreshed by this operation.
 * @param buildInfoProvider Keeps MediaPipe unavailable in FOSS before any repository access.
 *
 * @author Dmitriy Moroz
 */
internal class GetCachedLocalModelCatalogUseCaseImpl(
    private val repository: DownloadableModelRepository,
    private val buildInfoProvider: BuildInfoProvider,
) : GetCachedLocalModelCatalogUseCase {
    /**
     * Reads the requested cached runtime choices without fetching metadata or hiding cache errors.
     *
     * @param type Runtime family whose existing models should be displayed.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(type: LocalAiModel.Type): List<LocalAiModel> =
        if (type == LocalAiModel.Type.MediaPipe && buildInfoProvider.type == BuildType.FOSS) emptyList()
        else repository.readCachedCatalog(type)
}
