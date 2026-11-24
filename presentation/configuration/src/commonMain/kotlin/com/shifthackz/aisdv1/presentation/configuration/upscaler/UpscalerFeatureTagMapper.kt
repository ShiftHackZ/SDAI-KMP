package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag

/**
 * Localizes independent super-resolution capability labels for chips and search.
 *
 * @author Dmitriy Moroz
 */
internal fun UpscalerFeatureTag.mapToUi(): String = Localization.string(
    when (this) {
        UpscalerFeatureTag.Offline -> "provider_tag_offline"
        UpscalerFeatureTag.MultipleModels -> "provider_tag_multiple_models"
        UpscalerFeatureTag.TiledInference -> "upscaler_tag_tiled_inference"
    }
)
