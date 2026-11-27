package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.repository.DownloadableModelRepository
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Commits only validated dedicated model selections on supported platforms.
 *
 * @param models Reads current model completion rather than trusting presentation state.
 * @param buildInfo Supplies the host checked against the provider's allowed platforms.
 * @param preferences Independent persisted model selection.
 * @author Dmitriy Moroz
 */
internal class ConfigureUpscalerUseCaseImpl(
    private val models: DownloadableModelRepository,
    private val buildInfo: BuildInfoProvider, private val preferences: PreferenceManager
) : ConfigureUpscalerUseCase {
    /**
     * Rejects unsupported or incomplete models before changing the saved selection.
     *
     * @param modelId Dedicated downloaded upscaler identity.
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(modelId: String) {
        check(buildInfo.platform in UpscalerSource.forModel(modelId).allowedPlatforms)
        val model = models.getById(modelId)
        require(model.type == LocalAiModel.Type.Upscaler && model.downloaded)
        preferences.upscalerModelId = modelId
    }
}
