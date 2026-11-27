package com.shifthackz.aisdv1.presentation.components.model

import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.UpscalerSource

/**
 * Resolves the provider's localized name consistently in setup and generation forms.
 *
 * @author Dmitriy Moroz
 */
val UpscalerSource.displayName: String
    get() = Localization.string(
        when (this) {
            UpscalerSource.LOCAL_ESRGAN -> "upscaler_local_provider"
            UpscalerSource.LOCAL_MNN -> "upscaler_mnn_provider"
        }
    )

/**
 * Describes the selected runtime and its model setup in provider discovery and configuration.
 *
 * @author Dmitriy Moroz
 */
val UpscalerSource.description: String
    get() = Localization.string(
        when (this) {
            UpscalerSource.LOCAL_ESRGAN -> "upscaler_esrgan_description"
            UpscalerSource.LOCAL_MNN -> "upscaler_mnn_description"
        }
    )
