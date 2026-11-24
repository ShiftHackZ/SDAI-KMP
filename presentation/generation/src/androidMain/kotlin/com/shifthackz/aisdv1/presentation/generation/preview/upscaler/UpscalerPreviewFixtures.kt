package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewFixtures
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormState
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerState

/**
 * Fixed model and image inputs shared by standalone and component previews.
 *
 * @author Dmitriy Moroz
 */
internal object UpscalerPreviewFixtures {
    val esrgan = LocalAiModel(
        id = "upscaler_esrgan_anime", type = LocalAiModel.Type.Upscaler,
        name = "Real-ESRGAN Anime 6B", size = "17 MB", sources = emptyList(),
        downloaded = true,
    )
    val mnn = esrgan.copy(id = "upscaler_mnn_anime", name = "Real-ESRGAN Anime 6B (MNN)")
    val configuration = UpscalerConfiguration(esrgan.id)
    val form = UpscalerFormState(
        isSupported = true,
        platform = Platform.ANDROID,
        model = esrgan,
        models = listOf(
            esrgan,
            esrgan.copy(id = "upscaler_esrgan_x4plus", name = "Real-ESRGAN x4plus")
        ),
        isLoading = false,
    )
    val screen = UpscalerState(
        platform = Platform.ANDROID, isSupported = true,
        image = GenerationPreviewFixtures.ImageBase64, width = 512, height = 512,
        configuration = configuration, outputSize = UpscalerImageSize(2048, 2048),
    )
}
